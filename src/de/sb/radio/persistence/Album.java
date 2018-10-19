package de.sb.radio.persistence;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import static javax.persistence.InheritanceType.JOINED;

import javax.persistence.Column;

@Entity
@Table(schema = "radio", name = "Album")
@Inheritance(strategy = JOINED)
public class Album extends BaseEntity{

	@Column(nullable = false)
	private String title;
	@Column(name = "publication", nullable = false)
	private short releaseYear;
	private byte trackCount;
	@Column(name = "coverReference", nullable = false)
	private Document cover;
	private Track[] tracks;
	
	protected Album() {
		this.title = "";
		this.releaseYear = 0;
		this.trackCount = 0;
		this.cover = new Document();
		this.tracks = new Track[15];
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
