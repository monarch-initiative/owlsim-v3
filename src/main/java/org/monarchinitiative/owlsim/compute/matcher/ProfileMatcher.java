package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
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
	 */
	public MatchSet findMatchProfile(BasicQuery q);

	/**
	 * @return ontology interface
	 */
	public BMKnowledgeBase getKnowledgeBase();
}
