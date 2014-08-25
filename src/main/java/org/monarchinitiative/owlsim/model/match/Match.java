package org.monarchinitiative.owlsim.model.match;

/**
 * Representation of a match between a Query and some individual (aka item, element, label)
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

}
