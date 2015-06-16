package org.monarchinitiative.owlsim.compute.runner;

import org.monarchinitiative.owlsim.model.match.MatchSet;

public class PairwiseJob extends Job {
	
	public String queryIndividual;
	public String targetIndividual;
	public MatchSet matchSet;
	
	
	
	public String getQueryIndividual() {
		return queryIndividual;
	}



	public void setQueryIndividual(String queryIndividual) {
		this.queryIndividual = queryIndividual;
	}



	public String getTargetIndividual() {
		return targetIndividual;
	}



	public void setTargetIndividual(String targetIndividual) {
		this.targetIndividual = targetIndividual;
	}



	public MatchSet getMatchSet() {
		return matchSet;
	}



	public void setMatchSet(MatchSet matchSet) {
		this.matchSet = matchSet;
	}



	@Override
	public String toString() {
		return "Job [i=" + queryIndividual + ", j=" + targetIndividual + "]";
	}
	
	

}
