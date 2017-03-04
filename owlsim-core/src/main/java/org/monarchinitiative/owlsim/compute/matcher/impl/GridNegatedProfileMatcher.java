package org.monarchinitiative.owlsim.compute.matcher.impl;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
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
public class GridNegatedProfileMatcher extends AbstractSemanticSimilarityProfileMatcher implements NegationAwareProfileMatcher {
	
	private Logger LOG = Logger.getLogger(GridNegatedProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	public GridNegatedProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	@Override
	public String getShortName() {
		return "grid-negated";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		return null;
	}

}
