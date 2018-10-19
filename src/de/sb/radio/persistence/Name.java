package de.sb.radio.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Embeddable
public class Name {
	
	@Column(nullable = false)
	@NotNull
	@Size(min = 1, max = 31)
	private String forename;
	@Column(name = "surname", nullable = false)
	@NotNull
	@Size(min = 1, max = 31)
	private String lastname;
	
	public Name() {
	}
	
	public Name(String forename, String lastname) {
		this.forename = forename;
		this.lastname = lastname;
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
}
