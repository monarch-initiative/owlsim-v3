package org.monarchinitiative.owlsim.compute.matcher.impl.cosine;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.AbstractProfileMatcher;
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
public class CosineSimilarityProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(CosineSimilarityProfileMatcher.class);
	protected final int SCALE=1000;

	
	/**
	 * @param kb
	 */
	@Inject
	public CosineSimilarityProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new CosineSimilarityProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "cosine";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 * @throws UnknownFilterException 
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) throws UnknownFilterException {
		
		final int SCALE_SQ=SCALE*SCALE;
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		
		// TODO
		MatchSet mp =  MatchSetImpl.create(q);
		
		int sqrtQC = getScaledSqrt(queryProfileBM.cardinality());
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			
			//LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			
			// dot product of vector of {0,1} equivalent to cardinality of intersection
			int sumOfVectorProduct = queryProfileBM.andCardinality(targetProfileBM);
			int sqrtTC = getScaledSqrt(targetProfileBM.cardinality());

			double j = sumOfVectorProduct / (double) ((sqrtQC * sqrtTC) / SCALE_SQ);
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, j));
		}
		mp.sortMatches();
		return mp;
	}

	public int getScaledSqrt(int n) {
		return  (int) (Math.sqrt(n) * SCALE);
	}



}
