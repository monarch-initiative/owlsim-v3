package org.monarchinitiative.owlsim.compute.eval.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.stats.ICStatsCalculator;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.eval.data.LiftAllSimulatedData;
import org.monarchinitiative.owlsim.eval.data.LiftOneSimulatedData;
import org.monarchinitiative.owlsim.eval.data.RandomChooseNSimulatedData;
import org.monarchinitiative.owlsim.eval.data.RemoveByCategorySimulatedData;
import org.monarchinitiative.owlsim.eval.data.RemoveByCategorySimulatedData.NoCategoryFoundException;
import org.monarchinitiative.owlsim.eval.data.RemoveByICThresholdSimulatedData;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.CURIEMapper;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.monarchinitiative.owlsim.kb.impl.CURIEMapperImpl;
import org.monarchinitiative.owlsim.kb.impl.LabelMapperImpl;
import org.prefixcommons.CurieUtil;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class SimulatedDataTest {
	
	private Logger LOG = Logger.getLogger(SimulatedDataTest.class);
	
	private BMKnowledgeBase kb;
	CURIEMapper cm = new CURIEMapperImpl();
	LabelMapper lm = new LabelMapperImpl(cm);
	
	
	@Test
	public void testCreateRemoveCategoryNoIDAnnotations() throws Exception {
		Set<String> removed = new HashSet<String>();
		String category = null;
		try {
			//this should throw an exception
			removed = createRemoveCategoryAnnotations(false,false, category);			
			Assert.assertFalse("classes found in category"+category+": "+removed.toString(), true);
		} catch (Exception e) {
			LOG.error("Caught Exception "+e);
			Assert.assertTrue(e.getClass() == NoCategoryFoundException.class);
		}
	}

	@Test
	public void testCreateRemoveCategoryBadIDAnnotations() throws Exception {
		Set<String> removed = new HashSet<String>();
		String category = "BOGUS:123";
		try {
			//this should throw an exception
			removed = createRemoveCategoryAnnotations(false,false, category);			
			Assert.assertFalse("classes found in category"+category+": "+removed.toString(), true);
		} catch (Exception e) {
			LOG.error("Caught Exception "+e);
			Assert.assertTrue(e.getClass() == NoCategoryFoundException.class);
		}
	}

	@Test
	public void testCreateRemoveCategoryAnnotations() throws Exception {
		Set<String> removed = new HashSet<String>();
		Set<String> categoriesToRemove = new HashSet<String>();
		categoriesToRemove.add("http://purl.obolibrary.org/obo/MP_0004924");    //abnormal behavior
		categoriesToRemove.add("http://purl.obolibrary.org/obo/MP_0003631");    //nervous system phenotype
		categoriesToRemove.add("http://purl.obolibrary.org/obo/MP_0002211");	//abnormal primary sex determination, should remove match small gonad only

		
		for (String category : categoriesToRemove) {
			LOG.info("Testing category "+category+lm.getUniqueLabel(category));
			try {
				removed = createRemoveCategoryAnnotations(false,false,category);		
				LOG.info("Removed "+removed.size());
				Assert.assertTrue("classes found in category"+category+": "+removed.toString(), removed.size() > 0);
			} catch (Exception e) {
				LOG.error("Caught Exception "+e);
				Assert.assertFalse(true);
			}
		}
	}
	
	@Test
	public void testCreateRemoveCategoryInclusiveAnnotations() throws Exception {
		Set<String> removed = new HashSet<String>();
		Set<String> categoriesToRemove = new HashSet<String>();

		//should only be found if inclusive, as they are leaves
		categoriesToRemove.add("http://purl.obolibrary.org/obo/MP_0000872");      //abnormal cerebellum external granule cell layer morphology
		categoriesToRemove.add("http://purl.obolibrary.org/obo/MP_0001898");      //abnormal long term depression
		
		//for each one of the tests, compare the inclusive vs exclusive results
		for (String category : categoriesToRemove) {
			LOG.info("Testing category "+category);
			try {
				removed = createRemoveCategoryAnnotations(false,true,category);		
				Assert.assertTrue("classes found in category"+category+": "+removed.toString(), removed.size() > 0);

				//these should not be removed when inclusive==false
				removed = createRemoveCategoryAnnotations(false,false,category);		
				Assert.assertTrue("classes found in category"+category+": "+removed.toString(), removed.size() == 0);
				
			} catch (Exception e) {
				LOG.error("Caught Exception "+e);
				Assert.assertFalse(true);

			}
		}
	}
	
	@Test
	public void testCreateRemoveCategoryInverted() throws Exception {
		load("mp-subset.ttl");

		//test on pd
		String iid = "http://purl.obolibrary.org/obo/pd"; //parkinson's disease
		String cat1 = "http://purl.obolibrary.org/obo/MP_0001516"; //abnormal motor coordination/ balance
		String cat2 = "http://purl.obolibrary.org/obo/MP_0002152"; //abnormal brain morphology

		Boolean boolswitch = true;
		for (int j=0; j<2; j++) {

			RemoveByCategorySimulatedData data = new RemoveByCategorySimulatedData(kb);
			data.setInverse(true);
			Set<Integer> inverseCbits = new HashSet<Integer>();

			if (j==0) {
				data.setCategory(cat1);
				inverseCbits.add(kb.getClassIndex(cat2));
			} else {
				data.setCategory(cat2);
				inverseCbits.add(kb.getClassIndex(cat1));
				boolswitch = false;
			}

			//the other categories to remove from
			data.setInverseCategories(inverseCbits);

			//get the classes associated with the individual, create data sets
			EWAHCompressedBitmap atts = kb.getDirectTypesBM(iid);
			LOG.info(iid+ " classes: "+atts.getPositions());
			EWAHCompressedBitmap[] attSets = data.createAttributeSets(atts);

			Set<String> removed = new HashSet<String>();

			for (int i=0; i<attSets.length; i++) {
				EWAHCompressedBitmap a = attSets[i];
				LOG.info("set "+i+": "+a.getPositions());
				LOG.info("removed: "+atts.andNot(a).getPositions());
				for (int r : atts.andNot(a).getPositions()) {
					removed.add(kb.getClassId(r));
				}
			}
			//abnormal basal ganglion morphology
			Assert.assertEquals(boolswitch,removed.contains("http://purl.obolibrary.org/obo/MP_0006007"));
			//abnormal cerebellum external granule cell layer morphology
			Assert.assertEquals(boolswitch, removed.contains("http://purl.obolibrary.org/obo/MP_0000872"));
			//impaired coordination
			Assert.assertEquals(!boolswitch, removed.contains("http://purl.obolibrary.org/obo/MP_0001405"));
		}

	}
	
	//TODO haven't implemented recursive strategy yet, but should add tests when it is.

/*	@Test
	public void testCreateRemoveCategoryAllCombosAnnotations() throws Exception {
//		createRemoveCategoryAnnotations(true,"MP:0003631");
		createRemoveCategoryAnnotations(true,"MP:0004924");
		
	}
*/
	
	private Set<String> createRemoveCategoryAnnotations(Boolean setRecursive, Boolean inclusive, String categoryID) throws Exception {
		
		load("mp-subset.ttl");
		
		RemoveByCategorySimulatedData data = new RemoveByCategorySimulatedData(kb);

		data.setCategory(categoryID);
		data.setRecursive(setRecursive);
		data.setInclusive(inclusive);

		Map<Integer, EWAHCompressedBitmap[]> sets = data.createAssociations();
				
		
		int numSets = 0;
		
		//save the classes that were removed
		Set<String> removedClasses = new HashSet<String>();
				
		for (int ibit : sets.keySet()) {
			//get the individual
			String i = kb.getIndividualId(ibit);
			
			//get the original individual class map
			EWAHCompressedBitmap origBM =  kb.getDirectTypesBM(i);
			//get the derived class sets for the individual
			EWAHCompressedBitmap[] derivedBMs = sets.get(ibit);
			if (derivedBMs.length == 0) {
				LOG.info("No sets created for "+i);
			}
			for (EWAHCompressedBitmap bm : derivedBMs) {
				EWAHCompressedBitmap removedBM = origBM.andNot(bm);
				LOG.info("For "+i+": removed "+ removedBM.cardinality()+ " subclasses of "+categoryID);				
				for (int cbit : removedBM.getPositions()) {
					removedClasses.add(lm.getUniqueLabel(kb.getClassId(cbit)));					
				}
				numSets++;
			}
		}
		LOG.info("Created a total of "+numSets+" from "+sets.size()+" individuals.");
		return removedClasses;
	}
	
	@Test
	public void testLiftOneAnnotations() throws Exception {
		load("mp-subset.ttl");
		LiftOneSimulatedData data = new LiftOneSimulatedData(kb);

		Map<Integer, EWAHCompressedBitmap[]> sets = data.createAssociations();
		//save the classes that were removed
		Set<String> removedClasses = new HashSet<String>();
		Set<Integer> removedCBits = new HashSet<Integer>();
		
		for (int ibit : sets.keySet()) {
			//get the individual
			String i = kb.getIndividualId(ibit);
			
			//get the original individual class map
			EWAHCompressedBitmap origBM =  kb.getDirectTypesBM(i);
			//get the derived class sets for the individual
			EWAHCompressedBitmap[] derivedBMs = sets.get(ibit);
			if (derivedBMs.length == 0) {
				LOG.info("No sets created for "+i);
			}
			for (EWAHCompressedBitmap bm : derivedBMs) {
				EWAHCompressedBitmap removedBM = origBM.andNot(bm);
//				LOG.info("For "+i+": removed "+ removedBM.cardinality()+ ": "+removedBM.toString());				
				for (int cbit : removedBM.getPositions()) {
					removedClasses.add(lm.getUniqueLabel(kb.getClassId(cbit)));	
					removedCBits.add(cbit);
				}
				//numSets++;
			}
			//for each individual, the things removed == original
			LOG.info("i= "+i+" orig="+origBM.toString()+" removed="+removedCBits.toString()+" in "+derivedBMs.length+" sets");
			Assert.assertTrue("Bits missing: "+origBM.andNot(EWAHUtils.convertIndexSetToBitmap(removedCBits)), origBM.andNotCardinality(EWAHUtils.convertIndexSetToBitmap(removedCBits))==0);
		}
	}
	
	@Test
	public void testLiftAllAnnotations() throws Exception {
		this.testLiftAllAnnotationsByLevel(-1);
	
		this.testLiftAllAnnotationsByLevel(1);
		
		this.testLiftAllAnnotationsByLevel(3);
	}
	
	private void testLiftAllAnnotationsByLevel(int numLevels) throws Exception {

		load("mp-subset.ttl");
		LiftAllSimulatedData data = new LiftAllSimulatedData(kb);

		if (numLevels > 0) {
			data.setNumLevels(numLevels);
		} else {
			//numLevels == default = 1
			numLevels = 1;
		}
		
		Map<Integer, EWAHCompressedBitmap[]> sets = data.createAssociations();
		//save the classes that were removed
		
		for (int ibit : sets.keySet()) {
			//get the individual
			String i = kb.getIndividualId(ibit);
			
			//get the original individual class map
			EWAHCompressedBitmap origBM =  kb.getDirectTypesBM(i);
			//get the derived class sets for the individual
			EWAHCompressedBitmap[] derivedBMs = sets.get(ibit);
			if (derivedBMs.length == 0) {
				LOG.info("No sets created for "+i);
			}
			//there should be |derivedBM| == numLevels
			Assert.assertTrue("Derived datasets n="+derivedBMs.length+" (should be ="+numLevels+")", derivedBMs.length==numLevels);
			for (EWAHCompressedBitmap bm : derivedBMs) {
				EWAHCompressedBitmap removedBM = origBM.andNot(bm);
				//the items removed from the derivedBM should be == original
				Assert.assertTrue("missing: "+origBM.andNotCardinality(removedBM),origBM.equals(removedBM));
			}
		}
	}
	
	@Test
	public void testRemoveByICThreshold() throws Exception {
		this.testRemoveByICThresholdByThreshold(-1.0);
		this.testRemoveByICThresholdByThreshold(1.0);
		
	}
	
	private void testRemoveByICThresholdByThreshold(Double threshold) throws Exception {

		load("mp-subset.ttl");
		RemoveByICThresholdSimulatedData data = new RemoveByICThresholdSimulatedData(kb);
		ICStatsCalculator icc = new ICStatsCalculator(kb);

		if (threshold > 0) {
			data.setICCutoff(threshold);
		}
		
		Map<Integer, EWAHCompressedBitmap[]> sets = data.createAssociations();
		//save the classes that were removed
		
		for (int ibit : sets.keySet()) {
			//get the individual
			String i = kb.getIndividualId(ibit);
			
			//get the original individual class map
			EWAHCompressedBitmap origBM =  kb.getDirectTypesBM(i);
			DescriptiveStatistics origds = icc.getICStatsForAttributesByBM(origBM);

			//get the derived class sets for the individual
			EWAHCompressedBitmap[] derivedBMs = sets.get(ibit);
			
			for (EWAHCompressedBitmap bm : derivedBMs) {
				LOG.info("orig="+origBM+"; derived="+bm);
				DescriptiveStatistics ds = icc.getICStatsForAttributesByBM(bm);
				if (origBM.equals(bm)) {
					Assert.assertTrue("set="+bm+"; orig="+origds.getSum()+"; derived="+ds.getSum()+
							"; cutoff="+data.getICCutoff(),
							(ds.getSum() == origds.getSum())); 
					
				} else {
					Assert.assertTrue("set="+bm+"; orig="+origds.getSum()+"; derived="+ds.getSum()+
						"; cutoff="+data.getICCutoff(),
						(ds.getSum() < origds.getSum())); 
				}
			}
		}
		
	}
	
	@Test
	public void testRandomChooseNData() throws Exception {
		LOG.info("Creating ontology");
		this.create(2000,2,100);
		LOG.info("Finished creating ontology");

		//test defaults
		testRandomChooseNWithParams(-1,-1); 
		//decent sizes
		testRandomChooseNWithParams(5, 2);

		//bogus sizes - should resize
		testRandomChooseNWithParams(200, 5000);
	}
	
	private void testRandomChooseNWithParams(int numSets, int setLength) throws Exception {

		RandomChooseNSimulatedData data = new RandomChooseNSimulatedData(kb);

		if (setLength > 0) {
			data.setSetLength(setLength);
			Assert.assertTrue(data.getSetLength() == setLength);
		}
		
		if (numSets > 0) {
			data.setNumSets(numSets);
			Assert.assertTrue(data.getNumSets() == numSets);
		}
		
		LOG.info("Test creating random subsets with numSets="+numSets+", setLength="+setLength);
		
		Map<Integer, EWAHCompressedBitmap[]> sets = data.createAssociations();
		//save the classes that were removed
		LOG.info("Done creating associations for "+sets.size());
		for (int ibit : sets.keySet()) {
			//get the individual
			String i = kb.getIndividualId(ibit);
			
			//get the original individual class map
			EWAHCompressedBitmap origBM =  kb.getDirectTypesBM(i);

			//get the derived class sets for the individual
			EWAHCompressedBitmap[] derivedBMs = sets.get(ibit);
			Assert.assertTrue("DerivedBM.size should be > "+ data.getNumSets()+ "; but is "+derivedBMs.length,
					derivedBMs.length <= data.getNumSets());
			for (EWAHCompressedBitmap bm : derivedBMs) {
//				LOG.info("orig="+origBM+"; derived="+bm);
				Assert.assertFalse("Orig and derived should not be equal", origBM.equals(bm));
				if (origBM.cardinality() <= data.getSetLength()) {
					Assert.assertTrue(bm.cardinality() == origBM.cardinality()-1);
				} else {
					Assert.assertTrue(bm.getPositions().size() == data.getSetLength());				
				}
			}
		}
	}
	
	private void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		this.kb = loader.createKnowledgeBaseInterface();
	}
	
	private void create(int numClasses, int avgParents, int numIndividuals) throws OWLOntologyCreationException {
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf, new CurieUtil(new HashMap<String, String>()));
	}
	
}
