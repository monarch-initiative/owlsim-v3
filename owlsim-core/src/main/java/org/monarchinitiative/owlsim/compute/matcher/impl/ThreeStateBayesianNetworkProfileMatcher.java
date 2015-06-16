package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.cpt.impl.NodeProbabilities;
import org.monarchinitiative.owlsim.compute.cpt.impl.ThreeStateConditionalProbabilityIndex;
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
 * Pr(Child = on | P) = SUM[0<i<3^|P|] { Pr(Child=on | Si) * Pr(Si)   } 
 * 
 * 
 * @author cjm
 *
 */
public class ThreeStateBayesianNetworkProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(ThreeStateBayesianNetworkProfileMatcher.class);

	private ThreeStateConditionalProbabilityIndex cpi;
	private Map<BitMapPair,NodeProbabilities[]> targetToQueryCache;

	@Inject
	private ThreeStateBayesianNetworkProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
		try {
			calculateConditionalProbabilities(kb);
		} catch (IncoherentStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		targetToQueryCache = new HashMap<BitMapPair, NodeProbabilities[]>();
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


	
	public class BitMapPair {
		public final EWAHCompressedBitmap bm1;
		public final EWAHCompressedBitmap bm2;
		
		public BitMapPair(EWAHCompressedBitmap bm1, EWAHCompressedBitmap bm2) {
			super();
			this.bm1 = bm1;
			this.bm2 = bm2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((bm1 == null) ? 0 : bm1.hashCode());
			result = prime * result + ((bm2 == null) ? 0 : bm2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BitMapPair other = (BitMapPair) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (bm1 == null) {
				if (other.bm1 != null)
					return false;
			} else if (!bm1.equals(other.bm1))
				return false;
			if (bm2 == null) {
				if (other.bm2 != null)
					return false;
			} else if (!bm2.equals(other.bm2))
				return false;
			return true;
		}
		private ThreeStateBayesianNetworkProfileMatcher getOuterType() {
			return ThreeStateBayesianNetworkProfileMatcher.this;
		}
		
		
	}
	
	
	
	/**
	 * note that this is exposed primarily for debugging purposes
	 * 
	 * @return cache
	 */
	public Map<BitMapPair, NodeProbabilities[]> getTargetToQueryCache() {
		return targetToQueryCache;
	}

	/**
	 * Creates a CPT from the knowledgebase. Should be called on initiation.
	 * 
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
	 * @throws IncoherentStateException 
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) throws IncoherentStateException {

		boolean isUseNegation = q instanceof QueryWithNegation;
		if (!isUseNegation) {
			LOG.error("Consider using TwoState BN, this will be inefficient");
		}
		EWAHCompressedBitmap negatedQueryProfileBM = null;
		Set<String> negatedQueryClassIds = null;

		//double fpr = getFalsePositiveRate();
		//double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;
		//int numClasses = knowledgeBase.getClassIdsInSignature().size();
		//EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		//		EWAHCompressedBitmap negatedQueryProfileBM = null;
		if (isUseNegation) {
			LOG.info("Using QueryWithNegation");
			QueryWithNegation nq = (QueryWithNegation)q;
			negatedQueryProfileBM = getDirectNegatedProfileBM(nq);
			negatedQueryClassIds = knowledgeBase.getClassIds(negatedQueryProfileBM);
			LOG.info("nqp=" + negatedQueryProfileBM);
		}
		else {
			LOG.info("Not using QueryWithNegation");
		}

		Set<String> queryClassIds = q.getQueryClassIds();
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
		BitMapPair targetProfilePair;
		
		// cache that is local to a particular candidate target
		//NodeProbabilities[] probCache;

		public Calculator(EWAHCompressedBitmap targetProfileBM, EWAHCompressedBitmap negatedTargetProfileBM) {
			super();
			this.targetProfileBM = targetProfileBM;
			this.negatedTargetProfileBM = negatedTargetProfileBM;
			targetProfilePair = new BitMapPair(targetProfileBM, negatedTargetProfileBM);
			if (!targetToQueryCache.containsKey(targetProfilePair)) {
				targetToQueryCache.put(targetProfilePair, 
						new NodeProbabilities[knowledgeBase.getNumClassNodes()]);
			}
			//probCache = new NodeProbabilities[getKnowledgeBase().getNumClassNodes()];
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
		 * @throws IncoherentStateException 
		 */
		public double calculateProbability(Set<String> queryClassIds, 
				Set<String> negatedQueryClassIds) throws IncoherentStateException {
			double cump = 1.0;

			// treat set of query class Ids as a leaf node that is the
			// class intersection of all members; ie q1^...^qn
			// for a class intersection, the CPT is always such that
			//  Pr=1.0, if all parents=1
			//  Pr=0.0 otherwise
			for (String queryClassId : queryClassIds) {
				//LOG.info("+Q"+queryClassId);
				double p = calculateProbability(queryClassId).prOn;
				cump *= p;
			}
			if (negatedQueryClassIds != null) {
				// TODO: prOff=0
				for (String negatedQueryClassId : negatedQueryClassIds) {
					LOG.info("-Q"+negatedQueryClassId);
					double p = calculateProbability(negatedQueryClassId).prOff;
					LOG.info("   prOff="+p);
					cump *= p;
				}
			}
			return cump;
		}

		/**
		 * probability of queryClass being true, given all targets are on
		 * 
		 * @param queryClassId
		 * @param targetProfileBM
		 * @return probability
		 * @throws IncoherentStateException 
		 */
		private NodeProbabilities calculateProbability(String queryClassId) throws IncoherentStateException {
			BMKnowledgeBase kb = getKnowledgeBase();
			int qcix = kb.getClassIndex(queryClassId);
			return calculateProbability(qcix); 
		}


		/**
		 * Pr(Child = on | P) = SUM[0<i<3^|P|] { Pr(Si) | Pr(Child=on | Si)   }
		 * 
		 * @param qcix
		 * @return
		 * @throws IncoherentStateException 
		 */
		private NodeProbabilities calculateProbability(int qcix) throws IncoherentStateException {
			
			NodeProbabilities[] probCache = targetToQueryCache.get(targetProfilePair);
				
			if (probCache[qcix] != null) {
				LOG.debug("Using cached for "+qcix);
				return probCache[qcix];
			}

			BMKnowledgeBase kb = getKnowledgeBase();
			LOG.debug("Calculating probability for "+qcix+" ie "+kb.getClassId(qcix));

			NodeProbabilities prdChild;
			// TODO - determine efficiency of using get(ix) vs other methods
			if (targetProfileBM.get(qcix)) {
				LOG.info("Q is in target profile");
				prdChild = new NodeProbabilities(0.95, 0.01); // TODO - do not hardcode
			}
			else if (negatedTargetProfileBM != null &&
					negatedTargetProfileBM.get(qcix)) {
				LOG.info("Q is in negative target profile");
				prdChild = new NodeProbabilities(0.01, 0.99); // TODO - do not hardcode
			}
			else {
				List<Integer> pixs = kb.getDirectSuperClassesBM(qcix).getPositions();
				NodeProbabilities[] parentOnProbs = new NodeProbabilities[pixs.size()];
				LOG.debug("calculating for parents");
				for (int i=0; i<pixs.size(); i++) {
					// TODO - cache, to avoid repeated calculations
					// recursive call
					parentOnProbs[i] = 
							calculateProbability(pixs.get(i));
				}

				int numParents = pixs.size();
				int numStates = (int) Math.pow(3, numParents);

				// sum of probabilities
				double sumParentOnProbs = 0; // TODO: use logs
				double sumParentOffProbs = 0; // TODO: use logs

				// Pr(Q | Parents) = sum of { Pr(Q | off, off, ..., off), ... } 
				for (int parentsStateComboIx=0; parentsStateComboIx<numStates; parentsStateComboIx++) {
					NodeProbabilities cprd = 
							cpi.getConditionalProbabilityDistribution(qcix, parentsStateComboIx);
					//LOG.debug(" cp="+cpOn+" for states="+parentsStateComboIx);
					Map<Integer, Character> psm = cpi.getParentsToStateMapping(qcix, parentsStateComboIx);
					Set<Integer> onPixs = new HashSet<Integer>(); // set of parents in On state
					for (int pix : psm.keySet()) {
						if (psm.get(pix) == 't') {
							onPixs.add(pix);
						}
					}
					LOG.debug("   onPixs="+onPixs);
					double prParentStateCombo = 1.0;
					for (int i=0; i<pixs.size(); i++) {
						int pix = pixs.get(i);
						Character parentState = psm.get(pix);
						NodeProbabilities prdParent = parentOnProbs[i];

						// TODO - fix this for 3-state
						double prParentInState;
						if (parentState == 't') {
							prParentInState = prdParent.prOn;
						}
						else if (parentState == 'f') {
							prParentInState = prdParent.prOff;
						}
						else if (parentState == 'u') {
							prParentInState = prdParent.prUnknown;
						}
						else {
							throw new IncoherentStateException("Invalid state: "+parentState);
						}
						if (prParentInState < 0) {
							throw new IncoherentStateException("Invalid probability of "+
									parentState+" = "+prParentInState);
						}

						prParentStateCombo *= prParentInState;
					}
					sumParentOnProbs += prParentStateCombo * cprd.prOn;
					sumParentOffProbs += prParentStateCombo * cprd.prOff;
				}
				LOG.info("Calculated probability for "+qcix+" ie "+kb.getClassId(qcix)+
						" On= "+sumParentOnProbs+" Off= "+sumParentOffProbs);

				prdChild = new NodeProbabilities(sumParentOnProbs, sumParentOffProbs);
			}
			probCache[qcix] = prdChild;
			return prdChild;
		}


	}


}
