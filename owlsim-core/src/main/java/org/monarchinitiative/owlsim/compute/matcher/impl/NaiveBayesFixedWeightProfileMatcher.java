package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Calculate likelihood of query 'mutating' into target, assuming each
 * node in the ontology is independent (after pre-computing ancestor nodes
 * for query and target), using chain rule
 * 
 * p(C1=c1) * p(C2=c2) * ... p(Cn=cn)
 * 
 * Where p(Ci=ci) takes on one of 4 possibilities, depending on state
 * of query and state of target, corresponding to probability of misclassification.
 * 
 * 
 * 
 * 
 * 
 * @author cjm
 *
 */
public class NaiveBayesFixedWeightProfileMatcher extends AbstractProfileMatcher implements NegationAwareProfileMatcher {

	private Logger LOG = Logger.getLogger(NaiveBayesFixedWeightProfileMatcher.class);

	@Deprecated
	private double defaultFalsePositiveRate = 0.002; // alpha

	@Deprecated
	private double defaultFalseNegativeRate = 0.10; // beta

	// TODO - replace when tetsing is over
	//private double[] defaultFalsePositiveRateArr =  new double[]{0.002};
	//private double[] defaultFalseNegativeRateArr = new double[] {0.10};
	private double[] defaultFalsePositiveRateArr =  new double[]{1e-10,0.0005,0.001,0.005,0.01};
	private double[] defaultFalseNegativeRateArr = new double[] {1e-10,0.005,0.01,0.05,0.1,0.2,0.4,0.8,0.9};

