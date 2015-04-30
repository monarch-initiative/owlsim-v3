package org.monarchinitiative.owlsim.compute.cpt.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * INCOMPLETE
 * 
 * An implementation of {@link ConditionalProbabilityIndex} with 3 states:
 * possible: on, unknown, off
 * 
 * Pr(Top=on) = 1
 * 
 * 
 *  
 * 
 * @author cjm
 *
 */
public class ThreeStateConditionalProbabilityIndex 
extends AbstractConditionalProbabilityIndex
implements ConditionalProbabilityIndex {

	private Logger LOG = Logger.getLogger(ThreeStateConditionalProbabilityIndex.class);
	BMKnowledgeBase kb;
	private char UNKNOWN = 'u';
	private char ON = 't';
	private char OFF = 'f';
	private char[] STATES = {'f', 'u','t'};



	/**
	 * @param size
	 */
	public ThreeStateConditionalProbabilityIndex(int size) {
		super(size);
	}
	/**
	 * @param kb
	 */
	public ThreeStateConditionalProbabilityIndex(BMKnowledgeBase kb) {
		super(kb);
	}


	/**
	 * @param kb
	 * @return CPI
	 */
	public static ConditionalProbabilityIndex create(BMKnowledgeBase kb) {
		return new ThreeStateConditionalProbabilityIndex(kb.getNumClassNodes());
	}

	/**
	 * @param size
	 * @return CPI
	 */
	public static ConditionalProbabilityIndex create(int size) {
		return new ThreeStateConditionalProbabilityIndex(size);
	}

	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException {
		this.kb = kb;
		int totalInds = kb.getIndividualsBM(kb.getRootIndex()).cardinality();

		LOG.info("Calculating all CPTs...");
		for (String cid : kb.getClassIdsInSignature()) {
			LOG.debug("   Calculating CPT for "+cid);
			int cix = kb.getClassIndex(cid);
			int numIndividualsForChild = kb.getIndividualsBM(cix).cardinality();

			EWAHCompressedBitmap sups = kb.getDirectSuperClassesBM(cid);
			List<Integer> pixs = sups.getPositions(); // ASSUME STABLE ORDERING
			int numParents = pixs.size();

			// assume two states for now: will be extendable to yes, no, unknown
			int numStates = (int) Math.pow(2, numParents);

			if (numParents == 0) {
				LOG.debug("Root: "+cid);
				continue;
			}
			if (parentStateMapByIndex[cix] == null)
				parentStateMapByIndex[cix] = new Map[numStates];

			for (int parentSetStateIx=0; parentSetStateIx<numStates; parentSetStateIx++) {
				Map<Integer, Character> parentStateMap = 
						calculateParentStateMapForIndex(parentSetStateIx, pixs, STATES);

				EWAHCompressedBitmap allIndsForOnParentsBM = null;
				int numOff = 0;
				for (int pix : parentStateMap.keySet()) {
					char stateOfParent = parentStateMap.get(pix);
					if (stateOfParent == ON || stateOfParent == OFF) {
						// treat OFF as ON*Pr(FN)
						EWAHCompressedBitmap indsBM = kb.getIndividualsBM(pix);
						if (allIndsForOnParentsBM == null)
							allIndsForOnParentsBM = indsBM;
						else
							allIndsForOnParentsBM = allIndsForOnParentsBM.and(indsBM);
						
						if (stateOfParent == OFF) {
							// if parent is OFF, then logically the child must be OFF,
							// regardless of other parents. However, we also
							// allow for the possibility of false negatives.
							//
							// also consider the scenario that as we get more specific,
							// we may be less sure of our high-level negation
							// TODO
							numOff++;
						}
					}
					else {
						// UNKNOWN
					}


				}

				int numIndividualsForOnParents = 
						allIndsForOnParentsBM == null ? 
								totalInds : allIndsForOnParentsBM.cardinality();
				double conditionalProbability = 
						numIndividualsForChild / (double) numIndividualsForOnParents;
				if (numOff > 0) {
					// hardcode probability of false negative
					conditionalProbability *= Math.pow(0.01, numOff);
				}
				LOG.debug("  CP for "+parentStateMap+" = "+numIndividualsForChild+"/"+numIndividualsForOnParents+" = "+conditionalProbability);
				setConditionalProbabilityTableRow(cix, parentSetStateIx, 
						numStates, conditionalProbability);
				parentStateMapByIndex[cix][parentSetStateIx] = parentStateMap;
			}

		}
		LOG.info("DONE Calculating all CPTs");

	}


}
