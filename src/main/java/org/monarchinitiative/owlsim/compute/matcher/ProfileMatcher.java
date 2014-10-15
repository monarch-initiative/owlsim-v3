package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * methods for scoring a set of candidates given a class query profile
 * 
 *
 * @author cjm
 *
 */
public interface ProfileMatcher {

	/**
	 * @param q
	 * @return scored matches
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfile(ProfileQuery q) throws UnknownFilterException;

	/**
	 * @param q
	 * @param alpha - pvalue cutoff
	 * @return scored matches
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfile(ProfileQuery q, double alpha) throws UnknownFilterException;

	
	/**
	 * @return ontology interface
	 */
	public BMKnowledgeBase getKnowledgeBase();
}