	@Inject
	private NaiveBayesFixedWeightProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}

	/**
	 * @param kb
	 * @return new instance
	 */
	public static NaiveBayesFixedWeightProfileMatcher create(BMKnowledgeBase kb) {
		return new NaiveBayesFixedWeightProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "bayes-fixed";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		boolean isUseNegation = q instanceof QueryWithNegation;

		//double fpr = getFalsePositiveRate();
		//double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;
		int numClasses = knowledgeBase.getClassIdsInSignature().size();
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap negatedQueryProfileBM = null;
		if (isUseNegation) {
			LOG.info("Using negation*******");
			QueryWithNegation nq = (QueryWithNegation)q;
			negatedQueryProfileBM = getNegatedProfileBM(nq);
			LOG.info("nqp=" + negatedQueryProfileBM);
		}

		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			LOG.debug("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			int numInQueryAndNOTInTarget = queryProfileBM.andNotCardinality(targetProfileBM);
			int numNOTInQueryAndInTarget = targetProfileBM.andNotCardinality(queryProfileBM);
			int numNOTInQueryAndNOTInTarget = 
					numClasses - (numInQueryAndInTarget + numInQueryAndNOTInTarget + numNOTInQueryAndInTarget);
			int numNegatedInQueryAndInTarget = 0;
			int numInQueryAndNegatedInTarget = 0;
			int numNegatedInQueryAndNegatedInTarget = 0;
			if (isUseNegation) {
				// TODO. Investigate efficiency for storing all descendants of
				// a negated class as a bitmap
				EWAHCompressedBitmap negatedTargetProfileBM = knowledgeBase.getNegatedTypesBM(itemId);
				numNegatedInQueryAndInTarget = negatedQueryProfileBM.andCardinality(targetProfileBM);
				numInQueryAndNegatedInTarget = queryProfileBM.andCardinality(negatedTargetProfileBM);
				numNegatedInQueryAndNegatedInTarget = negatedQueryProfileBM.andCardinality(negatedTargetProfileBM);
				// TODO - check for inconsistency (Q cannot be positive and negated)

				// if negation mode is on, then we treat NOTInX as being UnknownInX,
				// thus with 7 mutually exclusive states
				numNOTInQueryAndInTarget -= numNegatedInQueryAndInTarget;
				numInQueryAndNOTInTarget -= numInQueryAndNegatedInTarget;
				numNOTInQueryAndNOTInTarget -= numNegatedInQueryAndNegatedInTarget;
			}
			double p = 0.0;
			// integrate over a Dirichlet prior for alpha & beta, rather than gridsearch
			// this can be done closed-form
			for (double fnr : defaultFalseNegativeRateArr) {
				for (double fpr : defaultFalsePositiveRateArr) {

					double pQ1T1 = Math.pow(1-fnr,  numInQueryAndInTarget);
					double pQ0T1 = Math.pow(fnr,  numNOTInQueryAndInTarget);
					double pQ1T0 = Math.pow(fpr,  numInQueryAndNOTInTarget);
					double pQ0T0 = Math.pow(1-fpr,  numNOTInQueryAndNOTInTarget);

					if (isUseNegation) {
						LOG.debug("ITEM="+itemId);
						LOG.debug("pQ1T1 = "+(1-fnr)+" ^ "+ numInQueryAndInTarget+" = "+pQ1T1);
						LOG.debug("pQ0T1 = "+(fnr)+" ^ "+ numNOTInQueryAndInTarget+" = "+pQ0T1);
						LOG.debug("pQ1T0 = "+(fpr)+" ^ "+ numInQueryAndNOTInTarget+" = "+pQ1T0);
						LOG.debug("pQ0T0 = "+(1-fpr)+" ^ "+ numNOTInQueryAndNOTInTarget+" = "+pQ0T0);

						// TODO - do not hardcode negation factor
						// for now we assume false negatives and positives are considerably
						// less likely, but above zero
						// TODO - works less well when we are summing over alphas and betas....
						double pQNT1 = Math.pow((fnr*fnr)/500,  numNegatedInQueryAndInTarget);
						double pQ1TN = Math.pow((fpr*fpr)/500,  numInQueryAndNegatedInTarget);
						//double pQNT1 = Math.pow(0,  numNegatedInQueryAndInTarget);
						//double pQ1TN = Math.pow(0,  numInQueryAndNegatedInTarget);
						double pQNTN = Math.pow(1-(fpr/50),  numNegatedInQueryAndNegatedInTarget);

						LOG.debug("pQNT1 = "+(fnr)+"/10 ^ "+ numNegatedInQueryAndInTarget+" = "+pQNT1);
						LOG.debug("pQ1TN = "+(fpr)+"/10 ^ "+ numInQueryAndNegatedInTarget+" = "+pQ1TN);
						LOG.debug("pQNTN = 1-"+(fpr)+"/10 ^ "+ numNegatedInQueryAndNegatedInTarget+" = "+pQNTN);

						p += 
								Math.exp(Math.log(pQ1T1) + Math.log(pQ0T1) + Math.log(pQ1T0) + Math.log(pQ0T0) +
										Math.log(pQNT1) +Math.log(pQ1TN) + Math.log(pQNTN));

					}
					else {

						//LOG.debug("pQ1T1 = "+(1-fnr)+" ^ "+ numInQueryAndInTarget+" = "+pQ1T1);
						//LOG.debug("pQ0T1 = "+(fnr)+" ^ "+ numNOTInQueryAndInTarget+" = "+pQ0T1);
						//LOG.debug("pQ1T0 = "+(fpr)+" ^ "+ numInQueryAndNOTInTarget+" = "+pQ1T0);
						//LOG.debug("pQ0T0 = "+(1-fpr)+" ^ "+ numNOTInQueryAndNOTInTarget+" = "+pQ0T0);
						//TODO: optimization
						p += 
								Math.exp(Math.log(pQ1T1) + Math.log(pQ0T1) + Math.log(pQ1T0) + Math.log(pQ0T0));
					}
				}
			}
			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
			LOG.debug("p for "+itemId+" = "+p);
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
	@Deprecated
	public double getFalsePositiveRate() {
		return defaultFalsePositiveRate;		
	}

	/**
	 * @return probability absence of a query class is a false negative
	 */
	@Deprecated
	public double getFalseNegativeRate() {
		return defaultFalseNegativeRate;		
	}




}
