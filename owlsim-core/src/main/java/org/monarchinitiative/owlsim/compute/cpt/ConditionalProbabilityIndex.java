package org.monarchinitiative.owlsim.compute.cpt;

import java.util.Map;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * Index for storing the value of Pr(C | P1=S1, P2=S2, ..., Pn=Sn),
 * where C is a child class, P1..Pn are parent classes, and S1..Sn are
 * the states of P1..Pn
 * 
 * 
 * The interface is relatively low-level for speed. Entries are accessed using
 * the integer index of the child class C (clsIndex). For the parents' states, a second index is
 * created, psi, an integer 0<=i<|S|^n, representing each possible state of each parent.
 * i.e. psi = 0 means P1=0,Pn=0,...,Pn=0.
 * 
 * 
 * @author cjm
 *
 */
public interface ConditionalProbabilityIndex {

	/**
	 * return Probability of C given P1=S1...Pn=Sn
	 * 
	 * @param clsIndex - integer encoding C
	 * @param parentsStatesIndex - integer encoding P1=S1...Pn=Sn
	 * @return Pr(C | P1=S1, P2=S2, ..., Pn=Sn)
	 */
	public Double getConditionalProbabilityChildIsOn(int clsIndex, int parentsStatesIndex);
	
	/**
	 * @param childClassIndex
	 * @param parentsStatesIndex
	 * @param numStates
	 * @param cp
	 * @throws IncoherentStateException
	 */
	//public void setConditionalProbabilityChildIsOn(int childClassIndex, int parentsStatesIndex, int numStates, 
	//		double cp) throws IncoherentStateException;
	
	/**
	 * Called to set all CPs
	 * 
	 * @param kb
	 * @throws IncoherentStateException
	 */
	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException;

	/**
	 * Returns the number of possible different state combinations for the parents of C
	 * 
	 * @param childClassIndex
	 * @return |S|^n
	 */
	public int getNumberOfParentStates(int childClassIndex);
	
	
	/**
	 * Each possible combination of P1=S1,...,Pn=Sn is encoded as an integer;
	 * this method maps from the integer back to a map of parents to states
	 * 
	 * @param clsIndex
	 * @param parentsState number between 0 and |S|^n
	 * @return map between class indices and states
	 */
	public Map<Integer, Character> getParentsToStateMapping(int clsIndex, int parentsState);

	
}
