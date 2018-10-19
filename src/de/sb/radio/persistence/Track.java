package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Track")
@Inheritance(strategy = JOINED)
public class Track extends BaseEntity{

	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String artist;
	@Column(nullable = false)
	private String genre;
	@Column(nullable = false)
	private byte ordinal;
	@Column(name = "albumReference", nullable = false)
	private Album album;
	@Column(name = "ownerReference", nullable = false)
	private Person owner;
	private Document recording;
	
	protected Track() {
		this.name = "";
		this.artist = "";
		this.genre = "";
		this.ordinal = 0;
		this.album = new Album();
		this.owner = new Person();
		this.recording = new Document();
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
