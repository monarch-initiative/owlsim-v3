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
 * Calculate probability of observing query (e.g. patient profile) given target
 * as evidence.
 * 
 * This implementation does not explicitly model NOTs, it uses a
 * {@link TwoStateConditionalProbabilityIndex}. The two states are ON
 * (true/observed) and OFF (unknown/not observed) - note the open world
 * assumptions: that the off state means there is no information about the truth
 * of the node, <i>it does not mean the node is false</i>.
 * 
 * Although we do not model negation as a 3rd state, we still compute on
 * negation, post-hoc, see below.
 * 
 * <h2>Calculating probabilities</h2>
 * <h3>Calculating probabilities for a single query node</h3>
 * 
 * Using a {@link TwoStateConditionalProbabilityIndex}, probabilities propagate
 * TO a child FROM its parents.
 * 
 * If the query node is ON, and the node is ON in the target, then Pr = 1-fnr;
 * otherwise the probability is calculated based on the probability of the
 * parents.
 * 
 * 
 * The probability of a child node being on C=on is dependent on the state of
 * its parents; we sum over 2<sup>N</sup> states
 * 
 * 
 * <code>
 * <pre>
 * Pr( C=on | P1=P1_s, ..., Pn=P2_s) =
 * Pr( C=on | S1, ..., Sn) =             <-- syntactic sugar
 *   Pr( C=on | on,on,...,on ) * Pr(on,on,...,on) +
 *   Pr( C=on | off,on,...,on ) * Pr(off,on,...,on) +
 *   ...
 *   Pr( C=on | off,off,...,off ) * Pr(off,off,...,off) 
 *   </pre>
 * </code>
 * 
 * For any given query Q=Q1,...Qm, we assume independent probabilities and
 * calculate Pr(Q) = Pq(Q1=on,...,Qm=on)
 * 
 * <h2>Negation</h2>
 * 
 * Each node can only have two states in this model; the off state can be
 * thought of as being the 'unknown' state. We assume an open world assumption.
 * The absence of a node in the query should be thought of as 'not observed'
 * rather than 'not'.
 * 
 * We still include negation in the calculation; for any negated query node i,
 * we calculate Pr(i) = ON, and assign a final probability of 1-fnr (this is the
 * only circumstance a fnr can have an effect, since we have the open world
 * model).
 * 
 * Similarly, for any negated target node j, the Pr of any query under this will
 * be 1-fpr
 * 
 * <h2>TODOs</h2>
 * 
 * Currently this method is too slow to be used for dynamic queries, taking 1-5s
 * per query. Some efficiency could be gained by calculating with log-probs.
 * 
 * If we cache probabilities per-node for every target, we would gain a lot of
 * speed, space = NumClasses x NumTargets
 * 
 * 
 * @author cjm
 *
 */
public class BayesianNetworkProfileMatcher extends AbstractProfileMatcher implements NegationAwareProfileMatcher {

	private Logger LOG = Logger.getLogger(BayesianNetworkProfileMatcher.class);

	double falseNegativeRate = 0.01; // TODO - do not hardcode
	double falsePositiveRate = 0.01; // TODO - do not hardcode

	ConditionalProbabilityIndex cpi = null; // index of
											// Pr(Node={on,off}|ParentsState)

	@Deprecated
	private Calculator[] calculatorCache;
	private Double[][] targetClassProbabilityCache;

    @Inject
	private BayesianNetworkProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
		int N = kb.getIndividualIdsInSignature().size();
		calculatorCache = new Calculator[N];
		for (int i = 0; i < N; i++) {
			calculatorCache[i] = null;
		}
		targetClassProbabilityCache = new Double[N][];
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

