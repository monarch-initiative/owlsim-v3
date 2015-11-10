package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Calculate probability of observing query (e.g. patient profile) given target as evidence.
 * 
 * Note this first implementation does not use NOTs; it uses a {@link TwoStateConditionalProbabilityIndex}.
 * The two states are ON (true/observed) and OFF (unknown/not observed)
 * - note the open world assumptions: that the off state means there is no
 * information about the truth of the node, it does not mean the node is false.
 * 
 * Probabilities propagate TO a child FROM its parents. 
 * 
 * The probability of a child node being on C=on is dependent on the state of its
 * parents:
 * 
 * 
 * Pr( C=on | P1=S1, ..., Pn=Sn) =
 * Pr( C=on | S1, ..., Sn) =             <-- syntactic sugar
 *   Pr( C=on | on,on,...,on ) * P(on,on,...,on) +
 *   Pr( C=on | off,on,...,on ) * P(off,on,...,on) +
 *   ...
 *   Pr( C=on | off,off,...,off ) * P(off,off,...,off) 
 * 
 * For any given query Q=Q1,...Qm, we assume independent probabilities
 * and calculate Pr(Q) = Pq(Q1=on,...,Qm=on)
 * 
 * <h2>Negation</h2>
 * 
 * Each node can only have two states in this model; the off state can be thought of
 * as being the 'unknown' state. We assume an open world assumption. The absence of
 * a node in the query should be thought of as 'not observed' rather than 'not'.
 * 
 * TODO: document negation strategy
 * 
 * @author cjm
 *
 */
public class BayesianNetworkProfileMatcher extends AbstractProfileMatcher implements NegationAwareProfileMatcher {

	private Logger LOG = Logger.getLogger(BayesianNetworkProfileMatcher.class);

	ConditionalProbabilityIndex cpi;

