package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

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
	public GridNegatedProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	



	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(BasicQuery q) {
		return null;
	}


}
