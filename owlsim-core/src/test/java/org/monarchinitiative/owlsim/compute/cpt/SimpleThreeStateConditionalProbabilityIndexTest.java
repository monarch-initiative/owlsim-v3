package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

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
