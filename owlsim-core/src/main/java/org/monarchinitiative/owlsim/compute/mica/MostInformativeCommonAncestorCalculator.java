package org.monarchinitiative.owlsim.compute.mica;

import java.util.Set;

import org.monarchinitiative.owlsim.compute.mica.impl.MostInformativeCommonAncestorCalculatorImpl;

import com.google.inject.ImplementedBy;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Calculator for Most Informative Common Ancestor (MICA), where MICA
 * is defined as the set of common ancestors with the highest information content
 * (lowest frequency)
 * 
 * @author cjm
 *
 */
@ImplementedBy(MostInformativeCommonAncestorCalculatorImpl.class)
public interface MostInformativeCommonAncestorCalculator {

	/**
	 * Representation for a (classId, frequency) pair
	 * 
	 * the index of the classId is also represented
	 * 
	 * @author cjm
	 *
	 */
	public class ClassFrequencyPair {
		/**
		 * CURIE of class
		 */
		public String classId;
		/**
		 * Number of times class is used
		 */
		public int frequency;
		/**
		 * Index of class
		 */
		public int classIndex;
		
		/**
		 * @param classId
		 * @param frequency
		 * @param classIndex
		 */
		public ClassFrequencyPair(String classId, int frequency, int classIndex) {
			super();
			this.classId = classId;
			this.frequency = frequency;
			this.classIndex = classIndex;
		}


	}
	
	/**
	 * Representation for a (classId, IC) pair
	 * 
	 * 
	 * @author cjm
	 *
	 */
	public class ClassInformationContentPair {
		/**
		 * CURIE
		 */
		public String classId;
		/**
		 * -log(freq(C)/corpusSize)
		 */
		public Double ic;
		
		/**
		 * @param classId
		 * @param ic
		 */
		public ClassInformationContentPair(String classId, Double ic) {
			super();
			this.classId = classId;
			this.ic = ic;
		}

		public String toString() {
			return classId + " IC="+ic;
		}
	}


	// All methods exploit:
	// the BM is guaranteed to be ordered such that the first integer is either least frequent
	// or joint least frequent

	/**
	 * @param queryProfileBM
	 * @param targetProfileBM
	 * @return frequency of MICA
	 */
	public int getFrequencyOfMostInformativeCommonAncestor(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM);

	/**
     * Note closure assumed for BM
     * 
	 * @param queryProfileBM
	 * @param targetProfileBM
	 * @return frequency of MICA plus MICA
	 */
	public ClassFrequencyPair getMostInformativeCommonAncestorWithFrequency(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM);
	
	/**
	 * Note closure assumed for BM
	 * 
	 * @param queryProfileBM
	 * @param targetProfileBM
	 * @return frequency of MICA plus IC
	 */
    public ClassInformationContentPair getMostInformativeCommonAncestorWithIC(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM);

    /**
     * @param queryClassIds
     * @param targetClassIds
     * @return frequency of MICA plus IC
     */
    public ClassInformationContentPair getMostInformativeCommonAncestorWithIC(
            Set<String> queryClassIds,
            Set<String> targetClassIds) ;
}
