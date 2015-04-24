package org.monarchinitiative.owlsim.compute.mica;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator.KBStats;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class MICAStorePerfIT {

	protected BMKnowledgeBase kb;
	protected MICAStoreImpl micaStore;
	private Logger LOG = Logger.getLogger(MICAStorePerfIT.class);
	protected boolean writeToStdout = false;
	private List<MICARun> runs;
	KBStatsCalculator kbsc;
	
	
	public class MICARun {
		public int size;
		public long timeStart;
		public long timeEnd;
		public long duration;
		KBStats kbstats;

		public  MICARun(int numClasses, long t1, long t2) {
			super();
			size = numClasses;
			timeStart = t1;
			timeEnd = t2;
			duration = t2-t1;
			LOG.info("|C|="+numClasses+" duration="+duration);
		}
	}

	@Test
	public void testLarge() throws Exception {
		runs = new ArrayList<MICARun>();
		for (int i=1; i<=2; i++) {
			create(i * 2000, 2, i * 4000);
		}
		JSONWriter w = new JSONWriter("target/mica-run-stats.json");
		w.write(runs);
	}

	private MICARun create(int numClasses, int avgParents, int numIndividuals) throws OWLOntologyCreationException, NoRootException {
		System.gc();
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);
		kbsc = new KBStatsCalculator(kb);
		LOG.info("creating MICAStore");
		long t1 = System.currentTimeMillis();
		MICAStoreImpl micaStore = new MICAStoreImpl(kb);
		long t2 = System.currentTimeMillis();
		LOG.info("created MICAStore");
		MICARun r = new MICARun(numClasses, t1,t2);
		r.kbstats = kbsc.calculateStats();
		runs.add(r);
		return r;
	}


}
