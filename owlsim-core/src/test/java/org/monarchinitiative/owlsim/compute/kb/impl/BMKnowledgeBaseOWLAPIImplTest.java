package org.monarchinitiative.owlsim.compute.kb.impl;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Tests a OWLAPI implementation of a KB
 * 
 * Also tests:
 *  - LabelMapper
 *  
 *
 */
public  class BMKnowledgeBaseOWLAPIImplTest extends AbstractOwlTest {

	private Logger LOG = Logger.getLogger(BMKnowledgeBaseOWLAPIImplTest.class);

	@Test
	public void basicTest() throws OWLOntologyCreationException, URISyntaxException, NonUniqueLabelException {
		load("mp-subset.ttl");
		String rootId = kb.getLabelMapper().lookupByUniqueLabel("mammalian phenotype");

		int n=0;
		for (String id : kb.getClassIdsInSignature()) {
			// check reflexivity of superclass
			checkContains(kb.getSuperClassesBM(id), id);

			// direct subclass is subset of direct plus indirect subclass
			Assert.assertEquals(kb.getDirectSuperClassesBM(id).and(kb.getSuperClassesBM(id)),
					kb.getDirectSuperClassesBM(id));
			n++;
		}
		Assert.assertTrue(n > 100);

		n = 0;
		for (String id : kb.getIndividualIdsInSignature()) {
			LOG.info("Testing ind "+id);
			// direct types is subset of direct plus indirect types
			Assert.assertEquals(kb.getDirectTypesBM(id).and(kb.getTypesBM(id)),
					kb.getDirectTypesBM(id));
			
			// check that the union of direct types plus the superclasses
			// of all direct types is the same as all types
			EWAHCompressedBitmap dtypes = kb.getDirectTypesBM(id);
			Set<String> cids = getClassIdsFromBM(dtypes);
			
			Assert.assertEquals(dtypes.or(kb.getSuperClassesBM(cids)),
					kb.getTypesBM(id));
			
			//check that getting a set of types filtered by a particular class
			//(in this case root) is equal to getting all types minus the parent of root
			EWAHCompressedBitmap alltypes = kb.getTypesBM(id);
			Set<String> allids = getClassIdsFromBM(alltypes);
			
			EWAHCompressedBitmap filteredtypes = kb.getFilteredTypesBM(cids, rootId);
			Set<String> filteredIds = getClassIdsFromBM(filteredtypes);

			
			//get root direct parents, which ought to be owlthing.
			EWAHCompressedBitmap rootParents = kb.getDirectSuperClassesBM(rootId);
			Set<String> parentIds = getClassIdsFromBM(rootParents);

			Assert.assertEquals(filteredtypes, kb.getTypesBM(id).andNot(rootParents));

			//check that a given type is in the subset of classes when filtered by 
			//any of it's superclasses
			for (String cid : cids) {
				EWAHCompressedBitmap supers = kb.getSuperClassesBM(cid);	
				//String clabel = kb.getLabelMapper().getUniqueLabel(cid);
				for (int p : supers) {
					String superid = kb.getClassId(p);
					filteredIds = new HashSet<String>();
					Set<String> filteredLabels = new HashSet<String>();
					EWAHCompressedBitmap filteredBM = kb.getFilteredDirectTypesBM(id, superid);
					for (int q : filteredBM) {
						String fid = kb.getClassId(q);
						filteredIds.add(fid);
						filteredLabels.add(kb.getLabelMapper().getUniqueLabel(fid));
					}
					//String m = "filter by "+kb.getLabelMapper().getUniqueLabel(superid)+" should contain "+clabel;
					//Assert.assertTrue(m, filteredBM.and(dtypes).getPositions().contains(kb.getClassIndex(cid)));
					checkContains(filteredBM.and(dtypes),cid);

				}
				
			}
			
			n++;
		}
		Assert.assertEquals(4, n);

		check(kb.getSuperClassesBM(rootId), 2);
		
		String xid =
				kb.getLabelMapper().lookupByUniqueLabel("abnormal cerebellum external granule cell layer morphology");
		check(kb.getDirectSuperClassesBM(xid), 1);
		check(kb.getSuperClassesBM(xid), 13);
		checkContainsLabel(kb.getSuperClassesBM(xid), "mammalian phenotype");
		checkContainsLabel(kb.getSuperClassesBM(xid), "abnormal brain development");
		checkContainsLabel(kb.getSuperClassesBM(xid), "abnormal cerebellum development");
		checkContainsLabel(kb.getSuperClassesBM(xid), "abnormal cerebellum morphology");
		
		String diseaseId = 
				kb.getLabelMapper().lookupByUniqueLabel("Parkinsons disease (fake for testing)");
//		check(kb.getDirectTypesBM(diseaseId), 25); // TODO
		check(kb.getTypesBM(diseaseId), 25);
		check(kb.getDirectTypesBM(diseaseId), 4);
		checkContainsLabel(kb.getTypesBM(diseaseId),
				"mammalian phenotype");
		checkContainsLabel(kb.getDirectTypesBM(diseaseId),
				"abnormal cerebellum external granule cell layer morphology");
	
	}


	//TODO remove, and replace caller with kb.getClassIds(bm)
	private Set<String> getClassIdsFromBM(EWAHCompressedBitmap bm) {
		Set<String> cids = new HashSet<String>();
		for (int c : bm) {
			cids.add(kb.getClassId(c));
		}
		return cids;		
	}



}
