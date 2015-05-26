package org.monarchinitiative.owlsim.compute.mica;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class MICAStoreTest extends AbstractMICAStoreTest {

	private Logger LOG = Logger.getLogger(MICAStoreTest.class);


	@Test
	public void micaStoreDAGTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException {
		load("SimpleDAG.owl");
		boolean okx1x2 = false;
		
		String root = "http://x.org/root";	
		String eroot = "http://x.org/eroot";	
		String x1 = "http://x.org/x1";	
		String x2 = "http://x.org/x2";	
		String leaf = "http://x.org/leaf";	
		
		int numFails = 0;

		for (String ci : kb.getClassIdsInSignature()) {
			int i = kb.getClassIndex(ci);
			//LOG.info("CI="+ci);
			for (String cj : kb.getClassIdsInSignature()) {
				int j = kb.getClassIndex(cj);
				int a = micaStore.getMICAIndex(i, j);
				Set<String> acids = kb.getClassIds(a);
				
				if (ci.equals(cj)) {
					if (!(acids.contains(ci) ||
							acids.contains(cj))) {
						numFails++;
					}
					else {
						LOG.info("As expected for identity");
					}
				}
				
				if (ci.equals(x1) && 
						cj.equals(x2)) {
					LOG.info("ancs(x1,x2)="+acids);
					if (acids.contains(root) &&
							acids.contains(eroot) && acids.size() == 2) {
						okx1x2 = true;
					}
				}
				//LOG.info("    MICA(CI,+"+cj+") = "+kb.getClassId(a));
			}
		}
		Assert.assertTrue("found MICA", okx1x2);
		Assert.assertEquals(0, numFails);
	}


	/**
	 * This test ensures that if LCA is tied, then the most informative is
	 * always the MICA
	 * 
	 * @throws OWLOntologyCreationException
	 * @throws NoRootException
	 * @throws URISyntaxException
	 * @throws NonUniqueLabelException
	 */
	@Test
	public void micaStoreICTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException {
		load("mica-test.owl");
		boolean okx1x2 = false;
		
		String root = "http://x.org/root";	
		String eroot = "http://x.org/eroot";	
		String x1 = "http://x.org/x1";	
		String x2 = "http://x.org/x2";	
		String leaf = "http://x.org/leaf";	
		
		int numClasses = kb.getClassIdsInSignature().size();
		String owlThing = kb.getClassId(kb.getRootIndex());
		LOG.info("Root="+owlThing);
		
		// ensure that classes are ordered such that most informative
		// classes have the lowest index.
		// technically this is an internal detail, and should be checked within
		// the MICA implementation itself, but we add here for safety
		int lastNum = 0;
		for (int i=0; i<numClasses; i++) {
			String ci = kb.getClassId(i);
			EWAHCompressedBitmap indbm = kb.getIndividualsBM(i);
			List<Integer> indIxs = indbm.getPositions();
			int num = indIxs.size();
			LOG.info(i+" Id="+ci+ " INDS:"+indIxs+" Num="+num);
			Assert.assertTrue("NOT ORDERED", num >= lastNum);
			lastNum = num;
			if (i == numClasses-1) {
				Assert.assertTrue("owl:Thing is least informative and therefore should be last",
						ci.equals(owlThing));
			}
		}
		
		int numFound = 0;

		for (String ci : kb.getClassIdsInSignature()) {
			int i = kb.getClassIndex(ci);
			//LOG.info("CI="+ci+" ix:"+i);
			for (String cj : kb.getClassIdsInSignature()) {
				int j = kb.getClassIndex(cj);
				//LOG.info(" CJ="+cj+" ix:"+j);
				int a = micaStore.getMICAIndex(i, j);
				Set<String> acids = kb.getClassIds(a);
				String acid1 = acids.iterator().next();
				LOG.info("   MICA("+ci+","+cj+") = "+acids);
				
				if (ci.equals(cj)) {
					Assert.assertTrue("if x=y, then ancestor=x=y", acids.contains(ci));
				}
				
				Assert.assertTrue("MICA(j,i) must equal MICA(i,J)",
						acids.equals(kb.getClassIds(micaStore.getMICAIndex(j, i))));
				
				if (ci.equalsIgnoreCase(owlThing) ||
						ci.equalsIgnoreCase(owlThing)) {
					Assert.assertTrue("MICA(_,Root)=Root",
							acid1.equals(owlThing));
				}
				
				if (ci.equals("http://x.org/ca2c") &&
						cj.equals("http://x.org/ca2d")) {
					Assert.assertTrue(acid1.equals("http://x.org/a2"));
					numFound ++;
				}
				if (ci.equals("http://x.org/x4") &&
						cj.equals("http://x.org/ca2a")) {
					Assert.assertTrue(acid1.equals(cj));
					numFound ++;
				}

				// LCA(x1,x2) = {ca1a, ca1b, ca1c, ca1d, ca2a, ca2b, ca2c, ca2d}
				// However, ca2a, ca2b have fewest individuals, so should be highest
				if (ci.equals("http://x.org/x1") &&
						cj.equals("http://x.org/x2")) {
					// This test has been especially constructed to test this corner case;
					// x1 and x2 have multiple least common subsumers:
					// ca1a, ca1b, ca1c, ca1d, ca2a, ca2b, ca2c, ca2d.
					// of these, two are tied as being maximmaly informative
					Assert.assertTrue("MICA of x1 and x2 MUST be either ca2a or ca2b",
							acids.contains("http://x.org/ca2a") ||
							acids.contains("http://x.org/ca2b"));
					numFound ++;
				}


			}
		}
		Assert.assertEquals(3, numFound);
	}

}
