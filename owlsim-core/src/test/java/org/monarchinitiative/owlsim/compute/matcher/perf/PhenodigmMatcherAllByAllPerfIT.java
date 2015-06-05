package org.monarchinitiative.owlsim.compute.matcher.perf;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.CompareAllByAll;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Performance test for PhenodigmICProfileMatcher using full HPO and HPO annotations,
 * all by all
 * 
 * @author cjm
 *
 */
public class PhenodigmMatcherAllByAllPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(PhenodigmMatcherAllByAllPerfIT.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return PhenodigmICProfileMatcher.create(kb);
	}

	@Test
	public void testPhenodigmAllByAll() throws Exception {
		load();
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		CompareAllByAll c = new CompareAllByAll(profileMatcher);
		c.compareAllByAll();
		
	}

	private void load() throws OWLOntologyCreationException {
		load("/ontologies/hp.obo", "/data/Homo_sapiens-data.owl");		
	}

}
