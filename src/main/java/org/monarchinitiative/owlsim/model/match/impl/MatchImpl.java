package org.monarchinitiative.owlsim.model.match.impl;

import org.monarchinitiative.owlsim.model.match.ExecutionMetadata;
import org.monarchinitiative.owlsim.model.match.Match;

/**
 * A match between a query profile and an individual
 * 
 * @author cjm
 *
 */
public class MatchImpl implements Match {
	
	String matchId;
	String matchLabel;
	double probability;
	
	/**
	 * @param matchId
	 * @param matchLabel
	 * @param probability
	 */
	public MatchImpl(String matchId, String matchLabel, double probability) {
		super();
		this.matchId = matchId;
		this.matchLabel = matchLabel;
		this.probability = probability;
	}
	
	public static Match create(String matchId, String matchLabel, double probability) {
		return new MatchImpl(matchId, matchLabel, probability);
	}

	public String getMatchId() {
		return matchId;
	}

	public void setMatchId(String matchId) {
		this.matchId = matchId;
	}

	public String getMatchLabel() {
		return matchLabel;
	}

	public void setMatchLabel(String matchLabel) {
		this.matchLabel = matchLabel;
	}

	public double getProbability() {
		return probability;
	}

	public void setProbability(double probability) {
		this.probability = probability;
	}
	
	public int getPercentageScore() {
		return (int) (Math.round(probability * 100));
	}
	
	public double getScore() {
		return probability * 100;
	}
	


	// TODO - move to abstract
	@Override
	public String toString() {
		return matchId + " \"" + matchLabel+ "\" " + getPercentageScore()+" p="+probability;
	}
}
