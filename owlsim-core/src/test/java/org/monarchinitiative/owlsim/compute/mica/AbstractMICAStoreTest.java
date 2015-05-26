package org.monarchinitiative.owlsim.compute.mica;

import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

/**
 * Common methods for testing performance of MICAStore
 * 
 * @author cjm
 *
 */
public abstract class AbstractMICAStoreTest {

	protected BMKnowledgeBase kb;
	protected MICAStore micaStore;
	private Logger LOG = Logger.getLogger(AbstractMICAStoreTest.class);
	protected boolean writeToStdout = false;
	KBStatsCalculator kbsc;

	protected long load(String fn, String... ontfns) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
		OWLLoader loader = new OWLLoader();
		LOG.info("Loading: "+fn);
		loader.load(IRI.create(Resources.getResource(fn)));
		for (String ontfn : ontfns) {
			URL res = getClass().getResource(ontfn);
			LOG.info("RES="+res);
			loader.loadOntologies(res.getFile());
		}
		kb = loader.createKnowledgeBaseInterface();
		kbsc = new KBStatsCalculator(kb);
		LOG.info("creating MICAStore");
		long t1 = System.currentTimeMillis();
		micaStore = new MICAStoreImpl(kb);
		long t2 = System.currentTimeMillis();
		LOG.info("created MICAStore");
		kbsc.calculateStats();
		long dt = (t2-t1);
		LOG.info("Duration: "+dt);
		return dt;
	}




}
