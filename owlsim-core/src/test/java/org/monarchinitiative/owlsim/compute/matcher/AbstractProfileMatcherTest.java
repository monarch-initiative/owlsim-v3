package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class AbstractProfileMatcherTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(AbstractProfileMatcherTest.class);
	protected boolean writeToStdout = true;
	protected ProfileMatchEvaluator eval = new ProfileMatchEvaluator();


	protected boolean isUseLabels() {
		return false;
	}
	
	protected String getId(String label) {
		if (isUseLabels()) {
			try {
				return kb.getLabelMapper().lookupByUniqueLabel(label);
			} catch (NonUniqueLabelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return "http://x.org/"+label;
	}

	protected TestQuery getTestQuery(ProfileQuery q, String expectedId, int maxRank) {
		return new TestQuery(q, getId(expectedId), maxRank);
	}

	protected void getTestQuery(ProfileQuery q, String expectedId) {
		getTestQuery(q, expectedId, 1);
	}

	protected void load(String fn) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		loader.load("src/test/resources/"+fn);
		kb = loader.createKnowledgeBaseInterface();
	}

	protected void load(String fn, String... datafns) throws OWLOntologyCreationException {
		OWLLoader loader = new OWLLoader();
		LOG.info("R="+fn);
		loader.load(getClass().getResource(fn).getFile());
		for (String datafn : datafns) {
			LOG.info("R="+datafn);
			loader.loadData(getClass().getResource(datafn).getFile());
		}
		kb = loader.createKnowledgeBaseInterface();
	}
	

	protected void search(ProfileMatcher profileMatcher,
			String expectedId, int maxRank,
			String... labels) throws NonUniqueLabelException, OWLOntologyCreationException, FileNotFoundException, UnknownFilterException {
		TestQuery tq = constructTestQuery(expectedId, maxRank, labels);
		evaluateTestQuery(profileMatcher, tq);
	}	

	@Deprecated
	protected TestQuery constructTestQuery(String expectedId, int maxRank,
			String... labels) {
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		for (String label: labels) {
			if (label.startsWith("not ")) {
				nqids.add(getId(label.replaceFirst("not ", "")));
			}
			else {
				qids.add(getId(label));
			}
		}
		LOG.info("QIDS="+qids);
		ProfileQuery q;
		if (nqids.size() == 0)
			q = ProfileQueryImpl.create(qids);
		else {
			LOG.info("NQIDS="+nqids);
			q = QueryWithNegationImpl.create(qids, nqids);
		}
		TestQuery tq = new TestQuery(q, getId(expectedId), maxRank);
		return tq;
	}
	

	// given a test query (a query plus expected results) and a matcher,
	// run the matcher with the query and evaluate results
	protected void evaluateTestQuery(ProfileMatcher profileMatcher, TestQuery tq) throws OWLOntologyCreationException, NonUniqueLabelException, FileNotFoundException, UnknownFilterException {

		Assert.assertTrue(eval.evaluateTestQuery(profileMatcher, tq));


	}

}
