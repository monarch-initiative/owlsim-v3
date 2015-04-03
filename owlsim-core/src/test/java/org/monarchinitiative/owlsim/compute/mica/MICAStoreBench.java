package org.monarchinitiative.owlsim.compute.mica;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.caliper.Benchmark;

/**
 * Tests performance of MICAStore using caliper
 * 
 * TODO - fix or remove
 * 
 * @author cjm
 *
 */
public class MICAStoreBench extends Benchmark {

	protected BMKnowledgeBase kb;
	protected MICAStoreImpl micaStore;
	private Logger LOG = Logger.getLogger(MICAStoreBench.class);
	protected boolean writeToStdout = false;


	public static class Benchmark1 {
		void timeNanoTime(int reps) {
			for (int i = 0; i < reps; i++) {
				System.nanoTime();
			}
		}
	}


	public static void main(String[] args) {
//	    CaliperMain.main(Benchmark1.class, args);
	}

	public static void timeMyOperation(int reps) throws OWLOntologyCreationException, NoRootException {
		MICAStoreBench b = new MICAStoreBench();
		for (int i = 0; i < reps; i++) {
			b.create(300, 2, 700);
		}
	}



	private void create(int numClasses, int avgParents, int numIndividuals) throws OWLOntologyCreationException, NoRootException {
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);
		LOG.info("creating MICAStore");
		MICAStoreImpl micaStore = new MICAStoreImpl(kb);
		LOG.info("created MICAStore");


	}


}
