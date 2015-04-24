package org.monarchinitiative.owlsim.model.match.impl;

import org.monarchinitiative.owlsim.model.match.Match;

/**
 * A match between a query profile and an individual
 * 
 * @author cjm
 *
 */
public class MatchImpl implements Match {
	
	private String matchId;
	private String matchLabel;
	private double rawScore;
	private Integer rank;
	private Double p;
	
	/**
	 * @param matchId
	 * @param matchLabel
	 * @param probability
	 */
	public MatchImpl(String matchId, String matchLabel, double probability) {
		super();
		this.matchId = matchId;
		this.matchLabel = matchLabel;
		this.rawScore = probability;
		rank = null;
		p = Double.NaN;
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

	public double getRawScore() {
		return rawScore;
	}

	public void setRawScore(double probability) {
		this.rawScore = probability;
	}
	
	public int getPercentageScore() {
		return (int) (Math.round(rawScore * 100));
	}
	
	public double getScore() {
		return rawScore * 100;
	}

	public int getRank() {
		return rank == null ? -1 :  rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	// TODO - move to abstract
	@Override
	public String toString() {
		return matchId + " \"" + matchLabel+ "\" Rank:" + getRank()+ " %sc="+getPercentageScore()+" rawScore="+rawScore+" p="+getSignificance();
	}

	public double getSignificance() {
		return p;
	}

	public void setSignificance(double p) {
		this.p = p;
	}
	
}
