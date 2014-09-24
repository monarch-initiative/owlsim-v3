package org.monarchinitiative.owlsim.compute.stats;

import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Various calculations related to information content of the annotations in the knowledgeBase
 * including generating the information content (IC) scores in general, and summary calculations based on
 * those scores.
 * 
 * These are independent of the similarity calculations used.
 * 
 * @author nlw
 *
 */
public class ICStatsCalculator {

	private Logger LOG = Logger.getLogger(ICStatsCalculator.class);
	private BMKnowledgeBase knowledgeBase;
	private int[] frequencyByClassIndex;
	private Double[] informationContentByClassIndex;
	private DescriptiveStatistics[] iDescriptiveStatistics;
	private SetDescriptiveStatistics dsKBIndSummary;		
	

	/**
	 * The constructor will populate an array of information 
	 * content (IC) scores per class index, given the frequencies 
	 * per class index in the supplied knowledgeBase.
	 * IC is defined as the negative log of the probability
	 * for a given class used in an annotation.
	 * @param kb
	 */
	public ICStatsCalculator(BMKnowledgeBase kb) {
		super();
		knowledgeBase = kb;		

		//populate scores
		frequencyByClassIndex = knowledgeBase.getIndividualCountPerClassArray();
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

	/**
	 * For all individuals in the knowledgeBase, populate an array of
	 * DescriptiveStatistics.  These are also added to {@code dsKBIndSummary}, 
	 * which provides a summary of the distribution of IC stats over the 
	 * entire knowledgeBase.
	 */
	public void calculateICSummary() {
		Set<String> inds = knowledgeBase.getIndividualIdsInSignature();
		dsKBIndSummary = new SetDescriptiveStatistics();
		this.iDescriptiveStatistics= new DescriptiveStatistics[inds.size()];

		//iterate over all individuals, and calculate their info profile
		//add it to an array for easy access.
		for (String individualId : inds) {
			int ibit = knowledgeBase.getIndividualIndex(individualId);
			EWAHCompressedBitmap attsBM = knowledgeBase.getDirectTypesBM(individualId);
			DescriptiveStatistics ds = new DescriptiveStatistics();
			ds = getICStatsForBM(attsBM);
//			LOG.info(individualId+": "+ds.toString());

			//add the summary to the whole 
			dsKBIndSummary.addSummary(ds);
			iDescriptiveStatistics[ibit] = ds;
		}		
	}

	/**
	 * Given a set of individual ids, create a {@code SetDescriptiveStatistics} object
	 * for that set.  This will allow comparisons of specified subsets of the 
	 * knowledgeBase to be compared to one another or to the whole background. 
	 * @param individualIDs
	 * @return
	 */
	public SetDescriptiveStatistics getSetDescriptiveStatisticsForIndividuals(Set<String> individualIDs) {
		SetDescriptiveStatistics setSummary = new SetDescriptiveStatistics();		

		if (this.iDescriptiveStatistics == null) {
			calculateICSummary();
		}
		for (String individualId : individualIDs) {
			int ibit = knowledgeBase.getIndividualIndex(individualId);
			DescriptiveStatistics ds = iDescriptiveStatistics[ibit];			
			setSummary.addSummary(ds);
		}	
		return setSummary;
	}

	/**
	 * Given a bitmap vector of classes, generate IC-based DescriptiveStatistics.  Can be used
	 * alone or added to a set.
	 * 
	 * @param attsBM
	 * @return
	 */
	public DescriptiveStatistics getICStatsForBM(EWAHCompressedBitmap attsBM) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		for (int bit : attsBM) {
			Double ic = this.getInformationContentByClassIndex(bit);
			ds.addValue(ic);
		}
		return ds;
	}

	/**
	 * Retrieve the DescriptiveStatistics for the supplied individual (id).
	 * @param individualId
	 * @return
	 */
	public DescriptiveStatistics getICStatsForIndividual(String individualId) {
		int ibit = knowledgeBase.getIndividualIndex(individualId);
		return getICStatsForIndividual(ibit);
	}
	
	/**
	 * Retrieve the DescriptiveStatistics for the supplied individual (bit).
	 * @param ibit
	 * @return
	 */
	public DescriptiveStatistics getICStatsForIndividual(int ibit) {
		return iDescriptiveStatistics[ibit];
	}

	/**
	 * Retrieve the IC value for a class by id.
	 * @param classId
	 * @return
	 */
	public Double getInformationContentByClassId(String classId) {
		int cbit = knowledgeBase.getClassIndex(classId);
		return getInformationContentByClassIndex(cbit);
	}

	/**
	 * Retrieve the IC value for a class by bit.
	 * @param cbit
	 * @return
	 */
	public Double getInformationContentByClassIndex(int cbit) {
		return informationContentByClassIndex[cbit];
	}
	
	public String toString() {
		return dsKBIndSummary.toString();
	}
	
	public SetDescriptiveStatistics getICSummaryForAllIndividuals() {
		return dsKBIndSummary;
	}


}
