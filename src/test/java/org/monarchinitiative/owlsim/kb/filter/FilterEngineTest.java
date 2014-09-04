package org.monarchinitiative.owlsim.kb.filter;

import java.net.URISyntaxException;

import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class FilterEngineTest extends AbstractFilterEngineTest {
	
	@Test
	public void testFilterEngine() throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		load("filter-test-ontology.owl");
		
		PropertyValueFilter pvf = new PropertyValueFilter();
		// TODO
	}

}
