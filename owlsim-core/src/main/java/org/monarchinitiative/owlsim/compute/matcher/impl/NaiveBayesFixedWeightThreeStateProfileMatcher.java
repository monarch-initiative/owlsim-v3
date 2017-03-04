package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * As naive bayes fixed weight, but allowing for 3 states
 * 
 * 
 * 
 * 
 * 
 * @author cjm
 *
 */
public class NaiveBayesFixedWeightThreeStateProfileMatcher extends AbstractProfileMatcher
		implements NegationAwareProfileMatcher {

	private Logger LOG = Logger.getLogger(NaiveBayesFixedWeightThreeStateProfileMatcher.class);

	private NaiveBayesFixedWeightThreeStateProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}

	/**
	 * @param kb
	 * @return new instance
	 */
	public static NaiveBayesFixedWeightThreeStateProfileMatcher create(BMKnowledgeBase kb) {
		return new NaiveBayesFixedWeightThreeStateProfileMatcher(kb);
	}

	@Override
	public String getShortName() {
		return "naive-bayes-fixed-weight-three-state";
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

		return onQueryNodesBM.or(EWAHUtils.convertIndexSetToBitmap(nodesWithOnParents));
	}

	// any negated query node that has at least one negated parent;
	// these are counted as no-transition
	private EWAHCompressedBitmap getQueryNegatedNoTransition(EWAHCompressedBitmap negatedQueryProfileBM) {
		Set<Integer> nodes = new HashSet<Integer>();

		// there may be more efficient ways of doing this, but this is
		// called once at the start of the search...
		for (int cix : negatedQueryProfileBM.getPositions()) {
			EWAHCompressedBitmap supsBM = knowledgeBase.getDirectSuperClassesBM(cix);
			int nParents = supsBM.cardinality();
			if (supsBM.andCardinality(negatedQueryProfileBM) > 0) {
				nodes.add(cix);
			}
		}

		return EWAHUtils.convertIndexSetToBitmap(nodes);
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		// double fpr = getFalsePositiveRate();
		// double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;

		EWAHCompressedBitmap nodesQtBM = getProfileBM(q);
		EWAHCompressedBitmap nodesQfBM = getNegatedProfileBM(q);

		// first, given a query (on and off states),
		// group all nodes according to transitions from parent node

		// nomenclature: QUERY {unk,true,false} PARENTS {unk,true,false}+
		// multiple values taken as union

		// uncommitted nodes with a true parent (trans)
		Set<Integer> nodesQuPt = new HashSet<Integer>();

		// uncommitted nodes with an uncommitted parent (no trans)
		Set<Integer> nodesQuPu = new HashSet<Integer>();

		// off nodes with on or uncommitted parent (trans)
		Set<Integer> nodesQfPtu = new HashSet<Integer>();

		// off nodes with uncommitted parent
		Set<Integer> nodesQfPu = new HashSet<Integer>();

		// calculate transitions for all query nodes
		/*
		 * if Q=t, then ALL parents MUST be t (NO transitions) if Q=u, then
		 * EITHER ALL parents ARE t : TRANSITION T->U AT LEAST ONE parent is=u :
		 * NO TRANSITION U->U NO PARENT is f if Q=f, then EITHER ONE parent IS f
		 * : NO TRANSITION F->F ALL parents ARE t : TRANSITION T->F ELSE :
		 * TRANSITION U->F
		 */
		for (String cid : knowledgeBase.getClassIdsInSignature()) {
			int cix = knowledgeBase.getClassIndex(cid);
			if (nodesQtBM.getPositions().contains(cix)) {
				// state T, transition must be T->T
				continue;
			}
			EWAHCompressedBitmap parentsBM = knowledgeBase.getDirectSuperClassesBM(cix);
			if (nodesQfBM.getPositions().contains(cix)) {
				// state = F
				if (parentsBM.andCardinality(nodesQfBM) == 0) {
					// transition T,U -> F
					nodesQfPtu.add(cix);
				} else {
					// F->F
				}
			} else {
				// state = U
				if (parentsBM.andCardinality(nodesQtBM) < parentsBM.cardinality()) {
					// transition T -> U ( F->U is impossible )
					nodesQuPt.add(cix);
				} else {
					// U->U
					nodesQuPu.add(cix);
				}
			}

		}
		EWAHCompressedBitmap nodesQuPtBM = EWAHUtils.convertIndexSetToBitmap(nodesQuPt);
		EWAHCompressedBitmap nodesQuPuBM = EWAHUtils.convertIndexSetToBitmap(nodesQuPu);
		EWAHCompressedBitmap nodesQfPtBM = EWAHUtils.convertIndexSetToBitmap(nodesQfPtu);
		EWAHCompressedBitmap nodesQfPuBM = EWAHUtils.convertIndexSetToBitmap(nodesQfPu);

		// include subclasses

		EWAHCompressedBitmap queryNegatedNoTransitionBM = getQueryNegatedNoTransition(nodesQfBM);
		EWAHCompressedBitmap queryNegatedWithTransitionBM = nodesQfBM.andNot(queryNegatedNoTransitionBM);

		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n = 0;

		// pr(Q=f | H=t)
		double prFalseNegative = 0.000001;

		// pr(Q=t | H=f)
		double prFalsePositive = 0.00001;

		// pr(Q=u | H=t) -- like a weaker false negative
		double prFalseMiss = 0.01;

		// pr(Q=u | H=f) -- failure to make a call when hidden is false
		double prTrueMiss = 0.85;

		// double prWeakFalsePositive = prFalsePositive * 100;
		// double prWeakFalsePositive = Math.exp(Math.log(prFalsePositive) /4 );
		double prWeakFalsePositive = 0.1;

		double pprQtHtPt = 1 - (prFalseNegative + prFalseMiss);
		double pprQfHfPt = 1 - (prFalsePositive + prTrueMiss);

		// double prWeakTrueMiss = prTrueMiss * 2; // failure to make a call
		// when hidden is non-obvious false
		double prWeakTrueMiss = 0.85;
		for (String itemId : indIds) {
			EWAHCompressedBitmap nodesHtBM = knowledgeBase.getTypesBM(itemId);

			// EWAHCompressedBitmap nodesHfBM =
			// knowledgeBase.getNegatedTypesBM(itemId);
			// TODO: consider propagating down
			EWAHCompressedBitmap nodesHfBM = knowledgeBase.getDirectNegatedTypesBM(itemId);

			// any node which has an off query parent is discounted
			// EWAHCompressedBitmap maskedTargetProfileBM =
			// nodesHtBM.and(queryBlanketProfileBM);

			LOG.info("TARGET PROFILE for " + itemId + " " + nodesHtBM);

			// cumulative log-probability
			double logp = 0.0;
			// 3^3=27 combos for q (query), h (hidden) and p (parents)
			// with states t, f and u

			// ---
			// *** Hidden/Target=TRUE
			// ---

			// ** T,T

			// 1. P(qi=TRUE | hi=TRUE, p(qi)=TRUE) = 1-(FN + FALSEMISS)
			// note that if Q=t and H=t then it's impossible for P=u OR P=f;
			// hence we use QtBM
			int nQtHtPt = nodesQtBM.andCardinality(nodesHtBM);
			if (nQtHtPt > 0) {
				double cprQtHtPt = Math.pow(pprQtHtPt, nQtHtPt);
				LOG.info("  nQtHtPt=" + nQtHtPt + " pr= " + cprQtHtPt);
				logp += Math.log(cprQtHtPt);
			}

			// P(qi=FALSE | hi=TRUE, p(qi)=TRUE) = FN
			// P(qi=FALSE | hi=TRUE, p(qi)=UNK) = FN
			// note we can combine P(qi=FALSE | hi=TRUE) for any non-false
			// parent setting
			// hence we use QfBM
			int nQfHtPt = nodesQfBM.andCardinality(nodesHtBM);
			if (nQfHtPt > 0) {
				double cprQfHtPt = Math.pow(prFalseNegative, nQfHtPt);
				LOG.info("  nQfHtPt=" + nQfHtPt + " pr= " + cprQfHtPt);
				logp += Math.log(cprQfHtPt);
			}

			// P(qi=UNK | hi=TRUE, p(qi)=TRUE) = FALSEMISS
			int nQuHtPt = nodesQuPtBM.andCardinality(nodesHtBM);
			if (nQuHtPt > 0) {
				double cprQuHtPt = Math.pow(prFalseMiss, nQuHtPt);
				LOG.info("  nQuHtPt=" + nQuHtPt + " pr= " + cprQuHtPt);
				logp += Math.log(cprQuHtPt);
			}

			// ** T,F
			// none of these contribute to the score

			// P(qi=TRUE | hi=TRUE, p(qi)=FALSE) = 0
			// P(qi=FALSE | hi=TRUE, p(qi)=FALSE) = 1
			// P(qi=UNK | hi=TRUE, p(qi)=FALSE) = 0

			// T,U

			// P(qi=TRUE | hi=TRUE, p(qi)=UNK) = 0
			// * ALREADY COVERED IN ABOVE: P(qi=FALSE | hi=TRUE, p(qi)=UNK) = FN

			// P(qi=UNK | hi=TRUE, p(qi)=UNK) = 1-FN
			int nQuHtPu = nodesQuPuBM.andCardinality(nodesHtBM);
			if (nQuHtPu > 0) {
				double cprQuHtPu = Math.pow(1 - prFalseNegative, nQuHtPu);
				LOG.info("  nQuHtPu=" + nQuHtPu + " pr= " + cprQuHtPu);
				logp += Math.log(cprQuHtPu);
			}

			// ---
			// *** Hidden/Target is FALSE
			// ---

			// F,T

			// P(qi=TRUE | hi=FALSE, p(qi)=TRUE) = FP // e.g. 0.001
			// TODO: should check c(qi), and negation flows in the other
			// direction
			// note that if Q=t, then P=t, hence we use Qt
			int nQtHfPt = nodesQtBM.andCardinality(nodesHfBM);
			if (nQtHfPt > 0) {
				double cprQtHfPt = Math.pow(prFalsePositive, nQtHfPt);
				LOG.info("  nQtHfPt=" + nQtHfPt + " pr= " + cprQtHfPt);
				logp += Math.log(cprQtHfPt);
			}
			// P(qi=FALSE | hi=FALSE, p(qi)=TRUE) = 1-(FP+TRUEMISS) // keep this
			// high
			int nQfHfPt = nodesQfPtBM.andCardinality(nodesHfBM);
			if (nQfHfPt > 0) {
				double cprQfHfPt = Math.pow(pprQfHfPt, nQfHfPt);
				LOG.info("  nQfHfPt=" + nQfHfPt + " pr= " + cprQfHfPt);
				logp += Math.log(cprQfHfPt);
			}

			// P(qi=UNK | hi=FALSE, p(qi)=TRUE) = TRUEMISS // e.g. 0.05
			int nQuHfPt = nodesQuPtBM.andCardinality(nodesHfBM);
			if (nQuHfPt > 0) {
				double cprQuHfPt = Math.pow(prTrueMiss, nQuHfPt);
				LOG.info("  nQuHfPt=" + nQuHfPt + " pr= " + cprQuHfPt);
				logp += Math.log(cprQuHfPt);
			}

			// F,F

			// P(qi=TRUE | hi=FALSE, p(qi)=FALSE) = 0
			// P(qi=FALSE | hi=FALSE, p(qi)=FALSE) = 1
			// P(qi=UNK | hi=FALSE, p(qi)=FALSE) = 0

			// F,U

			// P(qi=TRUE | hi=FALSE, p(qi)=UNK) = 0
			// P(qi=FALSE | hi=FALSE, p(qi)=UNK) = 1-TRUEMISS
			int nQfHfPu = nodesQfPuBM.andCardinality(nodesHfBM);
			if (nQfHfPu > 0) {
				double cprQfHfPu = Math.pow(1 - prTrueMiss, nQfHfPu);
				LOG.info("  nQfHfPu=" + nQfHfPu + " pr= " + cprQfHfPu);
				logp += Math.log(cprQfHfPu);
			}

			// P(qi=UNK | hi=FALSE, p(qi)=UNK) = TRUEMISS // e.g.
			int nQuHfPu = nodesQuPuBM.andCardinality(nodesHfBM);
			if (nQuHfPu > 0) {
				double cprQuHfPu = Math.pow(prTrueMiss, nQuHfPu);
				LOG.info("  nQuHfPu=" + nQuHfPu + " pr= " + cprQuHfPu);
				logp += Math.log(cprQuHfPu);
			}

			// ---
			// Hidden/Target is UNKNOWN (aka FALSE')
			// ---
			// 'unknown' for a hidden state makes no sense; also it would
			// introduce combinatorial explosions.
			// here we interpret the 3rd state as being logically FALSE, but as
			// being false in a non-obvious way, with lower penalties for
			// not observing the falseness

			// ** U,T

			// P(qi=TRUE | hi=UNK, p(qi)=TRUE) = FP' // > FP (it's more likely
			// to make a false call if it's non-obvious)
			// note that if Q=t, then P=t, hence we use Qt
			int nQtHuPt = nodesQtBM.andNot(nodesHtBM).andNotCardinality(nodesHfBM);
			if (nQtHuPt > 0) {
				double cprQtHuPt = Math.pow(prWeakFalsePositive, nQtHuPt);
				LOG.info("  nQtHuPt=" + nQtHuPt + " pr= " + cprQtHuPt);
				logp += Math.log(cprQtHuPt);
			}
			// P(qi=FALSE | hi=UNK, p(qi)=TRUE) = 1-(FP' + TRUEMISS')
			int nQfHuPt = nodesQfPtBM.andNot(nodesHtBM).andNotCardinality(nodesHfBM);
			if (nQfHuPt > 0) {
				double cprQfHuPt = Math.pow(1 - (prWeakFalsePositive + prWeakTrueMiss), nQfHuPt);
				LOG.info("  nQfHuPt=" + nQfHuPt + " pr= " + cprQfHuPt);
				logp += Math.log(cprQfHuPt);
			}
			// P(qi=UNK | hi=UNK, p(qi)=TRUE) = TRUEMISS' // > TRUEMISS (it's
			// more likely to miss a non-obvious absence than an obvious
			// absence)
			int nQuHuPt = nodesQuPtBM.andNot(nodesHtBM).andNotCardinality(nodesHfBM);
			if (nQuHuPt > 0) {
				double cprQuHuPt = Math.pow(prWeakTrueMiss, nQuHuPt);
				LOG.info("  nQuHuPt=" + nQuHuPt + " pr= " + cprQuHuPt);
				logp += Math.log(cprQuHuPt);
			}

			// ** U,F

			// P(qi=TRUE | hi=UNK, p(qi)=FALSE) = 0
			// P(qi=FALSE | hi=UNK, p(qi)=FALSE) = 1
			// P(qi=UNK | hi=UNK, p(qi)=FALSE) = 0

			// ** U,U

			// P(qi=TRUE | hi=UNK, p(qi)=UNK) = 0
			// P(qi=FALSE | hi=UNK, p(qi)=UNK) = 1 - TRUEMISS'
			int nQfHuPu = nodesQfPuBM.andNot(nodesHtBM).andNotCardinality(nodesHfBM);
			if (nQfHuPu > 0) {
				double cprQfHuPu = Math.pow(1 - prWeakTrueMiss, nQfHuPu);
				LOG.info("  nQfHuPu=" + nQfHuPu + " pr= " + cprQfHuPu);
				logp += Math.log(cprQfHuPu);
			}
			// P(qi=UNK | hi=UNK, p(qi)=UNK) = TRUEMISS'
			int nQuHuPu = nodesQuPuBM.andNot(nodesHtBM).andNotCardinality(nodesHfBM);
			if (nQuHuPu > 0) {
				double cprQuHuPu = Math.pow(prWeakTrueMiss, nQuHuPu);
				LOG.info("  nQuHuPu=" + nQuHuPu + " pr= " + cprQuHuPu);
				logp += Math.log(cprQuHuPu);
			}

			double p = Math.exp(logp);
			pvector[n] = p;
			indArr[n] = itemId;
			sumOfProbs += p;
			n++;
			LOG.info("logp for " + itemId + " = " + logp + "   sumOfLogProbs=" + sumOfProbs);
		}
		for (n = 0; n < pvector.length; n++) {
			double p = pvector[n] / sumOfProbs;
			String id = indArr[n];
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(id);
			mp.add(createMatch(id, label, p));
		}
		mp.sortMatches();
		return mp;
	}

}
