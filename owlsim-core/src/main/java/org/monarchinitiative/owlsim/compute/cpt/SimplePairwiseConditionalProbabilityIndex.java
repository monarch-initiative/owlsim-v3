package org.monarchinitiative.owlsim.compute.cpt;

import java.util.Map;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * Index for storing the value of Pr(C | D)
 * C and D need not stand in a relationship in the ontology
 * 
 * The interface is relatively low-level for speed. Entries are accessed using
 * the integer index of the child class C (clsIndex) and D
 * 
 * 
 * @author cjm
 *
 */
public interface SimplePairwiseConditionalProbabilityIndex {

	/**
	 * @param clsIndex - integer encoding C
	 * @param parentIndex - integer encoding D
	 * @return Pr(C | D)
	 */
	public Double getConditionalProbabilityChildIsOn(int clsIndex, int priorIndex);
	

	/**
	 * Called to set all CPs
	 * 
	 * @param kb
	 * @throws IncoherentStateException
	 */
	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException;

	
}
