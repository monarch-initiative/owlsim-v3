package org.monarchinitiative.owlsim.compute.stats;

import org.apache.commons.math.stat.descriptive.*;

/**
 * Can be used to store scores for collections/sets.
 * For example, these could be used to store collections of IC per individual, or counts of classes per individual,
 * or counts of individuals per class, or collections of similarity scores for one set of matches versus is another.
 * @author nicole
 *
 */
public class SetDescriptiveStatistics {

	//Change to SummaryStatistics if we are worried about space considerations
	private DescriptiveStatistics mean;
	private DescriptiveStatistics min;
	private DescriptiveStatistics max;
	private DescriptiveStatistics n;
	private DescriptiveStatistics sum;

	public SetDescriptiveStatistics() {
		mean = new DescriptiveStatistics();
		min = new DescriptiveStatistics();
		max = new DescriptiveStatistics();
		n = new DescriptiveStatistics();
		sum = new DescriptiveStatistics();
	}
	
	public String toString() {
		String s = "";
		s+="n: "+n.getN()+"\n";
		s+="mean(n/indiv): "+String.format("%1$.5f", n.getMean())+"\n";
		s+="mean(meanIC): "+String.format("%1$.5f", mean.getMean())+"\n";
		s+="mean(maxIC): "+String.format("%1$.5f", max.getMean())+"\n";
		s+="max(maxIC): "+String.format("%1$.5f", max.getMax())+"\n";
		s+="mean(sumIC): "+String.format("%1$.5f", sum.getMean())+"\n";
		return s;
	}

	public void addSummary(DescriptiveStatistics ds) {
		addMean(ds.getMean());
		addMin(ds.getMin());
		addMax(ds.getMax());
		addN(ds.getN());
		addSum(ds.getSum());
	}
	
	public DescriptiveStatistics getMean() {
		return mean;
	}

	public DescriptiveStatistics getMax() {
		return mean;
	}

	public DescriptiveStatistics getMin() {
		return mean;
	}

	public DescriptiveStatistics getSum() {
		return mean;
	}

	public DescriptiveStatistics getN() {
		return mean;
	}
	
	private void addMean(double value) {
		mean.addValue(value);
	}

	private void addMin(double value) {
		min.addValue(value);
	}

	private void addMax(double value) {
		max.addValue(value);
	}

	private void addN(double value) {
		n.addValue(value);
	}

	private void addSum(double value) {
		sum.addValue(value);
	}
	
}


