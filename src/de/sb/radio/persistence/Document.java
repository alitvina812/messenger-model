package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Document")
@Inheritance(strategy = JOINED)
public class Document extends BaseEntity{

	@Column(nullable = false)
	private byte[] contentHash;
	
	@Column(nullable = false)
	private String contentType;
	
	@Column(nullable = false)
	private byte[] content;
	
	protected Document() {
		this.contentHash = new byte[32];
		this.contentType = "";
		this.content = new byte[100];
	}
	
	public byte[] _scaledImageContent(String fileType, byte[] content, int width, int height) {
		byte[] result = new byte[1];
		return result;
	}
	
	public byte[] getContentHash() {
		return this.contentHash;
	}
	
	public void setContentHash(byte[] contentHash) {
		this.contentHash = contentHash;
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
	}
}
