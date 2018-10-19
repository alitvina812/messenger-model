package de.sb.radio.persistence;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Name {
	
	@Column(nullable = false)
	private String forename;
	@Column(name = "surname", nullable = false)
	private String lastname;
	
	public Name() {
		this.forename = "Indiana";
		this.lastname = "Jones";
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
