package org.monarchinitiative.owlsim.compute.mica;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class MICAStoreMPTest extends AbstractMICAStoreTest {

	private Logger LOG = Logger.getLogger(MICAStoreMPTest.class);


	@Test
	public void micaTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException {
		load("mp-subset.ttl");
		LabelMapper lm = kb.getLabelMapper();
		boolean isFoundMICA = false;
		for (String ci : kb.getClassIdsInSignature()) {
			int i = kb.getClassIndex(ci);
			String li = lm.getUniqueLabel(ci);
			//LOG.info("CI="+ci);
			for (String cj : kb.getClassIdsInSignature()) {
				int j = kb.getClassIndex(cj);
				String lj = lm.getUniqueLabel(cj);
				int a = micaStore.getMICAIndex(i, j);
				if (li != null && lj != null &&
						li.equals("abnormal basal ganglion morphology") &&
						lj.equals("abnormal hindbrain development")) {
					LOG.info(" MICA(CI,+"+cj+") = "+kb.getClassId(a));
					if (lm.getUniqueLabel(kb.getClassId(a)).equals("abnormal brain morphology")) {
						isFoundMICA = true;
					}
				}
				//LOG.info(" MICA(CI,+"+cj+") = "+kb.getClassId(a));
			}
		}
		Assert.assertTrue("found MICA", isFoundMICA);

	}


}
