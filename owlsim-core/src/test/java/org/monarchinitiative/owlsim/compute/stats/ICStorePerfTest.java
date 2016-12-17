package org.monarchinitiative.owlsim.compute.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.prefixcommons.CurieUtil;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ICStorePerfTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(ICStorePerfTest.class);
	protected boolean writeToStdout = false;
	private List<ICRun> runs;
	ICStatsCalculator icc;
	
	
	public class ICRun {
		public int size;
		public long timeStart;
		public long timeEnd;
		public long duration;
		String summary;
		

		public ICRun(int numClasses, int avgClassesPerIndividual, long t1, long t2) {
			super();
			size = numClasses;
			timeStart = t1;
			timeEnd = t2;
			duration = t2-t1;
			LOG.info("|C|="+numClasses+" |c/indiv|="+avgClassesPerIndividual+" duration="+duration);
		}

		public ICRun(int numIndividuals, long t1, long t2) {
			super();
			size = numIndividuals;
			timeStart = t1;
			timeEnd = t2;
			duration = t2-t1;
			LOG.info("|I|="+numIndividuals+" duration="+duration);
		}

	}

	@Test
	public void testCreateLarge() throws Exception {
		runs = new ArrayList<ICRun>();
		for (int i=1; i<=2; i++) {
			create(i * 2000, 2, i * 4000, (int)Math.round(Math.random()*10));
		}
		JSONWriter w = new JSONWriter("target/icc-run-stats.json");
		w.write(runs);
		
	}
	
	//@Test
	public void testAccess() throws Exception {
		runs = new ArrayList<ICRun>();
		create(2000, 2, 4000, (int)Math.round(Math.random()*10));
		for (int i=1; i<=5; i++) {
			access(i*50);
		}
		JSONWriter w = new JSONWriter("target/icc-access-stats.json");
		w.write(runs);
		
	}

	private ICRun create(int numClasses, int avgParents, int numIndividuals, int avgClassesPerIndividual) throws OWLOntologyCreationException, NoRootException {
		System.gc();
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals, avgClassesPerIndividual).getOntology();

		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf, new CurieUtil(new HashMap<String, String>()));
		icc = new ICStatsCalculator(kb);
		LOG.info("creating ICStoreSummary");
		long t1 = System.currentTimeMillis();
		icc.calculateICSummary();
		long t2 = System.currentTimeMillis();
		LOG.info("created ICStoreSummary");
		ICRun r = new ICRun(numClasses, avgClassesPerIndividual, t1,t2);
		r.summary = icc.toString();
		LOG.info("Summary="+icc.toString());
		runs.add(r);
		return r;
	}
	
	private ICRun access(int avgIndividuals) throws OWLOntologyCreationException, NoRootException {
		LOG.info("Building random subset of individuals");
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
		Set<String> individualIds = new HashSet<String>();
		while (Math.random() > (1 / (double) avgIndividuals)) {
			int ibit = (int)Math.round(Math.random()*numIndividuals);
			String individualId = kb.getIndividualId(ibit);
			individualIds.add(individualId);
		}
		LOG.info("Building DStats for subset");
		long t1 = System.currentTimeMillis();
		SetDescriptiveStatistics ds = icc.getSetDescriptiveStatisticsForIndividuals(individualIds);
		long t2 = System.currentTimeMillis();
		LOG.info("Findished building DStats for subset");
		ICRun r = new ICRun(individualIds.size(), t1, t2);
		LOG.info("Summary="+ds.toString());
		r.summary = ds.toString();
		runs.add(r);
		return r;
	}
}
