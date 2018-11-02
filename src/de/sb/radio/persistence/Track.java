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
	
	public Track(Album album, Person owner, Document recording) {
		this.album = album;
		this.owner = owner;
		this.recording = recording;
	}
	
	protected Track() {
		this(null, null, null);
	}
	
	@JsonbProperty
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@JsonbProperty
	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	@JsonbProperty
	public String getGenre() {
		return genre;
	}

	public void setGenre(String genre) {
		this.genre = genre;
	}

	@JsonbProperty
	public byte getOrdinal() {
		return ordinal;
	}

	public void setOrdinal(byte ordinal) {
		this.ordinal = ordinal;
	}

	@JsonbTransient
	public Album getAlbum() {
		return album;
	}

	@JsonbTransient
	public Person getOwner() {
		return owner;
	}

	@JsonbTransient
	public Document getRecording() {
		return recording;
	}

	@JsonbProperty()
	protected long getAlbumReference() {
		return this.album == null ? 0 : this.album.getIdentity();
	}
	
	
	@JsonbProperty()
	protected long getOwnerReference() {
		return this.owner == null ? 0 : this.owner.getIdentity();
	}
	
	
	@JsonbProperty()
	protected long getRecordingReference() {
		return this.recording == null ? 0 : this.recording.getIdentity();
	}
	
}