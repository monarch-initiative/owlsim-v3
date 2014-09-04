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
public  class CurieMapperImplTest extends AbstractOwlTest {

	private Logger LOG = Logger.getLogger(CurieMapperImplTest.class);

	@Test
	public void basicTest() throws OWLOntologyCreationException, URISyntaxException, NonUniqueLabelException {
		load("mp-subset.ttl");

		
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

			// direct types is subset of direct plus indirect types
			Assert.assertEquals(kb.getDirectTypesBM(id).and(kb.getTypesBM(id)),
					kb.getDirectTypesBM(id));
			
			// check that the union of direct types plus the superclasses
			// of all direct types is the same as all types
			EWAHCompressedBitmap dtypes = kb.getDirectTypesBM(id);
			Set<String> cids = new HashSet<String>();
			for (int p : dtypes) {
				cids.add(kb.getClassId(p));
			}
			Assert.assertEquals(dtypes.or(kb.getSuperClassesBM(cids)),
					kb.getTypesBM(id));
			n++;
		}
		Assert.assertEquals(4, n);

		String rootId = kb.getLabelMapper().lookupByUniqueLabel("mammalian phenotype");
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






}
