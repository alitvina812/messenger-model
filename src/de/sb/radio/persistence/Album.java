package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Album")
@Inheritance(strategy = JOINED)
public class Album extends BaseEntity{

	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(max = 127)
	private String title;
	
	@Column(name = "publication", nullable = false, updatable = true)
	@NotNull
	private short releaseYear;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	private byte trackCount;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "coverReference", nullable = false, updatable = true)
	private Document cover;
	
	@OneToMany(mappedBy = "album", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	public Album(Document cover) {
		this.cover = cover;
		this.tracks = new HashSet<Track>();
		this.trackCount = (byte) this.tracks.size();
	}
	
	protected Album() {
		this(null);
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public short getReleaseYear() {
		return releaseYear;
	}
	
	public void setReleaseYear(short releaseYear) {
		this.releaseYear = releaseYear;
	}

	public byte getTrackCount() {
		return trackCount;
	}

	public void setTrackCount(byte trackCount) {
		this.trackCount = trackCount;
	}

	public Document getCover() {
		return cover;
	}

	public void setCover(Document cover) {
		this.cover = cover;
	}

	public Set<Track> getTracks() {
		return tracks;
	}

	public void setTracks(Set<Track> tracks) {
		this.tracks = tracks;
	}
	
	
}
