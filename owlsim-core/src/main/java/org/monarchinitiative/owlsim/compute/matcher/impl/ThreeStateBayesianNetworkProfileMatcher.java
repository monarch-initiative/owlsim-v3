package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.cpt.impl.ThreeStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * INCOMPLETE
 * 
 * 
 * 
 * @author cjm
 *
 */
public class ThreeStateBayesianNetworkProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(ThreeStateBayesianNetworkProfileMatcher.class);

	ConditionalProbabilityIndex cpi;

	@Inject
	private ThreeStateBayesianNetworkProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
		try {
			calculateConditionalProbabilities(kb);
		} catch (IncoherentStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param kb
	 * @return new instance
	 */
	public static ThreeStateBayesianNetworkProfileMatcher create(BMKnowledgeBase kb) {
		return new ThreeStateBayesianNetworkProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "bayesian-network";
	}

	/**
	 * @param kb
	 * @throws IncoherentStateException
	 */
	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException {
		cpi = ThreeStateConditionalProbabilityIndex.create(kb);
		cpi.calculateConditionalProbabilities(kb);
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		boolean isUseNegation = q instanceof QueryWithNegation;
		if (!isUseNegation) {
			LOG.error("Consider using TwoState BN, this will be inefficient");
		}
		EWAHCompressedBitmap negatedQueryProfileBM;

		//double fpr = getFalsePositiveRate();
		//double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;
		//int numClasses = knowledgeBase.getClassIdsInSignature().size();
		//EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		//		EWAHCompressedBitmap negatedQueryProfileBM = null;
		LOG.info("Using negation*******");
		QueryWithNegation nq = (QueryWithNegation)q;
		negatedQueryProfileBM = getNegatedProfileBM(nq);
		LOG.info("nqp=" + negatedQueryProfileBM);

		Set<String> queryClassIds = q.getQueryClassIds();
		Set<String> negatedQueryClassIds = nq.getQueryClassIds();
		MatchSet mp = MatchSetImpl.create(q); // TODO

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			EWAHCompressedBitmap negatedTargetProfileBM = knowledgeBase.getNegatedTypesBM(itemId);

			LOG.debug("TARGET PROFILE for "+itemId+" "+targetProfileBM);

			Calculator calc = new Calculator(targetProfileBM, negatedTargetProfileBM);
			//double p = calculateProbability(queryClassIds, targetProfileBM);
			double p = calc.calculateProbability(queryClassIds, negatedQueryClassIds);

			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
			LOG.info("p for "+itemId+" = "+p);
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
	 * We wrap calculation within a class to allow for cacheing relative to
	 * a particular targetProfile
	 * 
	 * @author cjm
	 *
	 */
	public class Calculator {
		EWAHCompressedBitmap targetProfileBM;
		EWAHCompressedBitmap negatedTargetProfileBM;
		Double[] probCache;

		public Calculator(EWAHCompressedBitmap targetProfileBM, EWAHCompressedBitmap negatedTargetProfileBM) {
			super();
			this.targetProfileBM = targetProfileBM;
			probCache = new Double[getKnowledgeBase().getNumClassNodes()];
		}

		/**
		 * Calculate the probability of all queryClasses being on,
		 * given the nodes in the target profile are not
		 * 
		 * Note: currently this is asymmetric; ie we do not calculate
		 * the probability of the target given the query nodes are on;
		 * this has the effect of penalizing large queries; for a fixed
		 * query this is not an issue. However, it also does *not* penalize
		 * broad-spectrum targets
		 * 
		 * @param queryClassIds
		 * @param negatedQueryClassIds 
		 * @param targetProfileBM
		 * @return probability
		 */
		public double calculateProbability(Set<String> queryClassIds, 
				Set<String> negatedQueryClassIds) {
			double cump = 1.0;

			// treat set of query class Ids as a leaf node that is the
			// class intersection of all members; ie q1^...^qn
			// for a class intersection, the CPT is always such that
			//  Pr=1.0, if all parents=1
			//  Pr=0.0 otherwise
			for (String queryClassId : queryClassIds) {
				double p = calculateProbability(true, queryClassId);
				cump *= p;
			}
			for (String negatedQueryClassId : negatedQueryClassIds) {
				double p = calculateProbability(false, negatedQueryClassId);
				cump *= p;
			}
			return cump;
		}

		/**
		 * probability of queryClass being true, given all targets are on
		 * 
		 * @param queryClassId
		 * @param targetProfileBM
		 * @return probability
		 */
		private double calculateProbability(boolean isOn, String queryClassId) {
			BMKnowledgeBase kb = getKnowledgeBase();
			int qcix = kb.getClassIndex(queryClassId);
			return calculateProbability(isOn, qcix);
		}

		private double calculateProbability(boolean isOn, int qcix) {
			if (probCache[qcix] != null) {
				LOG.debug("Using cached for "+qcix);
				return probCache[qcix];
			}

			BMKnowledgeBase kb = getKnowledgeBase();
			LOG.debug("Calculating probability for "+qcix+" ie "+kb.getClassId(qcix));

			double returnProb;
			// TODO - determine efficiency of using get(ix) vs other methods
			if (targetProfileBM.get(qcix)) {
				LOG.debug("Q is in target profile");
				returnProb = 0.95; // TODO - do not hardcode
			}
			else {
				List<Integer> pixs = kb.getDirectSuperClassesBM(qcix).getPositions();
				double[] parentProbs = new double[pixs.size()];
				LOG.debug("calculating for parents");
				for (int i=0; i<pixs.size(); i++) {
					// TODO - cache, to avoid repeated calculations
					// recursive call
					parentProbs[i] = 
							calculateProbability(isOn, pixs.get(i));
				}

				int numParents = pixs.size();
				// assume two states for now: will be extendable to yes, no, unknown
				int numStates = (int) Math.pow(2, numParents);

				// sum of probabilities
				double sump = 0; // TODO: use logs

				// Pr(Q | Parents) = sum of { Pr(Q | off, off, ..., off), ... } 
				for (int parentState=0; parentState<numStates; parentState++) {
					double cp = cpi.getConditionalProbability(qcix, parentState);
					LOG.debug(" cp="+cp+" for states="+parentState);
					Map<Integer, Character> psm = cpi.getParentsToStateMapping(qcix, parentState);
					Set<Integer> onPixs = new HashSet<Integer>();
					for (int pix : psm.keySet()) {
						if (psm.get(pix) == 't') {
							onPixs.add(pix);
						}
					}
					LOG.debug("   onPixs="+onPixs);
					double p = 1.0;
					for (int i=0; i<pixs.size(); i++) {
						int pix = pixs.get(i);
						p *= onPixs.contains(pix) ? parentProbs[i] : 1-parentProbs[i];
					}
					sump += p * cp;
				}
				LOG.debug("Calculated probability for "+qcix+" ie "+kb.getClassId(qcix)+" = "+sump);

				returnProb = sump;
			}
			probCache[qcix] = returnProb;
			return returnProb;
		}


	}


}
