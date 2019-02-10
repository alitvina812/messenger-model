package de.sb.radio.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;


@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Negotiation {
	
	@Column(nullable = true, updatable = true)
	private Long timestamp;
	
	@Column(nullable = true, updatable = true, length = 2046)
	private String offer;
	
	@Column(nullable = true, updatable = true, length = 2046)
	private String answer;
	
	@JsonbProperty
	public boolean isOffering() {
		return this.offer != null & this.answer == null;
	}
	
	@JsonbProperty
	public boolean isAnswering() {
		return this.offer != null & this.answer != null;
	}
	
	@JsonbProperty
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	
	@JsonbProperty
	public String getOffer() {
		return this.offer;
	}
	
	public void setOffer(String offer) {
		this.offer = offer;
	}
	
	@JsonbProperty
	public String getAnswer() {
		return this.answer;
	}
	
	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
