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
		PropertyValueFilter opvf = PropertyValueFilter.create("http://x.org/op", "http://x.org/ib", false);
		testFilter(opvf, "http://x.org/ia");
		
		// TODO - test other owl property assertion types
		PropertyValueFilter dpvf = PropertyValueFilter.create("http://x.org/dp", "foo", false);
		testFilter(dpvf, "http://x.org/ib");
		
		// TODO - test identifier (individual) filter
	}

}
