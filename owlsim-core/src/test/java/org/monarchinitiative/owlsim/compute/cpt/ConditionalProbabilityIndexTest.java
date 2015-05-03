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
public class ConditionalProbabilityIndexTest extends AbstractTwoStateConditionalProbabilityIndexTest  {



	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("SimpleDAG.owl");
		
		String[] expectedMatches = {
				"Pr(http://x.org/leaf |  http://x.org/x2 = u ; http://x.org/ex1 = u ;  ) = 0.20",
				"Pr(http://x.org/leaf |  http://x.org/x2 = t ; http://x.org/ex1 = u ;  ) = 0.33",
				"Pr(http://x.org/leaf |  http://x.org/x2 = u ; http://x.org/ex1 = t ;  ) = 0.33",
				"Pr(http://x.org/leaf |  http://x.org/x2 = t ; http://x.org/ex1 = t ;  ) = 0.50"
		};
		examineCPT(expectedMatches, 4);
	}


}
