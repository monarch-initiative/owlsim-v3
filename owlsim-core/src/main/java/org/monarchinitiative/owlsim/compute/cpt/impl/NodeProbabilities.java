package org.monarchinitiative.owlsim.compute.cpt.impl;

public class NodeProbabilities {
	public final double prOn;
	public final double prOff;
	public final double prUnknown;
	public NodeProbabilities(double prOn, double prOff) {
		super();
		this.prOn = prOn;
		this.prOff = prOff;
		prUnknown = 1-(prOn+prOff);
	}
	
	public String toString() {
		return "ON: "+prOn + " OFF:" +prOff+" UNK:"+prUnknown;
	}

}
