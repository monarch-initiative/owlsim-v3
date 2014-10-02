package org.monarchinitiative.owlsim.compute.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;

public class ICDistribution {

	private Logger LOG = Logger.getLogger(ICDistribution.class);
	private double samplingRate;
	private List<ICDistributionValue> distribution;
	
	//not sure if the original stats should be saved with this object
	//convenient, but perhaps waste of space?
	private DescriptiveStatistics stats;
	
	/**
	 * The constructor populates the DescriptiveStatistics, which are then used
	 * for a quick access to the sorted elements and basic stats. The 'stats'
	 * member may be removed in the near future and replaced with a linear
	 * sort on the provided ic values.
	 * 
	 * The samplingRate represents the granularity of the bins to be created
	 * when generating the frequency values and the distribution points.
	 */
	public ICDistribution(double[] icData, double samplingRate) {
		stats = new DescriptiveStatistics();
		for (double d : icData) {
			stats.addValue(d);
		}
		this.samplingRate = samplingRate;		
		distribution = generateDistribution(stats.getSortedValues());		
	}
	
	/**
	 * Same as above - but with a given 'stats' object.
	 */
	public ICDistribution(DescriptiveStatistics stats, double samplingRate) {
		this.stats = stats;
		this.samplingRate = samplingRate;
		distribution = generateDistribution(stats.getSortedValues());		
	}

	/**
	 * Generates the distribution for the given set of sorted IC values by using the
	 * samplingRate to create bins and to compute the associated frequencies.
	 * The distribution is returned as a series of <x,y> coordinates encapsulated in
	 * {@code ICDistributionValue} objects.
	 */
	private List<ICDistributionValue> generateDistribution(double[] sortedDistroValues) {
		double current = 0;
		int prevI = 0;

		List<ICDistributionValue> distribution = new ArrayList<ICDistributionValue>();
		while ((sortedDistroValues.length > 0) && (current <= sortedDistroValues[sortedDistroValues.length - 1])) {
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
	
	//Comment: Not sure we need this
	public DescriptiveStatistics getDescriptiveStatistics() {
		return stats;
	}
	
	public String toString() {
		return "n="+stats.getN()+"; window="+samplingRate+"; dist="+distribution.toString();
	}
	
	/**
	 * Perform a one-sided {@code TestUtils.tTest} against the supplied reference.  If
	 * the tTest is unable to be performed due to insufficient values (length < 2),
	 * -1 will be returned.
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public double tTest(ICDistribution reference) throws Exception {
		double p = 0.0;
		//should we be adding this instance's values into the reference before computing the mean?
		if (stats.getValues().length < 2) {
			p = -1;
		} else {
			p = TestUtils.tTest(reference.getDescriptiveStatistics().getMean(), stats.getValues());
		}
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

	/**
	 * Returns the {@code Variance} of the current distribution against the mean of a supplied reference
	 * @param reference
	 * @return
	 * @throws Exception
	 */
	public double variance(ICDistribution reference) throws Exception {
		return new Variance().evaluate(stats.getValues(), reference.getDescriptiveStatistics().getMean());
	}
}
