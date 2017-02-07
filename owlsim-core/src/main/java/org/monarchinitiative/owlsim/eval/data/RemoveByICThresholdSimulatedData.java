package org.monarchinitiative.owlsim.eval.data;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.stats.ICStatsCalculator;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * <p>This will return a set containing one BM with any attribute classes removed
 * where {@code ic(att) < icCutoff}; therefore a threshold of {@code icCutoff} is
 * applied to the set of attributes.</p>
 * <p>If no {@code icCutoff} has been initialized with {@link setICCutoff}, it will remove any
 * attribute == maxIC(atts).  This cutoff can be retrieved with {@link getICCutoff}</p>
 * @author nlw
 */
public class RemoveByICThresholdSimulatedData extends AbstractSimulatedData {

	private static Logger LOG = Logger.getLogger(RemoveByICThresholdSimulatedData.class);
	private Double icCutoff = -1.0;

	
	public RemoveByICThresholdSimulatedData(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}

	public void setICCutoff(double icCutoff) {
		this.icCutoff = icCutoff;
	}
	
	public Double getICCutoff() {
		return icCutoff;
	}

	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts)
			throws Exception {

		EWAHCompressedBitmap[] attrSets = new EWAHCompressedBitmap[1];

		ICStatsCalculator icc = new ICStatsCalculator(this.getKnowledgeBase());

		if (icCutoff < 0) {
			DescriptiveStatistics ds = icc.getICStatsForAttributesByBM(atts);
			icCutoff = ds.getMax();
			LOG.info("No cutoff specified; Setting to maxIC value of "+ds.getMax());
		}

		//filter the attributes by the IC cutoff by making a 
		//derived set that only includes those classes > ICcutoff
		Set<Integer> s = new HashSet<Integer>();
		for (int cbit : atts.getPositions()) {
			Double ic = icc.getInformationContentByClassIndex(cbit);
			if (ic >= icCutoff) {
				s.add(cbit);
			} else {
				LOG.info("Removing "+cbit+" from resulting set. ("+ic+"<"+icCutoff+")");
			}
		}
		if (s.size() > 0) {
			attrSets[0] = EWAHUtils.convertIndexSetToBitmap(s);
		}
		
		return attrSets;
	}

}
