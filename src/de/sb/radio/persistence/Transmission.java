package de.sb.radio.persistence;

import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.annotation.JsonbVisibility;
import javax.persistence.Column;
import javax.persistence.Embeddable;

import de.sb.toolbox.bind.JsonProtectedPropertyStrategy;


@Embeddable
@JsonbVisibility(JsonProtectedPropertyStrategy.class)
public class Transmission {

	@Column(nullable = true, updatable = true)
	private String address;
	
	@Column(nullable = true, updatable = true)
	private Long timestamp;
	
	@Column(nullable = true, updatable = true, length = 4096)
	private String offer;
	
	@Column(nullable = true, updatable = true, length = 4096)
	private String answer;
	
	
	@JsonbProperty()
	public Long getTimestamp() {
		return this.timestamp;
	}
	
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}

	
	@JsonbProperty()
	public String getAddress() {
		return this.address;
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	@JsonbProperty()
	public String getOffer() {
		return this.offer;
	}
	
	public void setOffer(String offer) {
		this.offer = offer;
	}
	
	@JsonbProperty()
	public String getAnswer() {
		return this.answer;
	}
	
	public void setAnswer(String answer) {
		this.answer = answer;
	}
}