	@Inject
	private BayesianNetworkProfileMatcher(BMKnowledgeBase kb) {
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
	public static BayesianNetworkProfileMatcher create(BMKnowledgeBase kb) {
		return new BayesianNetworkProfileMatcher(kb);
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
		cpi = TwoStateConditionalProbabilityIndex.create(kb);
		cpi.calculateConditionalProbabilities(kb);
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
		//int numClasses = knowledgeBase.getClassIdsInSignature().size();
		//EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap negatedQueryProfileBM = null;
		Set<String> negatedQueryClassIds = null;
		if (isUseNegation) {
			LOG.info("Using QueryWithNegation");
			QueryWithNegation nq = (QueryWithNegation)q;
			negatedQueryProfileBM = getDirectNegatedProfileBM(nq);
			negatedQueryClassIds = knowledgeBase.getClassIds(negatedQueryProfileBM);
			LOG.info("nqp=" + negatedQueryProfileBM+" // "+negatedQueryClassIds);
		}
		else {
			LOG.info("Not using QueryWithNegation");
		}

		Set<String> queryClassIds = q.getQueryClassIds();
		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			EWAHCompressedBitmap negatedTargetProfileBM = knowledgeBase.getNegatedTypesBM(itemId);
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			LOG.info("NEGATIVE TARGET PROFILE for "+itemId+" "+negatedTargetProfileBM);

			Calculator calc = new Calculator(targetProfileBM, negatedTargetProfileBM);
			//double p = calculateProbability(queryClassIds, targetProfileBM);
			double p = calc.calculateProbability(queryClassIds);

			if (negatedQueryProfileBM != null) {
				double np = 1 - calc.calculateProbability(negatedQueryClassIds);
				LOG.info("Combined Probability = (POS) "+p+" * (NEG) "+np);
				p = p*np;
			}
			
			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
			//LOG.info("p for "+itemId+" = "+p);
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
			this.negatedTargetProfileBM = negatedTargetProfileBM;
			probCache = new Double[getKnowledgeBase().getNumClassNodes()];
		}

		/**
		 * Top-level call
		 * 
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
		 * @param targetProfileBM
		 * @return probability
		 */
		public double calculateProbability(Set<String> queryClassIds) {
			double cump = 1.0;

			// treat set of query class Ids as a leaf node that is the
			// class intersection of all members; ie q1^...^qn
			// for a class intersection, the CPT is always such that
			//  Pr=1.0, if all parents=1
			//  Pr=0.0 otherwise
			for (String queryClassId : queryClassIds) {
				double p = calculateProbability(queryClassId);
				if (negatedTargetProfileBM != null) {
					if (knowledgeBase.getSuperClassesBM(queryClassId).andCardinality(negatedTargetProfileBM) > 0) {
						LOG.info("NEGATIVE EVIDENCE for "+queryClassId);
						p *= 0.01;  // TODO - do not hardcode false negative
					}
				}

				cump *= p;
			}
			return cump;
		}

		/**
		 * probability of queryClass being true, given that all
		 * nodes in target profile are on
		 * 
		 * @param queryClassId
		 * @param targetProfileBM
		 * @return probability
		 */
		private double calculateProbability(String queryClassId) {
			BMKnowledgeBase kb = getKnowledgeBase();
			int qcix = kb.getClassIndex(queryClassId);
			return calculateProbability(qcix);
		}

		/**
		 * Calculate the probability that a node qc is ON.
		 * 
		 *  - If this is specified in the query, then a set value is returned (1-FP);
		 *  - If not specified, equal to sum of probabilities of all states of parents
		 * 
		 * @param qcix
		 * @return
		 */
		private double calculateProbability(int qcix) {
			if (probCache[qcix] != null) {
				LOG.debug("Using cached for "+qcix);
				return probCache[qcix];
			}

			BMKnowledgeBase kb = getKnowledgeBase();
			LOG.debug("Calculating probability for "+qcix+" ie "+kb.getClassId(qcix));

			double returnProb;


			// TODO - optimization: determine efficiency of using get(ix) vs other methods
			if (targetProfileBM.get(qcix)) {
				LOG.debug("Q is in target profile");
				returnProb = 0.95; // TODO - do not hardcode
			}
			else {
				List<Integer> pixs = kb.getDirectSuperClassesBM(qcix).getPositions();
				double[] parentProbs = new double[pixs.size()];
				LOG.debug("calculating probabilities for parents");
				for (int i=0; i<pixs.size(); i++) {
					// recursive call
					parentProbs[i] = 
							calculateProbability(pixs.get(i));
				}

				int numParents = pixs.size();
				// assume two states for now: will be extendable to yes, no, unknown
				int numStateCombinations = (int) Math.pow(2, numParents);

				// sum of probabilities
				double sump = 0; // TODO: use logs

				// Pr(Q | Parents) = sum of { Pr(Q | off, off, ..., off), ... } 
				for (int parentsStateComboIx=0; parentsStateComboIx<numStateCombinations; parentsStateComboIx++) {
					double cp = cpi.getConditionalProbabilityChildIsOn(qcix, parentsStateComboIx);
					LOG.debug(" cp="+cp+" for states="+parentsStateComboIx);
					Map<Integer, Character> psm = cpi.getParentsToStateMapping(qcix, parentsStateComboIx);
					Set<Integer> onPixs = new HashSet<Integer>();
					for (int pix : psm.keySet()) {
						if (psm.get(pix) == 't') {
							onPixs.add(pix);
						}
					}
					LOG.debug("   onPixs="+onPixs);
					double p = 1.0;  // probability of ParentSetStateCombo
					for (int i=0; i<pixs.size(); i++) {
						int pix = pixs.get(i);
						p *= onPixs.contains(pix) ? parentProbs[i] : 1-parentProbs[i];
					}
					// final probability is sum of probability of all combinations
					sump += cp * p;
				}
				LOG.debug("Calculated probability for "+qcix+" ie "+kb.getClassId(qcix)+" = "+sump);

				returnProb = sump;
			}
			probCache[qcix] = returnProb;
			return returnProb;
		}


	}


}
