package org.monarchinitiative.owlsim.compute.mica.impl;

import org.monarchinitiative.owlsim.compute.mica.MICAStore;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

import org.apache.log4j.Logger;

/**
 * A an all by all cache for Most Informative Common Ancestors
 * 
 * Note that a single MICA is stored for each i,j pair. If there are multiple
 * MICAs with the same IC, an arbitrary one is stored.
 * 
 * This requires memory on the order of |C|^2
 * 
 * It would be possible to create a separate implementation that uses (|C|^2)/2, as
 * the grid is symmetric. This is left as a future extension for now. For now we treat
 * double the memory for what may be negligible increase in speed.
 * 
 * @author cjm
 *
 */
public class MICAStoreImpl implements MICAStore {

	private Logger LOG = Logger.getLogger(MICAStoreImpl.class);

	private BMKnowledgeBase knowledgeBase;
	private int[][] micaGrid;
	
	/**
	 * Create and populate a new MICAStore
	 * 
	 * @param knowledgeBase
	 * @return MICAStore
	 * @throws NoRootException
	 */
	public MICAStore create(BMKnowledgeBase knowledgeBase) throws NoRootException {
		return new MICAStoreImpl(knowledgeBase);
	}
	
	public MICAStoreImpl(BMKnowledgeBase knowledgeBase) throws NoRootException {
		super();
		this.knowledgeBase = knowledgeBase;
		populateGrid();
	}

	private void populateGrid() throws NoRootException {
		int n = knowledgeBase.getNumClassNodes();
		LOG.info("Pre-calculating grid of size "+n+" * "+n);
		micaGrid = new int[n][n];
		EWAHCompressedBitmap[] bmSuperClassesArr = new EWAHCompressedBitmap[n];
		for (int i=0; i<n; i++) {
			bmSuperClassesArr[i] = knowledgeBase.getSuperClassesBM(i);
		}
		for (int i=0; i<n; i++) {
			micaGrid[i][i] = i;  // reflexive MICA
			EWAHCompressedBitmap bmi = bmSuperClassesArr[i];
			for (int j=0; j<i; j++) {
				// BM guaranteed to be ordered descending by frequency
				IntIterator iter = bmi.and(bmSuperClassesArr[j]).intIterator();
				if (iter.hasNext()) {
					int a = iter.next();
					micaGrid[i][j] = a;
					micaGrid[j][i] = a; // MICA is symmetric
				}
				else {
					LOG.error("bmi="+bmi.getPositions());
					LOG.error("bmj="+bmSuperClassesArr[j].getPositions());
					throw new NoRootException(i, j, knowledgeBase.getClassId(i), knowledgeBase.getClassId(j));
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.monarchinitiative.owlsim.compute.mica.impl.MICAStore#getMICAIndex(int, int)
	 */
	@Override
	public int getMICAIndex(int i, int j) {
		// Assumption: symmetric grid is pre-populated on both sides
		return micaGrid[i][j];
	}
	
	@Deprecated
	public int getMICAIndexAsym(int i, int j) {
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

	
	/* (non-Javadoc)
	 * @see org.monarchinitiative.owlsim.compute.mica.impl.MICAStore#getMICAClass(int, int)
	 */
	@Override
	public String getMICAClass(int i, int j) {
		return knowledgeBase.getClassId( getMICAIndex(i,j) );
	}
	
	

}
