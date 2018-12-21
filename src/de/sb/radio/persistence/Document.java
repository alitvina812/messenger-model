package de.sb.radio.persistence;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.AudioFileFormat.Type;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

@Entity
@Table(schema = "radio", name = "Document")
@PrimaryKeyJoinColumn(name = "documentIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Document extends BaseEntity {
	private static final int PCM_SIGNED_SIZE = 2;
	static private final byte[] DEFAULT_CONTENT = new byte[0];
	static private final byte[] DEFAULT_CONTENT_HASH = HashTools.sha256HashCode(DEFAULT_CONTENT);

	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 32, max = 32)
	private byte[] contentHash;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 63)
	private String contentType;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1)
	private byte[] content;
	
	public Document() {
		this.content = DEFAULT_CONTENT;
		this.contentHash = DEFAULT_CONTENT_HASH;
		this.contentType = "application/octet-stream";
	}

	
	@JsonbProperty
	public byte[] getContentHash() {
		return this.contentHash;
	}
	
	@JsonbProperty
	public String getContentType() {
		return this.contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	@JsonbTransient
	public byte[] getContent() {
		return this.content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
		this.contentHash = HashTools.sha256HashCode(content);
	}
	
	static public byte[] scaledImageContent (final String fileType, final byte[] content, final int width, final int height) throws NullPointerException, IllegalArgumentException {
		try {
			if (fileType == null | content == null) throw new NullPointerException();
			if (width < 0 | height < 0) throw new IllegalArgumentException();
			if (width == 0 & height == 0) return content;
			
			final BufferedImage originalImage;
			try (InputStream byteSource = new ByteArrayInputStream(content)) {
				originalImage = ImageIO.read(byteSource);
			}
			
			final int scaleWidth = width == 0 ? originalImage.getWidth() * height / originalImage.getHeight() : width;
			final int scaleHeight = height == 0 ? originalImage.getHeight() * width / originalImage.getWidth() : height;
			final BufferedImage scaledImage = new BufferedImage(scaleWidth, scaleHeight, originalImage.getType());
			final Graphics2D graphics = scaledImage.createGraphics();
			try {
				graphics.drawImage(originalImage, 0, 0, scaleWidth, scaleHeight, null);
			} finally {
				graphics.dispose();
			}
			
			try (ByteArrayOutputStream byteSink = new ByteArrayOutputStream()) {
				final boolean supported = ImageIO.write(scaledImage, fileType, byteSink);
				if (!supported) throw new IllegalArgumentException();
				return byteSink.toByteArray();
			}
		} catch (final IOException exception) {
			// there should never be I/O errors with byte array based I/O streams
			throw new AssertionError(exception);
		}
	}


	static public byte[] processedAudioContent (final byte[] content, final Processor processor) throws IOException, UnsupportedAudioFileException {
		// step 1: convert file format frames into WAV PCM_SIGNED frame content, excluding the audio headers
		final byte[] frameContent; 
		final AudioFormat frameFormat;
		try (ByteArrayOutputStream byteSink = new ByteArrayOutputStream()) {
			try (ByteArrayInputStream byteSource = new ByteArrayInputStream(content)) {
				try (AudioInputStream fileSource = AudioSystem.getAudioInputStream(byteSource)) {
					final float frameRate = fileSource.getFormat().getSampleRate();
					final int frameWidth = fileSource.getFormat().getChannels();
					frameFormat = new AudioFormat(Encoding.PCM_SIGNED, frameRate, PCM_SIGNED_SIZE * Byte.SIZE, frameWidth, PCM_SIGNED_SIZE * frameWidth, frameRate, false);
	
					try (AudioInputStream audioSource = AudioSystem.getAudioInputStream(frameFormat, fileSource)) {
						final byte[] frameBuffer = new byte[frameFormat.getFrameSize()];
						for (int bytesRead = audioSource.read(frameBuffer); bytesRead != -1; bytesRead = audioSource.read(frameBuffer)) {
							byteSink.write(frameBuffer, 0, bytesRead);
						}
					}
				}
			}
			frameContent = byteSink.toByteArray();
		}

		// step 2: process the WAV PCM_SIGNED frames
		final double[] frame = new double[frameFormat.getChannels()];
		for (int position = 0; position < frameContent.length; position += frame.length * PCM_SIGNED_SIZE) {
			for (int channel = 0; channel < frame.length; ++channel) {
				frame[channel] = readNormalizedSample(frameContent, position + channel * PCM_SIGNED_SIZE);
			}

			processor.process(frame);

			for (int channel = 0; channel < frame.length; ++channel) {
				writeNormalizedSample(frameContent, position + channel * PCM_SIGNED_SIZE, frame[channel]);
			}
		}

		// Step 3: convert WAV PCM_SIGNED frame content into WAV file format, including the audio headers
		final long frameCount = frameContent.length / frameFormat.getFrameSize();
		try (ByteArrayOutputStream byteSink = new ByteArrayOutputStream()) {
			try (ByteArrayInputStream byteSource = new ByteArrayInputStream(frameContent)) {
				try (AudioInputStream audioSource = new AudioInputStream(byteSource, frameFormat, frameCount)) {
					AudioSystem.write(audioSource, Type.WAVE, byteSink);
				}
			}
			return byteSink.toByteArray();
		}
	}
	
	
	/**
	 * Reads a normalized sample value within range [-1, +1] from the given
	 * frame buffer.
	 * @param frameBuffer the frame buffer
	 * @param offset the sample offset
	 * @return the unpacked and normalized sample 
	 * @throws NullPointerException if the given frame buffer is {@code null}
	 * @throws ArrayIndexOutOfBoundsException if the given offset is out of bounds
	 */
	static private double readNormalizedSample (byte[] frameBuffer, int offset) throws ArrayIndexOutOfBoundsException {
		final double sample = (frameBuffer[offset] & 0xFF) + (frameBuffer[offset + 1] << 8);
		return sample >= 0 ? +sample / Short.MAX_VALUE : -sample / Short.MIN_VALUE;
	}


	/**
	 * Writes a normalized sample value within range [-1, +1] into the given
	 * frame buffer.
	 * @param frameBuffer the frame buffer
	 * @param offset the sample offset
	 * @param the normalized sample to be packed 
	 * @throws NullPointerException if the given frame buffer is {@code null}
	 * @throws ArrayIndexOutOfBoundsException if the given offset is out of bounds
	 */
	static private void writeNormalizedSample (byte[] frameBuffer, int offset, double sample) throws ArrayIndexOutOfBoundsException {
		sample	= sample >= -1 ? (sample <= +1 ? sample : +1) : -1;

		final long value = Math.round(sample >= 0 ? +sample * Short.MAX_VALUE : -sample * Short.MIN_VALUE);
		frameBuffer[offset]		= (byte) (value >>> 0);	// pack LSB
		frameBuffer[offset + 1]	= (byte) (value >>> 8); // pack HSB
	}
}