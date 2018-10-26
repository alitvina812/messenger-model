package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Track")
@PrimaryKeyJoinColumn(name = "trackIdentity")
public class Track extends BaseEntity{

	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 127)
	private String name;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 127)
	private String artist;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 31)
	private String genre;
	
	@Column(nullable = false, updatable = true)
	@PositiveOrZero
	private byte ordinal;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name="albumReference", nullable = false, updatable = false, insertable = true)
	private Album album;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "ownerReference", nullable = false, updatable = false, insertable = true)
	private Person owner;
	
	@NotNull
	@ManyToOne(optional = false)
	@JoinColumn(name = "recordingReference", nullable = false, updatable = false, insertable = true)
	private Document recording;
	
	public Track(Album album, Person owner, Document recording) {
		this.album = album;
		this.owner = owner;
		this.recording = recording;
	}
	
	protected Track() {
		this(null, null, null);
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


	public Person getOwner() {
		return owner;
	}

	public Document getRecording() {
		return recording;
	}

	
}
