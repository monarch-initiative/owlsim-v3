package org.monarchinitiative.owlsim.compute.mica.perf;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.AbstractMICAStoreTest;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests performance of MICAStore using static version of HP and disease-phenotype
 * annotations
 * 
 * @author cjm
 *
 */
public class MICAStoreHPPerfIT extends AbstractMICAStoreTest {

	private Logger LOG = Logger.getLogger(MICAStoreHPPerfIT.class);


	@Test
	public void micaHPTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException {
		long timeToIndex = load("ontologies/hp.obo", "/data/Homo_sapiens-data.owl");
		LabelMapper lm = kb.getLabelMapper();
		boolean isFoundMICA = false;
		String c1 = kb.getLabelMapper().lookupByUniqueLabel("Dyssynergia");
		String c2 = kb.getLabelMapper().lookupByUniqueLabel("Atrophy of the dentate nucleus");
		String ca = kb.getLabelMapper().lookupByUniqueLabel("Abnormality of the cerebellum"); // expected ancestor
		int cix1 = kb.getClassIndex(c1);
		int cix2 = kb.getClassIndex(c2);
		int cixa = kb.getClassIndex(ca);
		LOG.info("Iterating through grid looking for "+cix1+ " vs "+cix2+" == "+cixa);
		long t1 = System.currentTimeMillis();
		int n = kb.getNumClassNodes();
		for (int i=0; i<n; i++) {
			//String li = lm.getUniqueLabel(ci);
			//LOG.info("CI="+ci);
			for (int j=0; j<n; j++) {
				//String lj = lm.getUniqueLabel(cj);
				int a = micaStore.getMICAIndex(i, j);
				if (i == cix1 && j == cix2) {
					LOG.info(" MICA = "+kb.getClassId(a));
					isFoundMICA = a == cixa;
				}
			}
		}
		long t2 = System.currentTimeMillis();
		long dt = t2-t1;
		LOG.info("Cache population time: "+timeToIndex); // Expect ~32s for 10k x 10k
		LOG.info("Cache traversal time: "+dt); // Expect <1s, e.g. 15ms for 100m lookups
		Assert.assertTrue("found MICA", isFoundMICA);
		Assert.assertTrue("Cache traversal should be faster than population", dt < timeToIndex);

	}


}
