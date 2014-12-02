package org.monarchinitiative.owlsim.eval.data;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Datasets generated from this class will be created by "lifting" each
 * annotation to it's superclass(es), creating a single new set with
 * the superclasses replacing each original attribute.  Providing {@link numLevels}
 * will allow lifting an arbitrary number of levels.  
 * 
 * Recursive methods will do this until an empty set is achieved.  
 * @author nlw
 * 
 */
public class LiftAllSimulatedData extends AbstractSimulatedData {

	private static Logger LOG = Logger.getLogger(LiftAllSimulatedData.class);
	private int numLevels = 1;

	
	public LiftAllSimulatedData(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}

	public void setNumLevels (int n) {
		numLevels = n;
	}
	
	public int getNumLevels() {
		return numLevels;
	}

	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts)
			throws Exception {

		int thing = this.getKnowledgeBase().getRootIndex(); //is this OWL:Thing?
		EWAHCompressedBitmap thingBM = EWAHUtils.converIndexSetToBitmap(new HashSet<Integer>(thing));

		EWAHCompressedBitmap liftAll = new EWAHCompressedBitmap();
		EWAHCompressedBitmap[] attrSets = new EWAHCompressedBitmap[numLevels];

		for (int i=0; i<numLevels; i++) {
			liftAll = new EWAHCompressedBitmap();
			
			List<Integer> aList = atts.getPositions();
			for (int j=0; j<aList.size(); j++) {
				Integer a = aList.get(j);
				//get direct superclasses of selected class
				EWAHCompressedBitmap supers = this.getKnowledgeBase().getDirectSuperClassesBM(a);
				supers = supers.andNot(thingBM);
				LOG.info(this.getKnowledgeBase().getClassId(a)+" has "+supers.cardinality()+" parents: "+supers);
				liftAll = liftAll.or(supers);
			}
			LOG.info(numLevels+"-level lifted set: "+liftAll+" (orig="+atts+")");
			if (liftAll.cardinality() > 0) {
				attrSets[i] = liftAll;
			}		
			atts = liftAll;
		}
		return attrSets;
	}

}
