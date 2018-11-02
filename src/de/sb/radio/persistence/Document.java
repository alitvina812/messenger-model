package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import static javax.persistence.InheritanceType.JOINED;

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

@Entity
@Table(schema = "radio", name = "Document")
@PrimaryKeyJoinColumn(name = "documentIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Document extends BaseEntity{
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
}