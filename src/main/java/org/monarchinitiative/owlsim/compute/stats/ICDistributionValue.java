package org.monarchinitiative.owlsim.compute.stats;

/**
 * This is a proxy for a set of <x,y> coordinates,
 * used to return IC distributions.
 */
public class ICDistributionValue {

	private double x;
	private double y;
	
	public ICDistributionValue(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
}
