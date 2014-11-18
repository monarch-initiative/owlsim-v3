package org.monarchinitiative.owlsim.compute.eval.data;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.eval.data.RemoveByCategorySimulatedData;
import org.monarchinitiative.owlsim.eval.data.RemoveByCategorySimulatedData.NoCategoryFoundException;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.CURIEMapper;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.impl.CURIEMapperImpl;
import org.monarchinitiative.owlsim.kb.impl.LabelMapperImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
	
	
	private void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		this.kb = loader.createKnowledgeBaseInterface();
	}
	
}
