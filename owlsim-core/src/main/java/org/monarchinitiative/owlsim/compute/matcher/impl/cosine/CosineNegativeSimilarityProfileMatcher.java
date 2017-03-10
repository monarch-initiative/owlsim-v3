package org.monarchinitiative.owlsim.compute.matcher.impl.cosine;

import java.util.List;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a semantic similarity
 * 
 * @author cjm
 *
 */
// TODO
public class CosineNegativeSimilarityProfileMatcher extends CosineSimilarityProfileMatcher implements NegationAwareProfileMatcher {
	
	private Logger LOG = Logger.getLogger(CosineNegativeSimilarityProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	public CosineNegativeSimilarityProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new CosineNegativeSimilarityProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "cosine-negative";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) throws UnknownFilterException {
		
        final int SCALE=1000;
		final int SCALE_SQ=SCALE*SCALE;

		EWAHCompressedBitmap qp = getProfileBM(q);
		EWAHCompressedBitmap qn = getNegatedProfileBM(q);
		
		// TODO
		MatchSet mp =  MatchSetImpl.create(q);
		
		int sqrtQC = getScaledSqrt(qp.cardinality() + qn.cardinality());
		EWAHCompressedBitmap onQueryNodesBM = getProfileBM(q);

		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap tp = knowledgeBase.getTypesBM(itemId);
			EWAHCompressedBitmap tn = knowledgeBase.getDirectNegatedTypesBM(itemId);
			
			
			// dot product of vector of {0,1} equivalent to cardinality of intersection
			int sumOfVectorProduct = getDotProduct(qp, qn, tp, tn);
			int sqrtTC = getScaledSqrt(tp.cardinality() + tn.cardinality());

			double j = sumOfVectorProduct / (double) ((sqrtQC * sqrtTC) / SCALE_SQ);
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, j));
		}
		mp.sortMatches();
		return mp;
	}

	public int getDotProduct(EWAHCompressedBitmap qp, EWAHCompressedBitmap qn,
			EWAHCompressedBitmap tp, EWAHCompressedBitmap tn) {
		return 
				qp.andCardinality(tp) +
				qn.andCardinality(tn) +
				(-1 * qp.andCardinality(tn)) +
				(-1 * qn.andCardinality(tp));
	}
	
	


}
