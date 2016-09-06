package org.monarchinitiative.owlsim.compute.runner;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.MatchSet;

public class SearchJob extends Job {
	
	public String queryIndividual;
	public Set<String> queryClassIds;
    public Set<String> negatedQueryClassIds;
    public Set<String> referenceIndividualIds;
	public MatchSet matchSet;
	
	
	
	public String getQueryIndividual() {
		return queryIndividual;
	}



	public void setQueryIndividual(String queryIndividual) {
		this.queryIndividual = queryIndividual;
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



	/**
     * @return the expectedIndividuals
     */
    public Set<String> getReferenceIndividualIds() {
        return referenceIndividualIds;
    }



    /**
     * @param expectedIndividuals the expectedIndividuals to set
     */
    public void setReferenceIndividualIds(Set<String> expectedIndividuals) {
        this.referenceIndividualIds = expectedIndividuals;
    }



    @Override
	public String toString() {
		return "SearchJob [queryIndividual=" + queryIndividual
				+ ", queryClassIds=" + queryClassIds
				+ ", negatedQueryClassIds=" + negatedQueryClassIds
				+ ", matchSet=" + matchSet + "]";
	}




}
