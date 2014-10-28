package org.monarchinitiative.owlsim.kb.filter;

import java.net.URISyntaxException;

import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class FilterEngineTest extends AbstractFilterEngineTest {
	
	@Test
	public void testFilterEngine() throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		load("filter-test-ontology.owl");
		
		
		// TODO - allow shortForms in properties
		PropertyValueFilter pvf = PropertyValueFilter.create("http://x.org/op", "http://x.org/ib", false);
		testFilter(pvf, "http://x.org/ia");
		
		// TODO - test other owl property assertion types
		
		// TODO - test identifier (individual) filter
	}

}