	public void precompute() {
		if (cpi != null)
			return;
		try {
			calculateConditionalProbabilities(knowledgeBase);
		} catch (IncoherentStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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

		precompute();
		boolean isUseNegation = q instanceof QueryWithNegation;

		double sumOfProbs = 0.0;
		EWAHCompressedBitmap negatedQueryProfileBM = null;
		Set<String> negatedQueryClassIds = null;
		if (isUseNegation) {
			LOG.info("Using QueryWithNegation");
			QueryWithNegation nq = (QueryWithNegation) q;
			negatedQueryProfileBM = getDirectNegatedProfileBM(nq);
			negatedQueryClassIds = knowledgeBase.getClassIds(negatedQueryProfileBM);
			LOG.info("nqp=" + negatedQueryProfileBM + " // " + negatedQueryClassIds);
		} else {
			LOG.info("Not using QueryWithNegation");
		}

		Set<String> queryClassIds = q.getQueryClassIds();
		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n = 0;

		// TODO - FOR DEBUGGING ONLY
		// int nc=0;
		// for (String itemId : indIds) {
		// int indIx = knowledgeBase.getIndividualIndex(itemId);
		// if (targetClassProbabilityCache[indIx] != null) {
		// Double[] a = targetClassProbabilityCache[indIx];
		// for (int i=0; i<a.length; i++) {
		// if (a[i] != null) {
		// nc++;
		// }
		// }
		// }
		// }
		// System.out.println("NUM_CACHED:"+nc);

		double debugMaxP = 0.0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			EWAHCompressedBitmap negatedTargetProfileBM = knowledgeBase.getNegatedTypesBM(itemId);

			int indIx = knowledgeBase.getIndividualIndex(itemId);
			// we create a new calculator for every target;
			// TODO: investigate speedup from cacheing this <-- doing this now
			Calculator calc = new Calculator(targetProfileBM, negatedTargetProfileBM);

			if (false) {
				if (targetClassProbabilityCache[indIx] == null) {
					LOG.info("Assigning cached probs for " + itemId);
					targetClassProbabilityCache[indIx] = calc.probCache;
				} else {
					calc.probCache = targetClassProbabilityCache[indIx];
				}
			}
			double p = calc.calculateProbability(queryClassIds);
			if (p > debugMaxP) {
				debugMaxP = p;
			}

			if (Double.isNaN(p)) {
				LOG.error("NaN for tgt " + itemId);
			}

			// NEGATION
			if (negatedQueryProfileBM != null) {
				double np = 1 - calc.calculateProbability(negatedQueryClassIds);
				// LOG.info("Combined Probability = (POS) "+p+" * (NEG) "+np);
				p = p * np;
			}

			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
			// LOG.info("p for "+itemId+" = "+p);
		}
		if (sumOfProbs == 0.0) {
			LOG.error("sumOfProds=0.0");
		}
		if (Double.isNaN(sumOfProbs)) {
			LOG.error("NaN for sumOfProds");
		}

		int tempNumNans = 0;
		for (n = 0; n < pvector.length; n++) {
			double p = pvector[n] / sumOfProbs;
			if (Double.isNaN(p)) {
				tempNumNans++;
			}

			String id = indArr[n];
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(id);
			mp.add(createMatch(id, label, p));
		}
		if (tempNumNans > 0) {
			LOG.error("#NaNs " + tempNumNans + " / " + pvector.length);
			LOG.error("maxPr = " + debugMaxP);
		}
		mp.sortMatches();
		return mp;
	}

	/**
	 * We wrap calculation within a class to allow for cacheing relative to a
	 * particular targetProfile
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
		 * Calculate the probability of all queryClasses being on, given the
		 * nodes in the target profile are not
		 * 
		 * Note: currently this is asymmetric; ie we do not calculate the
		 * probability of the target given the query nodes are on; this has the
		 * effect of penalizing large queries; for a fixed query this is not an
		 * issue. However, it also does *not* penalize broad-spectrum targets.
		 * 
		 * This also means the FNR is meaningless, unless negation is explicitly
		 * used
		 * 
		 * @param queryClassIds
		 * @param targetProfileBM
		 * @return probability of all queryClasses being on
		 */
		public double calculateProbability(Set<String> queryClassIds) {
			double cump = 1.0;

			// treat set of query class Ids as a leaf node that is the
			// class intersection of all members; ie q1^...^qn
			// for a class intersection, the CPT is always such that
			// Pr=1.0, if all parents=1
			// Pr=0.0 otherwise
			for (String queryClassId : queryClassIds) {
				double p = calculateProbability(queryClassId);

				if (Double.isNaN(p)) {
					LOG.error("NaN for qc=" + queryClassId);
				}

				// NEGATION
				// the FNR only comes into play if negation is explicitly
				// specified.
				// If the query is on but a superclass in the target has been
				// negated,
				// we assume the query is a false positive
				if (negatedTargetProfileBM != null) {
					if (knowledgeBase.getSuperClassesBM(queryClassId).andCardinality(negatedTargetProfileBM) > 0) {
						LOG.info("NEGATIVE EVIDENCE for " + queryClassId);
						p *= falsePositiveRate;
					}
				}
				cump *= p;
			}
			return cump;
		}

