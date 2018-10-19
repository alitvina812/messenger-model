package de.sb.radio.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import static javax.persistence.InheritanceType.JOINED;

import java.util.Collections;
import java.util.Set;


@Entity
@Table(schema = "radio", name = "Person")
@Inheritance(strategy = JOINED)
public class Person extends BaseEntity{
	static enum Group {
		USER, ADMIN;
	}
	
	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 127)
	@Email
	private String email;
	
	@Column(nullable = false)
	@NotNull
	@Size(min = 32, max = 32)
	private byte[] passwordHash;
	
	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 31)
	private String forename;
	@Column(name = "surname", nullable = false)
	@NotNull
	@Size(min = 1, max = 31)
	private String lastname;
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "groupAlias")
	private Group group;
	
	@OneToMany(mappedBy="owner")
	private Set<Track> tracks;
	
	@ManyToOne
	@JoinColumn(name="avatarReference")
	private Document avatar;
	
	public Person(Document avatar) {
		this.tracks = Collections.emptySet();
		this.avatar = avatar;
		this.group = Group.USER;
	}
	
	protected Person() {
		this(null);
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
	
	public Set<Track> getTracks() {
		return this.tracks;
	}
	
	public String getForename() {
		return this.forename;
	}
	
	public void setForename(String forename) {
		this.forename = forename;
	}
	
	public String getLastname() {
		return this.lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}
}
