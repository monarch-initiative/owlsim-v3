package org.monarchinitiative.owlsim.compute.mica.impl;

import java.util.Set;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator;
import org.monarchinitiative.owlsim.compute.stats.ICStatsCalculator;
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
	private ICStatsCalculator icStatsCalculator;
	private final BMKnowledgeBase knowledgeBase;
	

	/**
	 * @param knowledgeBase
	 */
	@Inject
	public MostInformativeCommonAncestorCalculatorImpl(
			BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
		this.icStatsCalculator = new ICStatsCalculator(knowledgeBase);
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

		// bits are ordered
		if (bitIterator.hasNext()) {
			int bit = bitIterator.next();
			String classId = knowledgeBase.getClassId(bit);
			return new ClassInformationContentPair(classId,
					icStatsCalculator.getInformationContentByClassIndex(bit));
		}
		else {
			return null;
		}
	}

    @Override
    public ClassInformationContentPair getMostInformativeCommonAncestorWithIC(
            Set<String> queryClassIds, Set<String> targetClassIds) {
        return getMostInformativeCommonAncestorWithIC(knowledgeBase.getSuperClassesBM(queryClassIds),
                knowledgeBase.getSuperClassesBM(targetClassIds));
    }
	
	
	
}
