package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;

import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;


public class ConditionalProbabilityIndexTest extends AbstractTwoStateConditionalProbabilityIndexTest  {



	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("SimpleDAG.owl");
		
		String leafId = kb.getClassId(kb.getClassIndex("http://x.org/leaf"));
		String x1Id = kb.getClassId(kb.getClassIndex("http://x.org/x1"));
		String x2Id = kb.getClassId(kb.getClassIndex("http://x.org/x2"));
	
		String[] expectedMatches = {
				"Pr("+leafId+" |  "+x1Id+" = u, "+x2Id+" = u ) = 0.18",
				"Pr("+leafId+" |  "+x1Id+" = t, "+x2Id+" = u ) = 0.35",
				"Pr("+leafId+" |  "+x1Id+" = u, "+x2Id+" = t ) = 0.35",
				"Pr("+leafId+" |  "+x1Id+" = t, "+x2Id+" = t ) = 0.52"
		};


		examineCPT(expectedMatches, 4);
	}


}
