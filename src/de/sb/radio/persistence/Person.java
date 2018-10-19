package de.sb.radio.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import static javax.persistence.InheritanceType.JOINED;


@Entity
@Table(schema = "radio", name = "Person")
@Inheritance(strategy = JOINED)
public class Person extends BaseEntity{
	
	@Column(nullable = false)
	private String email;
	@Column(nullable = false)
	private byte[] passwordHash;
	
	private Name name;
	
	@Column
	private Track[] tracks;	
	
	protected Person() {
		this.email = "email@mail.de";
		this.passwordHash = new byte[32];
		this.name = new Name();
		this.tracks = new Track[15];
	}
	
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	public byte[] getPasswordHash() {
		return this.passwordHash;
	}
	
	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	public Track[] getTracks() {
		return this.tracks;
	}
	
	public void setTracks(Track[] tracks) {
		this.tracks = tracks;
	}
	
	public Name getName() {
		return this.name;
	}
	
	public void setName(String forename, String lastname) {
		Name name = new Name(forename, lastname);
		this.name = name;
	}
}
