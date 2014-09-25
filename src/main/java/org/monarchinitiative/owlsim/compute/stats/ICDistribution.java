package org.monarchinitiative.owlsim.compute.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;

public class ICDistribution {

	private Logger LOG = Logger.getLogger(ICDistribution.class);
	private double samplingRate;
	private DescriptiveStatistics stats;
	private List<ICDistributionValue> distribution;
		
	public ICDistribution(double[] icData, double samplingRate) {
		stats = new DescriptiveStatistics();
		for (double d : icData) {
			stats.addValue(d);
		}
		this.samplingRate = samplingRate;		
		distribution = generateDistribution(stats.getSortedValues());		
	}
	
	public ICDistribution(DescriptiveStatistics stats, double samplingRate) {
		this.stats = stats;
		this.samplingRate = samplingRate;
		distribution = generateDistribution(stats.getSortedValues());		
	}

	private List<ICDistributionValue> generateDistribution(double[] sortedDistroValues) {
		double current = 0;
		int prevI = 0;

		List<ICDistributionValue> distribution = new ArrayList<ICDistributionValue>();
		while (current <= sortedDistroValues[sortedDistroValues.length - 1]) {
			double rate = current + samplingRate;
			
			int count = 0;
			for (int i = prevI ;i < sortedDistroValues.length ;i++) {
				if (sortedDistroValues[i] >= current && sortedDistroValues[i] < rate) {
					count++;
				} else {
					if (sortedDistroValues[i] >= rate) {
						prevI = i;
						break;
					}
				}
			}
			
			double perc = (double) count / sortedDistroValues.length;
			distribution.add(new ICDistributionValue(rate / 2, perc));
			current += samplingRate;
		}
		return distribution;
	}

	public List<ICDistributionValue> getDistribution() {
		return distribution;
	}
	
	public DescriptiveStatistics getDescriptiveStatistics() {
		return stats;
	}
	
	public String toString() {
		return "n="+stats.getN()+"; window="+samplingRate+"; dist="+distribution.toString();
	}
	
	/**
	 * Perform a one-sided {@code TestUtils.tTest} against the supplied reference
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public double tTest(ICDistribution reference) throws Exception {
		double p = 0.0;
		//should we be adding this instance's values into the reference before computing the mean?
		p = TestUtils.tTest(reference.getDescriptiveStatistics().getMean(), stats.getValues());
		return p;
	}
	
	/**
	 * Perform a one-sided {@code TestUtils.oneWayAnovaPValue} against the supplied reference
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public double oneWayAnovaPValue(ICDistribution reference) throws Exception {
		double p = 0.0;
		//add the current ic val distribution to the reference set
		List<double[]> sets = new ArrayList<double[]>();
		sets.add(stats.getValues());
		sets.add(reference.getDescriptiveStatistics().getValues());
		p = TestUtils.oneWayAnovaPValue(sets);
		return p;
	}
	
	/**
	 * Perform a one-sided {@code TestUtils.kolmogorovSmirnovStatistic} against the supplied reference
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public double kolmogorovSmirnovTest(ICDistribution reference) throws Exception {
		double p = 0.0;
		//add the current ic val distribution to the reference set
		p = TestUtils.kolmogorovSmirnovTest(stats.getValues(), reference.getDescriptiveStatistics().getValues());
		return p;
	}


}
