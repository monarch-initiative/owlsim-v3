package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Query;
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
		super();
		this.knowledgeBase = kb;
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
	 */
	public MatchSetImpl findMatchProfile(Query q) {
		
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		
		// TODO
		MatchSetImpl mp = new MatchSetImpl();
		mp.setQuery(q);
		
		// TODO: customize target set
		Set<String> indIds = knowledgeBase.getIndividualIdsInSignature();
		
		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			int numInQueryOrInTarget = queryProfileBM.orCardinality(targetProfileBM);
			double j = numInQueryAndInTarget / (double) numInQueryOrInTarget;
			pvector[n] = j;
			indArr[n] = itemId;
			n++;
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, j));
		}
		mp.sortMatches();
		return mp;
	}





}
