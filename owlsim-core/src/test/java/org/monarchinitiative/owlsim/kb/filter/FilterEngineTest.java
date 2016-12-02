package org.monarchinitiative.owlsim.kb.filter;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        TypeFilter tf = new TypeFilter("http://x.org/human");
        testFilter(tf, "http://x.org/ib");

        TypeFilter tf2 = new TypeFilter("http://x.org/mammal", false, false);
        testFilter(tf2, "http://x.org/ia", "http://x.org/ib");

        TypeFilter tf3 = new TypeFilter("http://x.org/mammal", true, false);
        testFilter(tf3);

        TypeFilter tf4 = new TypeFilter("http://x.org/human", false, true);
        testFilter(tf4, "http://x.org/ia", "http://x.org/iabc");

        IdFilter tf5 = new IdFilter("http://x.org/ib");
        testFilter(tf5, "http://x.org/ib");
 
        Set<String> posIds = new HashSet<String>();
        posIds.add("human");
        posIds.add("mouse");
        AnonIndividualFilter tf6 = new AnonIndividualFilter(posIds, new HashSet<String>());
        List<String> inds6 = filterEngine.applyFilter(tf6);
        assertEquals(1, inds6.size());
        //System.out.println(tf6);
        //testFilter(tf6);
 
	}

}
