package org.monarchinitiative.owlsim.compute.cpt.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.ConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * INCOMPLETE
 * 
 * An implementation of {@link ConditionalProbabilityIndex} with 3 states:
 * on (true), unknown, off (false)
 * 
 * The calculation for the on & unknown state combos is the same as for
 *  {@link TwoStateConditionalProbabilityIndex}; if any parent is off then
 *  the child is off.
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
	private char[] STATES = {OFF, UNKNOWN, ON};

	//Double[][] conditionalProbabilityOnByChildParentState; // Pr(C=on|ParentsStateCombo)
	//Double[][] conditionalProbabilityOffByChildParentState; // Pr(C=on|ParentsStateCombo)
	NodeProbabilities[][] conditionalProbabilityDistributionByChildParentState; // Pr(C=on|ParentsStateCombo)



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
	public static ThreeStateConditionalProbabilityIndex create(BMKnowledgeBase kb) {
		return new ThreeStateConditionalProbabilityIndex(kb.getNumClassNodes());
	}

	/**
	 * @param size
	 * @return CPI
	 */
	public static ConditionalProbabilityIndex create(int size) {
		return new ThreeStateConditionalProbabilityIndex(size);
	}

	protected void init(int size) {
		//conditionalProbabilityOnByChildParentState = new Double[size][];
		//conditionalProbabilityOffByChildParentState = new Double[size][];
		conditionalProbabilityDistributionByChildParentState = new NodeProbabilities[size][];
		parentStateMapByIndex = 
				(Map<Integer,Character>[][])new Map[size][];
	}

	public NodeProbabilities getConditionalProbabilityDistribution(int clsIndex, int parentsState) {
		return conditionalProbabilityDistributionByChildParentState[clsIndex][parentsState];
	}
	public void setConditionalProbabilityDistribution(int childClassIndex, int parentsState, int numStates, NodeProbabilities cp) throws IncoherentStateException {
		if (conditionalProbabilityDistributionByChildParentState[childClassIndex] == null)
			conditionalProbabilityDistributionByChildParentState[childClassIndex] = new NodeProbabilities[numStates];
		conditionalProbabilityDistributionByChildParentState[childClassIndex][parentsState] = cp;
	}
	
	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException {
		this.kb = kb;
		int totalInds = kb.getIndividualsBM(kb.getRootIndex()).cardinality();

		Set<String> individualsWithNegatedTypes = new HashSet<String>();
		for (String i : kb.getIndividualIdsInSignature()) {
			if (kb.getDirectNegatedTypesBM(i).cardinality() > 0) {
				individualsWithNegatedTypes.add(i);
			}
		}
		long t1 = System.currentTimeMillis();
		LOG.info("Calculating all CPTs... Time: "+t1);
		for (String cid : kb.getClassIdsInSignature()) {
			LOG.debug("   Calculating CPT for "+cid);
			int cix = kb.getClassIndex(cid);
			int numIndividualsForChild = kb.getIndividualsBM(cix).cardinality();

			EWAHCompressedBitmap sups = kb.getDirectSuperClassesBM(cid);
			List<Integer> pixs = sups.getPositions(); // ASSUME STABLE ORDERING
			int numParents = pixs.size();

			// assume two states for now: will be extendable to yes, no, unknown
			int numStates = (int) Math.pow(3, numParents);

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
				boolean hasParentThatIsOff = false;
				for (int pix : parentStateMap.keySet()) {
					char stateOfParent = parentStateMap.get(pix);
					if (stateOfParent == ON) {
						// treat OFF as ON*Pr(FN)
						EWAHCompressedBitmap indsBM = kb.getIndividualsBM(pix);
						if (allIndsForOnParentsBM == null)
							allIndsForOnParentsBM = indsBM;
						else
							allIndsForOnParentsBM = allIndsForOnParentsBM.and(indsBM);
					}
					else if (stateOfParent == OFF) {
						// if parent is OFF, then logically the child must be OFF,
						// regardless of other parents.
						hasParentThatIsOff = true;
						break;
					}
					else {
						// UNKNOWN
					}


				}

				double conditionalProbabilityChildIsOn;
				double conditionalProbabilityChildIsOff;
				if (hasParentThatIsOff) {
					conditionalProbabilityChildIsOn = 0;
					conditionalProbabilityChildIsOff = 1.0;
				}
				else {
					conditionalProbabilityChildIsOff = 0.0; // TODO

					int numIndividualsForOnParents = 
							allIndsForOnParentsBM == null ? 
									totalInds : allIndsForOnParentsBM.cardinality();

					int numOff = 0;
					EWAHCompressedBitmap cSupersBM = kb.getSuperClassesBM(cix);
					if (allIndsForOnParentsBM != null) {
						// todo: check efficiency
						for (int jix : allIndsForOnParentsBM.getPositions()) {
							String j = kb.getIndividualId(jix);
							if (kb.getDirectNegatedTypesBM(j).andCardinality(cSupersBM) > 0) {
								numOff++;
							}
						}
						//LOG.info("cp(OFF)="+numOff + " / "+numIndividualsForOnParents);
						conditionalProbabilityChildIsOff = numOff / (double) numIndividualsForOnParents;
					}
					else {
						// TODO: make this efficient; use a getDirectIndividuals method
						for (String j : individualsWithNegatedTypes) {
							if (kb.getDirectNegatedTypesBM(j).andCardinality(cSupersBM) > 0) {
								numOff++;
							}
						}
						//LOG.info("cp(OFF)="+numOff + " / "+numIndividualsForOnParents);
						conditionalProbabilityChildIsOff = numOff / (double) totalInds;
						
					}
					conditionalProbabilityChildIsOn = 
							numIndividualsForChild / (double) numIndividualsForOnParents;
					//LOG.info("  CP for "+parentStateMap+" = "+numIndividualsForChild+"/"+numIndividualsForOnParents+" = "+conditionalProbabilityChildIsOn);
					if (conditionalProbabilityChildIsOff + conditionalProbabilityChildIsOn > 1.0) {
						LOG.error("OOPS:"+conditionalProbabilityChildIsOff + " + " + conditionalProbabilityChildIsOn);
					}
				}
				
				NodeProbabilities prd = new NodeProbabilities(conditionalProbabilityChildIsOn, 
						conditionalProbabilityChildIsOff);
				//LOG.info("Setting PRD="+prd+" for "+cid+" PComboIx="+parentSetStateIx);
				setConditionalProbabilityDistribution(cix, parentSetStateIx, numStates, prd);
				
				parentStateMapByIndex[cix][parentSetStateIx] = parentStateMap;
			}

		}
		long t2 = System.currentTimeMillis();
		long td = t2-t1;
		LOG.info("DONE Calculating all CPTs. Time: "+td);

	}
	@Override
	public Double getConditionalProbabilityChildIsOn(int clsIndex,
			int parentsStatesIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
