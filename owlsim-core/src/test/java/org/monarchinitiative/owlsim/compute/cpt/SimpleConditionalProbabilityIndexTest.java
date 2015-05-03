package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
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
public class SimpleConditionalProbabilityIndexTest extends AbstractTwoStateConditionalProbabilityIndexTest {

	private Logger LOG = Logger.getLogger(SimpleConditionalProbabilityIndexTest.class);


	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("abc-linear.owl");

		String[] expectedMatches = {

				"Pr(http://x.org/a |  http://x.org/b = u ;  ) = 0.33",
				"Pr(http://x.org/a |  http://x.org/b = t ;  ) = 0.50",
				"Pr(http://x.org/b |  http://x.org/c = u ;  ) = 0.67",
				"Pr(http://x.org/b |  http://x.org/c = t ;  ) = 0.67"

		};
		examineCPT(expectedMatches, 4);

	}


}
