package org.monarchinitiative.owlsim.compute.matcher.impl;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * As GridProfileMatcher, allows negated queries
 * 
 * TODO: this is INCOMPLETE
 * 
 * @author cjm
 *
 */
public class GridNegatedProfileMatcher extends AbstractSemanticSimilarityProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(GridNegatedProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	@Inject
	public GridNegatedProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	



	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		return null;
	}


}
