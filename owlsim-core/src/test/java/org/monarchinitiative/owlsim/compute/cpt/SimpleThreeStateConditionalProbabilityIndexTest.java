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
public class SimpleThreeStateConditionalProbabilityIndexTest extends AbstractThreeStateConditionalProbabilityIndexTest{

	private Logger LOG = Logger.getLogger(SimpleThreeStateConditionalProbabilityIndexTest.class);

	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("abc-linear.owl");

		String[] expectedMatches = new String[] {
		"Pr(http://x.org/c =ON |  http://www.w3.org/2002/07/owl#Thing = f ;  ) = 0 %",
		"Pr(http://x.org/c =OFF |  http://www.w3.org/2002/07/owl#Thing = f ;  ) = 100 %",
		"Pr(http://x.org/c =ON |  http://www.w3.org/2002/07/owl#Thing = u ;  ) = 100 %",
		"Pr(http://x.org/c =OFF |  http://www.w3.org/2002/07/owl#Thing = u ;  ) = 0 %",
		"Pr(http://x.org/c =ON |  http://www.w3.org/2002/07/owl#Thing = t ;  ) = 100 %",
		"Pr(http://x.org/c =OFF |  http://www.w3.org/2002/07/owl#Thing = t ;  ) = 0 %",
		
		"Pr(http://x.org/b =ON |  http://x.org/c = f ;  ) = 0 %",
		"Pr(http://x.org/b =OFF |  http://x.org/c = f ;  ) = 100 %",
		"Pr(http://x.org/b =ON |  http://x.org/c = u ;  ) = 67 %",
		"Pr(http://x.org/b =OFF |  http://x.org/c = u ;  ) = 0 %",
		"Pr(http://x.org/b =ON |  http://x.org/c = t ;  ) = 67 %",
		"Pr(http://x.org/b =OFF |  http://x.org/c = t ;  ) = 0 %"

		};
		
		examineCPT(expectedMatches, 12);
		

	}


	

	@Test
	public void cptNegationTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("abc-linear-neg.owl");

		String[] expectedMatches = new String[] {
				"Pr(http://x.org/b =ON |  http://x.org/c = f ;  ) = 0 %",
				"Pr(http://x.org/b =OFF |  http://x.org/c = f ;  ) = 100 %",
				"Pr(http://x.org/b =ON |  http://x.org/c = u ;  ) = 70 %",
				"Pr(http://x.org/b =OFF |  http://x.org/c = u ;  ) = 20 %",
				"Pr(http://x.org/b =ON |  http://x.org/c = t ;  ) = 70 %",
				"Pr(http://x.org/b =OFF |  http://x.org/c = t ;  ) = 10 %"
		};
		
		examineCPT(expectedMatches, 6);
	}


}
