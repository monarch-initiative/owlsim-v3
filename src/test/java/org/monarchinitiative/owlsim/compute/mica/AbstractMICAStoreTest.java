package org.monarchinitiative.owlsim.compute.mica;

import java.net.URISyntaxException;

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
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public abstract class AbstractMICAStoreTest {

	protected BMKnowledgeBase kb;
	protected MICAStoreImpl micaStore;
	private Logger LOG = Logger.getLogger(AbstractMICAStoreTest.class);
	protected boolean writeToStdout = false;
	KBStatsCalculator kbsc;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		LOG.info("Loading: "+fn);
		kb = loader.createKnowledgeBaseInterface();
		kbsc = new KBStatsCalculator(kb);
		LOG.info("creating MICAStore");
		long t1 = System.currentTimeMillis();
		micaStore = new MICAStoreImpl(kb);
		long t2 = System.currentTimeMillis();
		LOG.info("created MICAStore");
		kbsc.calculateStats();
		LOG.info("Duration: "+(t2-t1));
	}




}
