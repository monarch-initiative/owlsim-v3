package org.monarchinitiative.owlsim.kb.filter;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

/**
 * 
 * @author cjm
 *
 */
public abstract class AbstractFilterEngineTest {

	protected BMKnowledgeBase kb;
	protected FilterEngine filterEngine;
	protected MICAStoreImpl micaStore;
	private Logger LOG = Logger.getLogger(AbstractFilterEngineTest.class);
	protected boolean writeToStdout = false;
	KBStatsCalculator kbsc;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		LOG.info("Loading: "+fn);
		kb = loader.createKnowledgeBaseInterface();
		filterEngine = FilterEngine.create(kb);
	}
	
	protected void testFilter(Filter filter, Set<String> expectedIds) {
		List<String> inds = filterEngine.applyFilter(filter);
		Assert.assertEquals(expectedIds.size(), inds.size());
		for (String ind : inds) {
			Assert.assertTrue(expectedIds.contains(ind));
		}
	}




}
