package de.sb.radio.persistence;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import de.sb.radio.persistence.Person.Group;

import static javax.persistence.InheritanceType.JOINED;

import java.util.Collections;
import java.util.Set;


@Entity
@Table(schema = "radio", name = "Person")
@Inheritance(strategy = JOINED)
public class Person extends BaseEntity{
	static enum Group {USER, ADMIN;}
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 128)
	@Email
	private String email;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 32, max = 32)
	private byte[] passwordHash;
	
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "groupAlias", nullable = false, updatable = true)
	private Group group;
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 31)
	@Pattern(regexp = "[a-zA-Z]+")
	private String forename;
	
	@Column(name = "surname", nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 31)
	@Pattern(regexp = "[a-zA-Z]+")
	private String lastname;
	
	@OneToMany(mappedBy="owner", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	@ManyToOne(optional = false)
	@JoinColumn(name="avatarReference", nullable = false, updatable = true)
	private Document avatar;
	
	public Person(Document avatar) {
		this.passwordHash = HashTools.sha256HashCode("");
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
	
	public void setPasswordHash(String password) {
		this.passwordHash = HashTools.sha256HashCode(password);
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

	public Group getGroup () {
		return this.group;
	}
	
	public void setGroup (final Group group) {
		this.group = group;
	}
	
	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}
}
