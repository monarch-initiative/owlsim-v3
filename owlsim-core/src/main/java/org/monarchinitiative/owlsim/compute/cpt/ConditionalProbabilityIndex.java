package org.monarchinitiative.owlsim.compute.cpt;

import java.util.Map;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * Index for storing the value of Pr(C | P1=S1, P2=S2, ..., Pn=Sn)
 * 
 * Currently only states are {1,0}
 * 
 * The interface is relatively low-level for speed. Entries are accessed using
 * the integer index of the child class. For the parents states, a second index is
 * created, an integer 0<=i<|S|^n, representing each possible state of each parent
 * 
 * @author cjm
 *
 */
public interface ConditionalProbabilityIndex {

	public Double getConditionalProbability(int clsIndex, int parentsStatesIndex);
	public void setConditionalProbabilityTableRow(int childClassIndex, int parentsStatesIndex, int numStates, 
			double cp) throws IncoherentStateException;
	
	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException;
	public int getNumberOfParentStates(int childClassIndex);
	public Map<Integer, Character> getParentsToStateMapping(int clsIndex, int parentsState);

	
}
