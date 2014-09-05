package org.monarchinitiative.owlsim.compute.matcher.perf;

import java.io.FileNotFoundException;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GMProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class GMProfileMatcherHPPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(GMProfileMatcherHPPerfIT.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return GMProfileMatcher.create(kb);
	}

	@Test
	public void testBasic() throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {
		loadHP();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher = createProfileMatcher(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		TestQuery tq = eval.constructTestQuery(labelMapper,
				"http://purl.obolibrary.org/obo/OMIM_270400",
				10,
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia");
		Level level = Level.INFO;
		LOG.setLevel(level );
		LOG.getRootLogger().setLevel(level);
		Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void loadHP() throws OWLOntologyCreationException {
		load("/ontology/hp.obo", "/data/omim-disease-phenotype.owl");
		
	}

}
