package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.impl.NodeProbabilities;
import org.monarchinitiative.owlsim.compute.cpt.impl.ThreeStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.mica.AbstractMICAStoreTest;
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
public class FlatThreeStateConditionalProbabilityIndexTest extends AbstractThreeStateConditionalProbabilityIndexTest {

	private Logger LOG = Logger.getLogger(FlatThreeStateConditionalProbabilityIndexTest.class);


	


	@Test
	public void cptFlatNegationTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("no-hierarchy-negation-test.owl");

		String[] expectedMatches =
			{
				"Pr(http://x.org/c4 =ON |  http://www.w3.org/2002/07/owl#Thing = f ;  ) = 0 %",
				"Pr(http://x.org/c4 =ON |  http://www.w3.org/2002/07/owl#Thing = u ;  ) = 13 %",
				"Pr(http://x.org/c4 =ON |  http://www.w3.org/2002/07/owl#Thing = t ;  ) = 13 %",
				"Pr(http://x.org/c4 =OFF |  http://www.w3.org/2002/07/owl#Thing = f ;  ) = 100 %",
				"Pr(http://x.org/c4 =OFF |  http://www.w3.org/2002/07/owl#Thing = u ;  ) = 13 %",
				"Pr(http://x.org/c4 =OFF |  http://www.w3.org/2002/07/owl#Thing = t ;  ) = 13 %"
			};
		examineCPT(expectedMatches, 6);
	
	}


}
