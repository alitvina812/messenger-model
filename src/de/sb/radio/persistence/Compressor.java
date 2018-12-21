package de.sb.radio.persistence;

public class Compressor implements Processor {
	private double expansionRatio;

	
	public Compressor (final double compressionRatio) {
		this.expansionRatio = 1 / compressionRatio;
	}


	public double getExpansionRatio() {
		return expansionRatio;
	}


	public void setExpansionRatio(double expansionRatio) {
		this.expansionRatio = expansionRatio;
	}


	public void process (final double[] frame) throws NullPointerException {
		for (int channel = 0; channel < frame.length; ++channel) {
			final double sample = frame[channel];
			frame[channel] = Math.signum(sample) * (1 - Math.pow(1 - Math.abs(sample), this.expansionRatio));
		}
	}
}
