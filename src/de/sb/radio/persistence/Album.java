package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Album")
@Inheritance(strategy = JOINED)
public class Album extends BaseEntity{

	@Column(nullable = false)
	@NotNull
	@Size(max = 127)
	private String title;
	@Column(name = "publication", nullable = false)
	@NotNull
	private short releaseYear;
	private byte trackCount;
	@Column(name = "coverReference", nullable = false)
	@NotNull
	private Document cover;
	@OneToMany(mappedBy = "album", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	protected Album() {
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

	public Track[] getTracks() {
		return tracks;
	}

	public void setTracks(Track[] tracks) {
		this.tracks = tracks;
	}
	
	
}
