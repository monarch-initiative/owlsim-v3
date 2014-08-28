package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a semantic similarity
 * 
 * @author cjm
 *
 */
public class JaccardSimilarityProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(JaccardSimilarityProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	public JaccardSimilarityProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new JaccardSimilarityProfileMatcher(kb);
	}

	

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfileImpl(BasicQuery q) throws UnknownFilterException {
		
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		
		// TODO
		MatchSet mp =  MatchSetImpl.create(q);
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			int numInQueryOrInTarget = queryProfileBM.orCardinality(targetProfileBM);
			double j = numInQueryAndInTarget / (double) numInQueryOrInTarget;
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, j));
		}
		mp.sortMatches();
		return mp;
	}





}
