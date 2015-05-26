package org.monarchinitiative.owlsim.model.match;


/**
 * 
 * TODO: common superclass with {@link Match}?
 * 
 * @author cjm
 *
 */
public interface ClassMatch {

	public String getQueryClassId();
	public String getMatchClassId();
	public String getMICAClassId();

	/**
	 * higher scores translate to better matches
	 * 
	 * @return score
	 */
	public double getMICAInformationContent();


}
