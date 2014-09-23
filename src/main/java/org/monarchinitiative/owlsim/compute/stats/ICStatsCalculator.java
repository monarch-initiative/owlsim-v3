package org.monarchinitiative.owlsim.compute.stats;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.googlecode.javaewah.IntIterator;

public class ICStatsCalculator {

	private BMKnowledgeBase knowledgeBase;
	private int[] frequencyByClassIndex;
	private Double[] informationContentByClassIndex;
	private DescriptiveStatistics[] iDescriptiveStatistics;
	private SetDescriptiveStatistics dsKBIndSummary;		

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
	 * For all individuals in the knowledgebase, iterate and add ICs for
	 * profile summary statistical calculations.
	 */
	public void calculateKBSummary() {
		Set<String> inds = knowledgeBase.getIndividualIdsInSignature();
		dsKBIndSummary = new SetDescriptiveStatistics();
		this.iDescriptiveStatistics= new DescriptiveStatistics[inds.size()];
		
		//iterate over all individuals, and calculate their info profile
		//add it to an array for easy access.
		for (String individualId : inds) {
			int ibit = knowledgeBase.getIndividualIndex(individualId);
			EWAHCompressedBitmap attsBM = knowledgeBase.getTypesBM(individualId);	
			DescriptiveStatistics ds = new DescriptiveStatistics();
			ds = getICStatsForBM(attsBM);
			//add the summary to the whole 
			dsKBIndSummary.addSummary(ds);
			iDescriptiveStatistics[ibit] = ds;
		}		
	}
		
	public SetDescriptiveStatistics makeSetDescriptiveStatisticsForIndividuals(Set<String> individualIDs) {
		SetDescriptiveStatistics setSummary = new SetDescriptiveStatistics();		
		if (this.iDescriptiveStatistics == null) {
			calculateKBSummary();
		}
		for (String individualId : individualIDs) {
			int ibit = knowledgeBase.getIndividualIndex(individualId);
			DescriptiveStatistics ds = iDescriptiveStatistics[ibit];			
			//add the summary to the whole 
			setSummary.addSummary(ds);
		}	
		return setSummary;
	}
	
	public DescriptiveStatistics getICStatsForBM(EWAHCompressedBitmap attsBM) {
		DescriptiveStatistics ds = new DescriptiveStatistics();
		IntIterator bitIterator = attsBM.intIterator();
		if (bitIterator.hasNext()) {
			int cbit = bitIterator.next();
			Double ic = this.getInformationContentByClassIndex(cbit);
			ds.addValue(ic);
		}	
		return ds;
	}
		
	public DescriptiveStatistics getICStatsForIndividual(String individualId) {
		int ibit = knowledgeBase.getIndividualIndex(individualId);
		return iDescriptiveStatistics[ibit];
	}
	
	public Double getInformationContentByClassId(String classId) {
		int bit = knowledgeBase.getClassIndex(classId);
		return getInformationContentByClassIndex(bit);
	}
	
	public Double getInformationContentByClassIndex(int bit) {
		return informationContentByClassIndex[bit];
	}
	
	//not sure if we need this accessible by other methods
//	public Double[] getInformationContentByClassIndexArray() {
//		return informationContentByClassIndex;
//	}

	/* 
	 * A convenience class to convert a Double array list to a double[]
	 * */
	public static double[] convertDoubles(List<Double> doubles)
	{
	    double[] ret = new double[doubles.size()];
	    Iterator<Double> iterator = doubles.iterator();
	    int i = 0;
	    while(iterator.hasNext())
	    {
	        ret[i] = iterator.next().doubleValue();
	        i++;
	    }
	    return ret;
	}
	
}
