package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;

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
