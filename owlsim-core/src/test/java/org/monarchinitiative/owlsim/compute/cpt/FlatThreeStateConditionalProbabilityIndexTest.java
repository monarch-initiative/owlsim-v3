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
