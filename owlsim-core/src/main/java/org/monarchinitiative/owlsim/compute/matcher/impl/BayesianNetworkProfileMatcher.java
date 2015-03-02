package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Calculate of observing query given target as evidence.
 * 
 * Note this first implementation does not use NOTs
 * 
 * 
 * 
 * @author cjm
 *
 */
public class BayesianNetworkProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(BayesianNetworkProfileMatcher.class);
	
	ConditionalProbabilityIndex cpi;

	@Inject
	private BayesianNetworkProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
		calculateConditionalProbabilities(kb);
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

	public void calculateConditionalProbabilities(BMKnowledgeBase kb) {
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
		int numClasses = knowledgeBase.getClassIdsInSignature().size();
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap negatedQueryProfileBM = null;
		if (isUseNegation) {
			LOG.info("Using negation*******");
			QueryWithNegation nq = (QueryWithNegation)q;
			negatedQueryProfileBM = getNegatedProfileBM(nq);
			LOG.info("nqp=" + negatedQueryProfileBM);
		}

		Set<String> queryClassIds = q.getQueryClassIds();
		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			LOG.debug("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			
			double p = calculateProbability(queryClassIds, targetProfileBM);
			
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

	public double calculateProbability(Set<String> queryClassIds,
			EWAHCompressedBitmap targetProfileBM) {
		double cump = 1.0;
		
		// treat set of query class Ids as a leaf node that is the
		// class intersection of all members; ie q1^...^qn
		// for a class intersection, the CPT is always such that
		//  Pr=1.0, if all parents=1
		//  Pr=0.0 otherwise
		for (String queryClassId : queryClassIds) {
			double p = calculateProbability(queryClassId, targetProfileBM);
			cump *= p;
		}
		return cump;
	}

	private double calculateProbability(String queryClassId,
			EWAHCompressedBitmap targetProfileBM) {
		BMKnowledgeBase kb = getKnowledgeBase();
		int qcix = kb.getClassIndex(queryClassId);
		return calculateProbability(qcix, targetProfileBM);
	}
	
	private double calculateProbability(int qcix,
			EWAHCompressedBitmap targetProfileBM) {
		BMKnowledgeBase kb = getKnowledgeBase();
		LOG.info("Calculating probability for "+qcix+" ie "+kb.getClassId(qcix));
		
		// TODO - determine efficiency of using get(ix) vs other methods
		if (targetProfileBM.get(qcix)) {
			LOG.info("Q is in target profile");
			return 0.95; // TODO - do not hardcode
		}
		else {
			List<Integer> pixs = kb.getDirectSuperClassesBM(qcix).getPositions();
			double[] parentProbs = new double[pixs.size()];
			LOG.info("calculating for parents");
			for (int i=0; i<pixs.size(); i++) {
				parentProbs[i] = 
						calculateProbability(pixs.get(i), targetProfileBM);
			}
			int numParents = pixs.size();
			// assume two states for now: will be extendable to yes, no, unknown
			int numStates = (int) Math.pow(2, numParents);
			double sump = 0; // TODO: use logs
			for (int parentState=0; parentState<numStates; parentState++) {
				double cp = cpi.getConditionalProbability(qcix, parentState);
				LOG.info(" cp="+cp+" for states="+parentState);
				Map<Integer, Character> psm = cpi.getParentsToStateMapping(qcix, parentState);
				Set<Integer> onPixs = new HashSet<Integer>();
				for (int pix : psm.keySet()) {
					if (psm.get(pix) == 't') {
						onPixs.add(pix);
					}
				}
				LOG.info("   onPixs="+onPixs);
				double p = 1.0;
				for (int i=0; i<pixs.size(); i++) {
					int pix = pixs.get(i);
					p *= onPixs.contains(pix) ? parentProbs[i] : 1-parentProbs[i];
				}
				sump += p * cp;
			}			
			LOG.info("Calculated probability for "+qcix+" ie "+kb.getClassId(qcix)+" = "+sump);

			return sump;
		}
	}





}
