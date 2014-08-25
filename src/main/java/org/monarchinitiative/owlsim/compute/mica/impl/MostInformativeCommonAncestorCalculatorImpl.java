package org.monarchinitiative.owlsim.compute.mica.impl;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

/**
 * An implementation of MostInformativeCommonAncestorCalculator that exploits
 * properties of the construction of bitmap vectors:
 * 
 * the BM is guaranteed to be ordered such that the first integer is either least frequent
 * or joint least frequent.
 * 
 * @author cjm
 *
 */
public class MostInformativeCommonAncestorCalculatorImpl implements MostInformativeCommonAncestorCalculator {

	//private Logger LOG = Logger.getLogger(MaximumInformationContentSimilarityProfileMatcher.class);

	
	private int[] frequencyByClassIndex;
	private Double[] informationContentByClassIndex;
	private final BMKnowledgeBase knowledgeBase;
	

	/**
	 * @param knowledgeBase
	 */
	@Inject
	public MostInformativeCommonAncestorCalculatorImpl(
			BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
		frequencyByClassIndex = knowledgeBase.getClassFrequencyArray();
		int numInds = knowledgeBase.getIndividualIdsInSignature().size();
		informationContentByClassIndex = new Double[frequencyByClassIndex.length];
		for (int i=0; i<frequencyByClassIndex.length; i++) {
			int freq = frequencyByClassIndex[i];
			informationContentByClassIndex[i] = 
					freq == 0 ?
							null :
								-Math.log(freq / (double)numInds);
							
		}
	}

	public int getFrequencyOfMostInformativeCommonAncestor(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM) {
		EWAHCompressedBitmap commonSubsumersBM = queryProfileBM.and(targetProfileBM);
		IntIterator bitIterator = commonSubsumersBM.intIterator();
		if (bitIterator.hasNext()) {
			int bit = bitIterator.next();
			return frequencyByClassIndex[bit];
		}
		else {
			return -1;
		}
	}

	public ClassFrequencyPair getMostInformativeCommonAncestorWithFrequency(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM) {
		
		EWAHCompressedBitmap commonSubsumersBM = queryProfileBM.and(targetProfileBM);
		IntIterator bitIterator = commonSubsumersBM.intIterator();
		if (bitIterator.hasNext()) {
			int bit = bitIterator.next();
			String classId = knowledgeBase.getClassId(bit);
			return new ClassFrequencyPair(classId,
					frequencyByClassIndex[bit],
					bit);
		}
		else {
			return null;
		}
	}
	
	public ClassInformationContentPair getMostInformativeCommonAncestorWithIC(EWAHCompressedBitmap queryProfileBM, EWAHCompressedBitmap targetProfileBM) {
		
		EWAHCompressedBitmap commonSubsumersBM = queryProfileBM.and(targetProfileBM);
		IntIterator bitIterator = commonSubsumersBM.intIterator();
		if (bitIterator.hasNext()) {
			int bit = bitIterator.next();
			String classId = knowledgeBase.getClassId(bit);
			return new ClassInformationContentPair(classId,
					informationContentByClassIndex[bit]);
		}
		else {
			return null;
		}
	}
}
