package org.monarchinitiative.owlsim.compute.stats;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class CoAnnotationStatsTest {

	private BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(CoAnnotationStatsTest.class);
	private boolean writeToStdout = false;
	private OWLOntology ontology;
	private CoAnnotationStats coAnnotationStats;
	
	@Test
	public void testCoAnnotationInitialization() throws Exception {
		create(10000, 2, 5000, 20);

		long start = System.currentTimeMillis();
		LOG.info("Initializing co-annotation statistics ...");
		coAnnotationStats = new CoAnnotationStats(kb);
		long end = System.currentTimeMillis();
		LOG.info("Co-annotation statistics initialized ... " + (end - start) + " ms");
		
		start = System.currentTimeMillis();
		LOG.info("Populating co-annotation matrix ...");
		coAnnotationStats.populateFullCoAnnotationMatrix();
		end = System.currentTimeMillis();
		LOG.info("Co-annotation statistics populated ... " + (end - start) + " ms");

		LOG.info("Test done ...");
	}

	private void create(int numClasses, int avgParents, int numIndividuals,
			int avgClassesPerIndividual) throws OWLOntologyCreationException,
			NoRootException {
		LOG.info("Creating random ontology ...");
		System.gc();
		this.ontology = RandomOntologyMaker.create(numClasses, avgParents)
				.addRandomIndividuals(numIndividuals, avgClassesPerIndividual)
				.getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);
		LOG.info("Knowledge base ready ...");
	}

}
