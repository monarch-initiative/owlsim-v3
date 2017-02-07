package org.monarchinitiative.owlsim.eval.data;

import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Datasets generated from this class will be created by iteratively "lifting" each
 * annotation to it's superclass(es).  For a given set of n attributes, this
 * will create n derived attribute sets.  Some of the derived attribute sets may be
 * identical.
 * Recursive implementations will do this until the number of attributes in the
 * set is zero.
 * @author nlw
 */
public class LiftOneSimulatedData extends AbstractSimulatedData {

	private static Logger LOG = Logger.getLogger(LiftOneSimulatedData.class);

	
	public LiftOneSimulatedData(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}


	@Override
	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts)
			throws Exception {

		EWAHCompressedBitmap[] derivedAtts = new EWAHCompressedBitmap[0];

		return createAttributeSetsRecursive(atts,derivedAtts);
	}
	
	//TODO not yet actually recursive implementation
	private EWAHCompressedBitmap[] createAttributeSetsRecursive(EWAHCompressedBitmap atts, EWAHCompressedBitmap[] derivedAtts) {

		int thing = this.getKnowledgeBase().getRootIndex(); //is this OWL:Thing?
		EWAHCompressedBitmap thingBM = EWAHUtils.convertIndexSetToBitmap(new HashSet<Integer>(thing));
		EWAHCompressedBitmap[] attrSets = new EWAHCompressedBitmap[atts.cardinality()];

		List<Integer> aList = atts.getPositions();
		for (int i=0; i<aList.size(); i++) {
			Integer a = aList.get(i);
			List<Integer> thisList = atts.getPositions();
			thisList.remove(i);
			EWAHCompressedBitmap bm = EWAHUtils.convertSortedIndexListToBitmap(thisList);
			//get direct superclasses of selected class
			EWAHCompressedBitmap supers = this.getKnowledgeBase().getDirectSuperClassesBM(a);
			//remove the root node
			supers = supers.andNot(thingBM);
			LOG.info(this.getKnowledgeBase().getClassId(a)+" has "+supers.cardinality()+" parents: "+supers);
			bm = bm.or(supers);
			if (bm.cardinality() > 0) {
				attrSets[i] = bm;
			}				
		}
		
		return attrSets;
	}
	

}
