package org.monarchinitiative.owlsim.compute.mica.impl;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;
import org.apache.log4j.Logger;

/**
 * A an all by all cache for Most Informative Common Ancestors
 * 
 * Note that a single MICA is stored for each i,j pair. If there are multiple
 * MICAs with the same IC, an arbitrary one is stored
 * 
 * @author cjm
 *
 */
public class MICAStoreImpl {

	private Logger LOG = Logger.getLogger(MICAStoreImpl.class);

	private BMKnowledgeBase knowledgeBase;
	private int[][] micaGrid;
	
	public MICAStoreImpl(BMKnowledgeBase knowledgeBase) throws NoRootException {
		super();
		this.knowledgeBase = knowledgeBase;
		populateGrid();
	}

	private void populateGrid() throws NoRootException {
		int n = knowledgeBase.getClassIdsInSignature().size();
		micaGrid = new int[n][n];
		EWAHCompressedBitmap[] bmArr = new EWAHCompressedBitmap[n];
		for (int i=0; i<n; i++) {
			String id = knowledgeBase.getClassId(i);
			bmArr[i] = knowledgeBase.getSuperClassesBM(id);
		}
		for (int i=0; i<n; i++) {
			EWAHCompressedBitmap bmi = bmArr[i];
			for (int j=0; j<i; j++) {
				// BM guaranteed to be ordered descending by frequency
				IntIterator iter = bmi.and(bmArr[j]).intIterator();
				if (iter.hasNext()) {
					micaGrid[i][j] = iter.next();
				}
				else {
					LOG.error("bmi="+bmi.getPositions());
					LOG.error("bmj="+bmArr[j].getPositions());
					throw new NoRootException(i, j, knowledgeBase.getClassId(i), knowledgeBase.getClassId(j));
				}
			}
		}
	}
	
	/**
	 * @param i
	 * @param j
	 * @return index of MICA
	 */
	public int getMICAIndex(int i, int j) {
		if (j > i) {
			// grid is symmetric and only populated for i<j
			return micaGrid[j][i];
		}
		if (i != j) {
			// i<j
			return micaGrid[i][j];
		}
		// i==j : MICA(i,i) = i
		return i;

	}
	
	/**
	 * @param i
	 * @param j
	 * @return MICA class Id
	 */
	public String getMICA(int i, int j) {
		return knowledgeBase.getClassId( getMICAIndex(i,j) );
	}
	
	

}
