package org.monarchinitiative.owlsim.compute.matcher.impl.cosine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.WeightedQuery;
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
public class CosineWeightedSimilarityProfileMatcher extends CosineSimilarityProfileMatcher implements NegationAwareProfileMatcher {
	
	private Logger LOG = Logger.getLogger(CosineWeightedSimilarityProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	public CosineWeightedSimilarityProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new CosineWeightedSimilarityProfileMatcher(kb);
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
		
		// TODO
		WeightedQuery wpq = (WeightedQuery)q;
		Map<String, Double> qwmap = wpq.getQueryClassWeightMap();
		final int SCALE_SQ=SCALE*SCALE;

		EWAHCompressedBitmap qp = getProfileBM(q);
		EWAHCompressedBitmap qn = getNegatedProfileBM(q);
		
		// TODO
		MatchSet mp =  MatchSetImpl.create(q);
		
		final int SCALE=1000;
		int sqrtQC = getScaledSqrt(qp.cardinality() + qn.cardinality());
		EWAHCompressedBitmap onQueryNodesBM = getProfileBM(q);
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap tp = knowledgeBase.getTypesBM(itemId);
			EWAHCompressedBitmap tn = knowledgeBase.getDirectNegatedTypesBM(itemId);

			// TODO: methods for weights for target
			Map<String, Double> twmap = new HashMap<String, Double>();
			
			
			// dot product of vector of {0,1} equivalent to cardinality of intersection
			double sumOfVectorProduct = getDotProduct(qp, qn, tp, tn, qwmap, twmap);
			int sqrtTC = getScaledSqrt(tp.cardinality() + tn.cardinality());

			// TODO
			double j = sumOfVectorProduct / (double) ((sqrtQC * sqrtTC) / SCALE_SQ);
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, j));
		}
		mp.sortMatches();
		return mp;
	}

	public double getDotProduct(EWAHCompressedBitmap qp, EWAHCompressedBitmap qn,
			EWAHCompressedBitmap tp, EWAHCompressedBitmap tn,
			Map<String, Double> qwmap,
			Map<String, Double> twmap) {
		return 
				getSumWeights(qp, tp, qwmap, twmap) +
				getSumWeights(qn, tn, qwmap, twmap) +
				(-1 * getSumWeights(qp, tn, qwmap, twmap)) +
				(-1 * getSumWeights(qn, tp, qwmap, twmap));
	}
	
	public double getSumWeights(EWAHCompressedBitmap qp, EWAHCompressedBitmap qn, 
			Map<String, Double> qwmap,
			Map<String, Double> twmap) {
		double sumWeights = 0.0;
		for (int ix : qp.and(qn)) {
			String cid = knowledgeBase.getClassId(ix);
			double qweight = qwmap.containsKey(cid) ? qwmap.get(cid) : 1.0;
			double tweight = twmap.containsKey(cid) ? twmap.get(cid) : 1.0;
			sumWeights += qweight * tweight;
		}
		return sumWeights;
	}


}
