package org.monarchinitiative.owlsim.compute.matcher;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaivesBayesFixedWeightProfileMatcher;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class BasicProbabilisticProfileMatcherHPPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(BasicProbabilisticProfileMatcherHPPerfIT.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return NaivesBayesFixedWeightProfileMatcher.create(kb);
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
				//"Aplasia/hypoplasia of the extremities",
				"Scrotal hypoplasia",
				"Renal cyst",
				"Micrognathia");
		Level level = Level.INFO;
		LOG.setLevel(level );
		LOG.getRootLogger().setLevel(level);
		assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	private void loadHP() throws OWLOntologyCreationException {
		load("/ontologies/hp.obo", "/data/omim-disease-phenotype.owl", "/data/disorders.ttl");
		
	}

}
