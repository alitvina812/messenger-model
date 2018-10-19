package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Document")
@Inheritance(strategy = JOINED)
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
	
	public byte[] _scaledImageContent(String fileType, byte[] content, int width, int height) {
		return content;
	}
	
	public byte[] getContentHash() {
		return this.contentHash;
	}
	
	
	public String getContentType() {
		return this.contentType;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public byte[] getContent() {
		return this.content;
	}
	
	public void setContent(byte[] content) {
		this.content = content;
		this.contentHash = HashTools.sha256HashCode(content);
	}
}