		/**
		 * probability of queryClass being true, given that all nodes in target
		 * profile are on
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
		 * - If this is specified in the query, then a set value is returned
		 * (1-FP); - If not specified, equal to sum of probabilities of all
		 * states of parents
		 * 
		 * Side effects: caches probability
		 * 
		 * @param qcix
		 * @return Pr(Qi=on|T)
		 */
		private double calculateProbability(int qcix) {
			if (probCache[qcix] != null) {
				LOG.debug("Using cached for " + qcix);
				return probCache[qcix];
			}

			BMKnowledgeBase kb = getKnowledgeBase();
			LOG.debug("Calculating probability for " + qcix + " ie " + kb.getClassId(qcix));

			double probQiGivenT;

			// TODO - optimization: determine efficiency of using get(ix) vs
			// other methods
			if (targetProfileBM.get(qcix)) {
				LOG.debug("Qi is in target profile");
				probQiGivenT = 1 - falsePositiveRate;
			} else {
				// Qi is NOT in target profile;
				// Pr(Qi=on | T) = Pr(QiP1=on, QiP2=on, ..|T)Pr(on on...) +
				// Pr(QiP1=off, ...)
				List<Integer> pixs = kb.getDirectSuperClassesBM(qcix).getPositions();
				double[] parentProbs = new double[pixs.size()];
				LOG.debug("calculating probabilities for parents");
				for (int i = 0; i < pixs.size(); i++) {
					// recursive call; cache prevents repeated calculations
					parentProbs[i] = calculateProbability(pixs.get(i));
				}

				int numParents = pixs.size();
				// assume two states for now: will be extendable to yes, no,
				// unknown
				int numStateCombinations = (int) Math.pow(2, numParents);

				// sum of probabilities
				double sump = 0; // TODO: use logs

				// Pr(Q | Parents) = sum of { Pr(Q | off, off, ..., off), ... }
				for (int parentsStateComboIx = 0; parentsStateComboIx < numStateCombinations; parentsStateComboIx++) {
					double cp = cpi.getConditionalProbabilityChildIsOn(qcix, parentsStateComboIx);
					LOG.debug(" cp=" + cp + " for states=" + parentsStateComboIx);
					Map<Integer, Character> psm = cpi.getParentsToStateMapping(qcix, parentsStateComboIx);
					Set<Integer> onPixs = new HashSet<Integer>();
					for (int pix : psm.keySet()) {
						if (psm.get(pix) == 't') {
							onPixs.add(pix);
						}
					}
					LOG.debug("   onPixs=" + onPixs);
					double p = 1.0; // probability of ParentSetStateCombo
					for (int i = 0; i < pixs.size(); i++) {
						int pix = pixs.get(i);
						p *= onPixs.contains(pix) ? parentProbs[i] : 1 - parentProbs[i];
					}
					// final probability is sum of probability of all
					// combinations
					sump += cp * p;
				}
				LOG.debug("Calculated probability for " + qcix + " ie " + kb.getClassId(qcix) + " = " + sump);

				probQiGivenT = sump;
			}
			probCache[qcix] = probQiGivenT;
			return probQiGivenT;
		}

	}

}
