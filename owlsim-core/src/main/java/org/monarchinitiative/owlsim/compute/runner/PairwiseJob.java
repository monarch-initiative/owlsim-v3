package org.monarchinitiative.owlsim.compute.runner;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.MatchSet;

public class PairwiseJob extends Job {
	
	public String queryIndividual;
	public String targetIndividual;
	public Set<String> queryClassIds;
	public Set<String> negatedQueryClassIds;
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

	public Set<String> getQueryClassIds() {
		return queryClassIds;
	}



	public void setQueryClassIds(Set<String> queryClassIds) {
		this.queryClassIds = queryClassIds;
	}



	public Set<String> getNegatedQueryClassIds() {
		return negatedQueryClassIds;
	}



	public void setNegatedQueryClassIds(Set<String> negatedQueryClassIds) {
		this.negatedQueryClassIds = negatedQueryClassIds;
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
