package org.monarchinitiative.owlsim.compute.weights;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

public class SpreadingActivationNetworkUtilTest {

	BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(SpreadingActivationNetworkUtilTest.class);

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		OWLLoader loader = new OWLLoader();
		LOG.info("Loading: "+fn);
		loader.load(IRI.create(Resources.getResource(fn)));
		kb = loader.createKnowledgeBaseInterface();
	}
	
	//@Test
	public void test() throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		load("SimpleDAG.owl");
		Set<String> qids = new HashSet<String>();
		Set<String> nqids = new HashSet<String>();
		qids.add("ex1");
		nqids.add("ex2");
		QueryWithNegation q = 
			QueryWithNegationImpl.create(qids, nqids);
		SpreadingActivationNetworkUtil san = new SpreadingActivationNetworkUtil(kb);
		san.propagateQuery(q);
	}

}
