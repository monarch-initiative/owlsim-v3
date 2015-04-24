package org.monarchinitiative.owlsim.model.match.impl;

import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.model.match.Match;

/**
 * A match between a query profile and an individual,
 * in which each class of the query profile is matched
 * 
 * @author cjm
 *
 */
public class GridMatchImpl implements Match {
	
	String matchId;
	String matchLabel;
	double score;
	ClassInformationContentPair[] queryMatchArray;
	int rank;
	Double p;
	
	/**
	 * @param matchId
	 * @param matchLabel
	 * @param probability
	 * @param qmatchArr 
	 */
	public GridMatchImpl(String matchId, String matchLabel, double probability, ClassInformationContentPair[] queryMatchArray) {
		super();
		this.matchId = matchId;
		this.matchLabel = matchLabel;
		this.score = probability;
		this.queryMatchArray = queryMatchArray;
		this.p = Double.NaN;
	}
	
	public static Match create(String matchId, String matchLabel, double probability, ClassInformationContentPair[] qmatchArr) {
		return new GridMatchImpl(matchId, matchLabel, probability, qmatchArr);
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

	public double getScore() {
		return score;
	}

	public void setScore(double probability) {
		this.score = probability;
	}
	
	public int getPercentageScore() {
		return (int) (Math.round(score * 100));
	}


	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	
	// TODO - move to abstract
	@Override
	public String toString() {
		return matchId + " \"" + matchLabel+ "\" Rank="+getRank() + " sc=" + getPercentageScore()+" score="+score+" p="+getSignificance();
	}
	
	public double getSignificance() {
		return p;
	}
	
	public void setSignificance(double p) {
		this.p = p;
	}
	
}
