package de.sb.radio.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Inheritance;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;

import static javax.persistence.InheritanceType.JOINED;

import java.util.Collections;
import java.util.Set;


@Entity
@Table(schema = "radio", name = "Person")
@PrimaryKeyJoinColumn(name = "personIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Person extends BaseEntity{
	public static enum Group {USER, ADMIN;}
	
	@Column(nullable = false, updatable = true, unique = true)
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
	private String forename;
	
	@Column(name = "surname", nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 31)
	private String lastname;
	
	@OneToMany(mappedBy="owner", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	@ManyToOne(optional = false)
	@JoinColumn(name="avatarReference", nullable = false, updatable = true)
	private Document avatar;
	
	@Column(nullable = false, updatable = false, insertable = true)
	private long avatarReference;
	
	@Column(nullable = false, updatable = false, insertable = true)
	private long[] trackReferences;
	
	public Person(Document avatar) {
		this.passwordHash = HashTools.sha256HashCode("");
		this.tracks = Collections.emptySet();
		this.avatar = avatar;
		this.group = Group.USER;
	}
	
	protected Person() {
		this(null);
	}
	
	@JsonbProperty("person-email")
	@JsonbTransient
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@JsonbProperty
	@JsonbTransient
	public byte[] getPasswordHash() {
		return this.passwordHash;
	}
	
	public void setPasswordHash(String password) {
		this.passwordHash = HashTools.sha256HashCode(password);
	}
	
	@JsonbProperty("person-tracklist")
	@JsonbTransient
	public Set<Track> getTracks() {
		return this.tracks;
	}
	
	@JsonbProperty("person-forename")
	@JsonbTransient
	public String getForename() {
		return this.forename;
	}
	
	public void setForename(String forename) {
		this.forename = forename;
	}
	
	@JsonbProperty("person-lastname")
	@JsonbTransient
	public String getLastname() {
		return this.lastname;
	}
	
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}

	@JsonbProperty
	@JsonbTransient
	public Group getGroup () {
		return this.group;
	}
	
	public void setGroup (final Group group) {
		this.group = group;
	}
	
	@JsonbProperty("avatar")
	@JsonbTransient
	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbProperty()
	@JsonbTransient
	protected long getAvatarReference() {
		return this.avatarReference;
	}
	
	protected void setAvatarReference (final long avatarReference) {
		this.avatarReference = avatarReference;
	}
	
	@JsonbProperty()
	@JsonbTransient
	protected long[] getTrackReferences() {
		return this.trackReferences;
	}
	
	protected void setRecordingReference (final long[] trackReferences) {
		this.trackReferences = trackReferences;
	}
}
