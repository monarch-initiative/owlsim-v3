package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
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
public class NaiveBayesFixedWeightTwoStateProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(NaiveBayesFixedWeightTwoStateProfileMatcher.class);

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
	private NaiveBayesFixedWeightTwoStateProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}

	/**
	 * @param kb
	 * @return new instance
	 */
	public static NaiveBayesFixedWeightTwoStateProfileMatcher create(BMKnowledgeBase kb) {
		return new NaiveBayesFixedWeightTwoStateProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "naive-bayes-fixed-weight-two-state";
	}

	private EWAHCompressedBitmap getQueryBlanketBM(ProfileQuery q) {
		EWAHCompressedBitmap onQueryNodesBM = getProfileBM(q);
		Set<Integer> nodesWithOnParents = new HashSet<Integer>();

		// there may be more efficient ways of doing this, but this is
		// called once at the start of the search...
		for (String cid : knowledgeBase.getClassIdsInSignature()) {
			int cix = knowledgeBase.getClassIndex(cid);
			EWAHCompressedBitmap supsBM = knowledgeBase.getDirectSuperClassesBM(cid);
			int nParents = supsBM.cardinality();
			if (supsBM.andCardinality(onQueryNodesBM) == nParents) {
				nodesWithOnParents.add(cix);
			}
		}

		return onQueryNodesBM.or(EWAHUtils.converIndexSetToBitmap(nodesWithOnParents));
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		//double fpr = getFalsePositiveRate();
		//double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;

		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap queryBlanketProfileBM = getQueryBlanketBM(q);
		LOG.info("|OnQueryNodes|="+queryProfileBM.cardinality());
		LOG.info("|QueryNodesWithOnParents|="+queryBlanketProfileBM.cardinality());

		//int numClassesConsidered = knowledgeBase.getClassIdsInSignature().size();
		int numClassesConsidered = queryBlanketProfileBM.cardinality();

		EWAHCompressedBitmap negatedQueryProfileBM = null;

		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			// any node which has an off query parent is discounted
			targetProfileBM = targetProfileBM.and(queryBlanketProfileBM);
			LOG.debug("TARGET PROFILE for "+itemId+" "+targetProfileBM);


			// two state model.
			int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
			int numInQueryAndNOTInTarget = queryProfileBM.andNotCardinality(targetProfileBM);
			int numNOTInQueryAndInTarget = targetProfileBM.andNotCardinality(queryProfileBM);
			int numNOTInQueryAndNOTInTarget = 
					numClassesConsidered - (numInQueryAndInTarget + numInQueryAndNOTInTarget + numNOTInQueryAndInTarget);

			double p = 0.0;
			// integrate over a Dirichlet prior for alpha & beta, rather than gridsearch
			// this can be done closed-form
			for (double fnr : defaultFalseNegativeRateArr) {
				for (double fpr : defaultFalsePositiveRateArr) {

					double pQ1T1 = Math.pow(1-fnr,  numInQueryAndInTarget);
					double pQ0T1 = Math.pow(fnr,  numNOTInQueryAndInTarget);
					double pQ1T0 = Math.pow(fpr,  numInQueryAndNOTInTarget);
					double pQ0T0 = Math.pow(1-fpr,  numNOTInQueryAndNOTInTarget);



					//LOG.debug("pQ1T1 = "+(1-fnr)+" ^ "+ numInQueryAndInTarget+" = "+pQ1T1);
					//LOG.debug("pQ0T1 = "+(fnr)+" ^ "+ numNOTInQueryAndInTarget+" = "+pQ0T1);
					//LOG.debug("pQ1T0 = "+(fpr)+" ^ "+ numInQueryAndNOTInTarget+" = "+pQ1T0);
					//LOG.debug("pQ0T0 = "+(1-fpr)+" ^ "+ numNOTInQueryAndNOTInTarget+" = "+pQ0T0);
					//TODO: optimization
					p += 
							Math.exp(Math.log(pQ1T1) + Math.log(pQ0T1) + Math.log(pQ1T0) + Math.log(pQ0T0));

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
