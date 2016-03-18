package org.monarchinitiative.owlsim.compute.cpt.impl;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.cpt.SimplePairwiseConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * 
 * Note: currently only used by 3-state implementation
 * 
 * @author cjm
 *
 */
public class DefaultSimplePairwiseConditionalProbabilityIndex implements SimplePairwiseConditionalProbabilityIndex {

	private Logger LOG = Logger.getLogger(DefaultSimplePairwiseConditionalProbabilityIndex.class);
	BMKnowledgeBase kb;
	final short SCALE_FACTOR = 16384;
	private int size;

	private short[][] cpIndex;

	/**
	 * @param size
	 */
	public DefaultSimplePairwiseConditionalProbabilityIndex(int size) {
		super();
		init(size);
	}
	/**
	 * @param kb
	 */
	public DefaultSimplePairwiseConditionalProbabilityIndex(BMKnowledgeBase kb) {
		super();
		this.kb = kb;
		init(kb.getNumClassNodes());
	}
	/**
	 * @param kb
	 * @return CPI
	 */
	public static SimplePairwiseConditionalProbabilityIndex create(BMKnowledgeBase kb) {
		return new DefaultSimplePairwiseConditionalProbabilityIndex(kb.getNumClassNodes());
	}

	protected  void init(int size) {
		cpIndex = new short[size][size];
		this.size = size;
	}

	public void calculateConditionalProbabilities(BMKnowledgeBase kb) throws IncoherentStateException {
		int[] na = kb.getIndividualCountPerClassArray();
		LOG.info("SIZE="+size);
		for (int i=0; i<size; i++) {
			int numC = na[i];
			LOG.info(i+" numC="+numC);
			if (numC == 0) {
				for (int j=0; j<size; j++) {
					cpIndex[i][j] = 0;
				}
			}
			else {
				for (int j=0; j<size; j++) {
					int numP = na[j];
					if (numP == 0) {
						cpIndex[i][j] = 0;
					}
					else {
						int numCandP = kb.getIndividualsBM(i).andCardinality(kb.getIndividualsBM(j));
						cpIndex[i][j] = (short) ((SCALE_FACTOR * numCandP) / numP);
					}
				}
			}
		}
	}
	
	@Override
	public Double getConditionalProbabilityChildIsOn(int clsIndex,
			int priorIndex) {
		return  ((double)cpIndex[clsIndex][priorIndex]) / SCALE_FACTOR;
	}
	/**
	 * @return the cpIndex
	 */
	public short[][] getCpIndex() {
		return cpIndex;
	}



}
