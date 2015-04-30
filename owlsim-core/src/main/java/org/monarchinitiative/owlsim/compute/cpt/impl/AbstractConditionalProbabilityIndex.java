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
 * 
 * @author cjm
 *
 */
public abstract class AbstractConditionalProbabilityIndex implements ConditionalProbabilityIndex {

	private Logger LOG = Logger.getLogger(AbstractConditionalProbabilityIndex.class);
	BMKnowledgeBase kb;
	

	Double[][] conditionalProbabilityByChildParentState;
	Map<Integer,Character>[][] parentStateMapByIndex;
	
	/**
	 * @param size
	 */
	public AbstractConditionalProbabilityIndex(int size) {
		super();
		init(size);
	}
	/**
	 * @param kb
	 */
	public AbstractConditionalProbabilityIndex(BMKnowledgeBase kb) {
		super();
		this.kb = kb;
		init(kb.getNumClassNodes());
	}
	private void init(int size) {
		conditionalProbabilityByChildParentState = new Double[size][];
		parentStateMapByIndex = 
				 (Map<Integer,Character>[][])new Map[size][];
	}
	
	public Double getConditionalProbability(int clsIndex, int parentsState) {
		return conditionalProbabilityByChildParentState[clsIndex][parentsState];
	}
	public Map<Integer, Character> getParentsToStateMapping(int clsIndex, int parentsState) {
		return parentStateMapByIndex[clsIndex][parentsState];
	}
	public int getNumberOfParentStates(int clsIndex) {
		return parentStateMapByIndex[clsIndex] == null ?
				0 : parentStateMapByIndex[clsIndex].length;
	}
	
	public void setConditionalProbabilityTableRow(int childClassIndex, int parentsState, int numStates, double cp) throws IncoherentStateException {
		if (conditionalProbabilityByChildParentState[childClassIndex] == null)
			conditionalProbabilityByChildParentState[childClassIndex] = new Double[numStates];
		if (cp < 0.0) {
			throw new IncoherentStateException("Pr(C|Parents)="+cp);
		}
		if (cp > 1.0) {
			throw new IncoherentStateException("Pr(C|Parents)="+cp);
		}
		conditionalProbabilityByChildParentState[childClassIndex][parentsState] = cp;
	}

	public abstract void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException;

	protected Map<Integer,Character> calculateParentStateMapForIndex(int parentState, 
			List<Integer> parentIxs, char[] STATES) {
		int numStateTypes = STATES.length;
		Map<Integer,Character> parentStateMap = new HashMap<Integer,Character>();
		for (int i=0; i<parentIxs.size(); i++) {
			int mod = parentState % numStateTypes;
			Integer p = parentIxs.get(i);
			parentStateMap.put(p, STATES[mod]);
			parentState = parentState / numStateTypes;
		}
		
		return parentStateMap;
	}

}
