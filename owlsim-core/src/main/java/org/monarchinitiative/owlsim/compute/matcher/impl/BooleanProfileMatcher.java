package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Implements a standard boolean query
 * 
 * @author cjm
 *
 */
public class BooleanProfileMatcher extends AbstractProfileMatcher implements NegationAwareProfileMatcher {
	
	private Logger LOG = Logger.getLogger(BooleanProfileMatcher.class);
	
	/**
	 * @param kb
	 */
	@Inject
	public BooleanProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new BooleanProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "boolean";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) throws UnknownFilterException {
		
		EWAHCompressedBitmap queryProfileBM = getDirectProfileBM(q);
        boolean hasNegationQuery = false;
        EWAHCompressedBitmap negatedQueryProfileBM = null;
        if (q instanceof QueryWithNegation) {
            negatedQueryProfileBM = getDirectNegatedProfileBM((QueryWithNegation) q);
            hasNegationQuery = negatedQueryProfileBM.cardinality() > 0;
        }

		// TODO
		MatchSet mp =  MatchSetImpl.create(q);
		int qcard = queryProfileBM.cardinality();
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			if (numInQueryAndInTarget == qcard) {
			    if (!hasNegationQuery ||
			            negatedQueryProfileBM.andCardinality(targetProfileBM) == 0) {
		            String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
		            mp.add(createMatch(itemId, label, 1));
			        
			    }
			}
		}
		mp.sortMatches();
		return mp;
	}





}
