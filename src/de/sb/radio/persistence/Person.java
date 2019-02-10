package de.sb.radio.persistence;

import java.util.Collections;
import java.util.Set;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbTransient;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;


@Entity
@Table(schema = "radio", name = "Person")
@PrimaryKeyJoinColumn(name = "personIdentity")
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Person extends BaseEntity{
	static public enum Group {USER, ADMIN}
	
	static private final byte[] DEFAULT_PASSWORD_HASH = HashTools.sha256HashCode("");
	
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
	
	@Column(nullable = false, updatable = true)
	@NotNull
	@Size(min = 1, max = 31)
	private String surname;
	
	@Valid
	@Embedded
	@AttributeOverrides({
		@AttributeOverride(name="timestamp", column = @Column(name="negotiationTimestamp")),		
		@AttributeOverride(name="offer", column = @Column(name="negotiationOffer")),
		@AttributeOverride(name="answer", column = @Column(name="negotiationAnswer"))
	})
	private Negotiation negotiation;
	
	@OneToMany(mappedBy="owner", cascade = {CascadeType.REMOVE, CascadeType.REFRESH})
	private Set<Track> tracks;
	
	@ManyToOne(optional = false)
	@JoinColumn(name="avatarReference", nullable = false, updatable = true)
	private Document avatar;
	
	public Person(Document avatar) {
		this.passwordHash = DEFAULT_PASSWORD_HASH;
		this.tracks = Collections.emptySet();
		this.avatar = avatar;
		this.group = Group.USER;
	}
	
	protected Person() {
		this(null);
	}
	
	@JsonbProperty
	public String getEmail() {
		return this.email;
	}
	
	public void setEmail(String email) {
		this.email = email;
	}
	
	@JsonbTransient
	public byte[] getPasswordHash() {
		return this.passwordHash;
	}
	
	public void setPasswordHash(byte[] passwordHash) {
		this.passwordHash = passwordHash;
	}
	
	@JsonbTransient
	public Set<Track> getTracks() {
		return this.tracks;
	}
	
	@JsonbProperty
	public String getForename() {
		return this.forename;
	}
	
	public void setForename(String forename) {
		this.forename = forename;
	}
	
	@JsonbProperty
	public String getSurname() {
		return this.surname;
	}
	
	public void setSurname(String surname) {
		this.surname = surname;
	}
	
	@JsonbProperty
	public Negotiation getNegotiation() {
		return this.negotiation;
	}
	
	public void setNegotiation(Negotiation negotiation) {
		this.negotiation = negotiation;
	}
	
	@JsonbProperty
	public Group getGroup () {
		return this.group;
	}
	
	public void setGroup (final Group group) {
		this.group = group;
	}
	
	@JsonbTransient
	public Document getAvatar() {
		return avatar;
	}

	public void setAvatar(Document avatar) {
		this.avatar = avatar;
	}
	
	@JsonbProperty()
	protected long getAvatarReference() {
		return this.avatar == null ? 0 : this.avatar.getIdentity();
	}
	
	
	@JsonbProperty()
	protected long[] getTrackReferences() {
		return this.tracks.stream().mapToLong(track -> track.getIdentity()).toArray();
	}
	
}