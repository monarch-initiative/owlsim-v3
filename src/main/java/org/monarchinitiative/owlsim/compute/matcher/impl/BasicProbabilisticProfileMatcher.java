package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.Query;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a probability of being the match
 * 
 * @author cjm
 *
 */
public class BasicProbabilisticProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(BasicProbabilisticProfileMatcher.class);

	private double defaultFalsePositiveRate = 0.01;
	private double defaultFalseNegativeRate = 0.10;
	
	private BasicProbabilisticProfileMatcher(BMKnowledgeBase kb) {
		super();
		this.knowledgeBase = kb;
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static BasicProbabilisticProfileMatcher create(BMKnowledgeBase kb) {
		return new BasicProbabilisticProfileMatcher(kb);
	}
	

	

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfile(Query q) {
		
		double fpr = getFalsePositiveRate();
		double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;
		int numClasses = knowledgeBase.getClassIdsInSignature().size();
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		
		MatchSet mp = MatchSetImpl.create(q);
		
		// TODO: customize target set
		Set<String> indIds = knowledgeBase.getIndividualIdsInSignature();
		
		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			int numInQueryAndNOTInTarget = queryProfileBM.andNotCardinality(targetProfileBM);
			int numNOTInQueryAndInTarget = targetProfileBM.andNotCardinality(queryProfileBM);
			int numNOTInQueryAndNOTInTarget = 
					numClasses - (numInQueryAndInTarget + numInQueryAndNOTInTarget + numNOTInQueryAndInTarget);
			// TODO: per-class fale +/- values
			double pQ1T1 = Math.pow(1-fnr,  numInQueryAndInTarget);
			double pQ0T1 = Math.pow(fnr,  numNOTInQueryAndInTarget);
			double pQ1T0 = Math.pow(fpr,  numInQueryAndNOTInTarget);
			double pQ0T0 = Math.pow(1-fpr,  numNOTInQueryAndNOTInTarget);
			LOG.info("pQ1T1 = "+(1-fnr)+" ^ "+ numInQueryAndInTarget+" = "+pQ1T1);
			LOG.info("pQ0T1 = "+(fnr)+" ^ "+ numNOTInQueryAndInTarget+" = "+pQ0T1);
			LOG.info("pQ1T0 = "+(fpr)+" ^ "+ numInQueryAndNOTInTarget+" = "+pQ1T0);
			LOG.info("pQ0T0 = "+(1-fpr)+" ^ "+ numNOTInQueryAndNOTInTarget+" = "+pQ0T0);
			//TODO: optimization
			double p = 
					Math.exp(Math.log(pQ1T1) + Math.log(pQ0T1) + Math.log(pQ1T0) + Math.log(pQ0T0));
			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
		}
		for (n = 0; n<pvector.length; n++) {
			double p = pvector[n] / sumOfProbs;
			String id = indArr[n];
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(id);
			mp.add(createMatch(id, label, p));
		}
		mp.sortMatches();
		return mp;
	}
	
	/**
	 * @return probability a query class is a false positive
	 */
	public double getFalsePositiveRate() {
		return defaultFalsePositiveRate;		
	}
	
	/**
	 * @return probability absence of a query class is a false negative
	 */
	public double getFalseNegativeRate() {
		return defaultFalseNegativeRate;		
	}



}
