package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Track")
@Inheritance(strategy = JOINED)
public class Track extends BaseEntity{

	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 127)
	private String name;
	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 127)
	private String artist;
	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 31)
	private String genre;
	@Column(nullable = false)
	@NotNull
	private byte ordinal;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name="albumReference", nullable = false, updatable = false)
	private Album album;
	
	@Column(name = "ownerReference", nullable = false)
	@NotNull
	@ManyToOne(fetch=FetchType.LAZY)
	private Person owner;
	private Document recording;
	
	protected Track() {
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	public byte getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(byte ordinal) {
		this.ordinal = ordinal;
	}

	public Album getAlbum() {
		return album;
	}

	public void setAlbum(Album album) {
		this.album = album;
	}

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person owner) {
		this.owner = owner;
	}

	public Document getRecording() {
		return recording;
	}

	public void setRecording(Document recording) {
		this.recording = recording;
	}
	
}
