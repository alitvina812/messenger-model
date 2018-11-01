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

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import static javax.persistence.InheritanceType.JOINED;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Track")
@PrimaryKeyJoinColumn(name = "trackIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
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
	
	@Column(nullable = false, updatable = false, insertable = true)
	private long albumReference;
	
	@Column(nullable = false, updatable = false, insertable = true)
	private long ownerReference;
	
	@Column(nullable = false, updatable = false, insertable = true)
	private long recordingReference;
	
	public Track(Album album, Person owner, Document recording) {
		this.album = album;
		this.owner = owner;
		this.recording = recording;
	}
	
	protected Track() {
		this(null, null, null);
	}
	
	@JsonbProperty("track-name")
	@JsonbTransient
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@JsonbProperty("track-artist")
	@JsonbTransient
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	@JsonbProperty("track-genre")
	@JsonbTransient
	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	@JsonbProperty("track-ordinal")
	@JsonbTransient
	public byte getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(byte ordinal) {
		this.ordinal = ordinal;
	}

	@JsonbProperty("album")
	@JsonbTransient
	public Album getAlbum() {
		return album;
	}

	@JsonbProperty("track-owner")
	@JsonbTransient
	public Person getOwner() {
		return owner;
	}

	@JsonbProperty("recording")
	@JsonbTransient
	public Document getRecording() {
		return recording;
	}

	@JsonbProperty()
	@JsonbTransient
	protected long getAlbumReference() {
		return this.albumReference;
	}
	
	protected void setAlbumReference (final long albumReference) {
		this.albumReference = albumReference;
	}
	
	@JsonbProperty()
	@JsonbTransient
	protected long getOwnerReference() {
		return this.ownerReference;
	}
	
	protected void setOwnerReference (final long ownerReference) {
		this.ownerReference = ownerReference;
	}
	
	@JsonbProperty()
	@JsonbTransient
	protected long getRecordingReference() {
		return this.recordingReference;
	}
	
	protected void setRecordingReference (final long recordingReference) {
		this.recordingReference = recordingReference;
	}
	
	
}