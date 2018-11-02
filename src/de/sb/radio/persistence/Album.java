package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;
import de.sb.toolbox.val.NotEqual;

import static javax.persistence.InheritanceType.JOINED;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.CascadeType;
import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Album")
@PrimaryKeyJoinColumn(name = "albumIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Album extends BaseEntity{

	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 127)
	private String title;
	
	@Column(name = "publication", nullable = false, updatable = true)
	@NotEqual("0")
	private short releaseYear;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Positive
	private byte trackCount;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "coverReference", nullable = false, updatable = false, insertable = true)
	private Document cover;
	
	@OneToMany(mappedBy = "album", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	
	public Album(Document cover) {
		this.cover = cover;
		this.tracks = Collections.emptySet();
	}
	
	protected Album() {
		this(null);
	}
	
	@JsonbProperty
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	@JsonbProperty
	public short getReleaseYear() {
		return releaseYear;
	}
	
	public void setReleaseYear(short releaseYear) {
		this.releaseYear = releaseYear;
	}

	@JsonbProperty
	public byte getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(byte trackCount) {
		this.trackCount = trackCount;
	}

	@JsonbTransient
	public Document getCover() {
		return cover;
	}

	@JsonbTransient
	public Set<Track> getTracks() {
		return tracks;
	}
	
	@JsonbProperty()
	protected long getCoverReference() {
		return this.cover == null ? 0 : this.cover.getIdentity();
	}
	
	
	@JsonbProperty()
	protected long[] getTrackReferences() {
		return this.tracks.stream().mapToLong(track -> track.getIdentity()).toArray();
	}

}