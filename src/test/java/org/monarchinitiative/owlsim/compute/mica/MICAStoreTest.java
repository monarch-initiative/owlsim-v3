package org.monarchinitiative.owlsim.compute.mica;

import java.net.URISyntaxException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class MICAStoreTest extends AbstractMICAStoreTest {

	private Logger LOG = Logger.getLogger(MICAStoreTest.class);


	@Test
	public void micaTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException {
		load("SimpleDAG.owl");
		LabelMapper lm = kb.getLabelMapper();
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


}
