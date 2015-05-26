package org.monarchinitiative.owlsim.compute.mica;

import org.monarchinitiative.owlsim.compute.stats.ICStatsCalculator;

/**
 * A store that caches the results of a MICA (Most Informative Common Ancestor) call
 * 
 * Note that this does not cache the IC - use a {@link ICStatsCalculator} for this
 * 
 * @author cjm
 *
 */
public interface MICAStore {

	/**
	 * @param i
	 * @param j
	 * @return index of MICA
	 */
	public abstract int getMICAIndex(int i, int j);

	/**
	 * @param i
	 * @param j
	 * @return MICA class Id
	 */
	public abstract String getMICAClass(int i, int j);

}