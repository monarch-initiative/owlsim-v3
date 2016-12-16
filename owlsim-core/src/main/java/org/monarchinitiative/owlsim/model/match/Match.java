package org.monarchinitiative.owlsim.model.match;


/**
 * Representation of a hit to some target
 * 
 * Note this object does not store the initial query
 * 
 * TODO: decide whether this should be generic and covert both I-I and C-C matches,
 * or whether to have a distinct class
 * 
 * @author cjm
 *
 */
public interface Match {


	/**
	 * @return id of the matched individual
	 */
	public String getMatchId();
	
	/**
     * @param matchId The matchId to set
     */
    public void setMatchId(String matchId);

	/**
	 * @return label of the matched individual
	 */
	public String getMatchLabel();

	/**
	 * @return match score, mapped from underlying representation to percentage
	 */
	public int getPercentageScore();
	
	/**
	 * higher scores translate to better matches
	 * 
	 * @return score
	 */
	public double getScore();

	/**
	 * note that identical scares will have the same rank
	 * 
	 * @return rank within owner MatchSet
	 */
	public int getRank() ;

	/**
	 * @param rank
	 */
	public void setRank(int rank);
	
	/**
	 * 
	 * @return significance (p-value)
	 */
	public double getSignificance();
	
	/**
	 * set the significance (p-value) of the match
	 * this is agnostic to the kind of significance test
	 * that is performed.
	 */
	public void setSignificance(double p);
}
