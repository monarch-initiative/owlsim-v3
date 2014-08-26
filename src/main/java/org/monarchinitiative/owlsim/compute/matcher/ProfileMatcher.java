package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;

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
	public MatchSet findMatchProfile(BasicQuery q) throws UnknownFilterException;

	/**
	 * @return ontology interface
	 */
	public BMKnowledgeBase getKnowledgeBase();
}
