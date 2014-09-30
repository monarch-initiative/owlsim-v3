package org.monarchinitiative.owlsim.compute.stats;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.ICDistribution;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

public class ICDistributionTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(ICDistributionTest.class);
	protected boolean writeToStdout = false;
	ICStatsCalculator icc;
	private static double cutoff = 0.95;

	@Test
	public void testSelfComparisonTTest() throws Exception {
		create(2000, 2, 4000, 10);
		for (int i=1; i<=10; i++) {
			int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = (int)Math.round(Math.random()*numIndividuals);
				numClasses = (int)icc.getICStatsForIndividual(randomibit).getN();
//				LOG.info("Skipping individual "+randomibit+" with too few classes");
			}
//			LOG.info("Found individual with "+numClasses+" classes.");
			String indiv = kb.getIndividualId(randomibit);
			ICDistribution randomReferenceDistro = new ICDistribution(icc.getICStatsForIndividual(randomibit), 0.001);		
			double pvalue = randomReferenceDistro.tTest(randomReferenceDistro);
			LOG.info("I1 v I1 (tTest), p="+pvalue); //should be 1.0
			Assert.assertTrue(indiv+" pvalue=1", (pvalue>cutoff));
		}
	}	
	
	@Test
	public void testSelfComparisonOneWayAnova() throws Exception {
		create(2000, 2, 4000, 10);
		for (int i=1; i<=10; i++) {
			int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = (int)Math.round(Math.random()*numIndividuals);
				numClasses = (int)icc.getICStatsForIndividual(randomibit).getN();
//				LOG.info("Skipping individual "+randomibit+" with too few classes");
			}
//			LOG.info("Found individual with "+numClasses+" classes.");
			String indiv = kb.getIndividualId(randomibit);
			ICDistribution randomReferenceDistro = new ICDistribution(icc.getICStatsForIndividual(randomibit), 0.001);		
			double pvalue = randomReferenceDistro.oneWayAnovaPValue(randomReferenceDistro);
			LOG.info("I1 v I1 (anova), p="+pvalue); //should be 1.0
			Assert.assertTrue(indiv+" pvalue=1", (pvalue>cutoff));
		}
	}	
	
	@Test
	public void testSelfComparisonKS() throws Exception {
		create(2000, 2, 4000, 10);
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
		for (int i=1; i<=25; i++) {
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = (int)Math.round(Math.random()*numIndividuals);
				numClasses = (int)icc.getICStatsForIndividual(randomibit).getN();
//				LOG.info("Skipping individual "+randomibit+" with too few classes");
			}
//			LOG.info("Found individual with "+numClasses+" classes.");
			String indiv = kb.getIndividualId(randomibit);
			ICDistribution randomReferenceDistro = new ICDistribution(icc.getICStatsForIndividual(randomibit), 0.001);		
			double pvalue = randomReferenceDistro.kolmogorovSmirnovTest(randomReferenceDistro);
			LOG.info("|C|="+numClasses+". I1 v I1 (K-S), p="+pvalue); //should be 1.0
			Assert.assertTrue(indiv+" pvalue=1", (pvalue>cutoff));
		}
	}	
	
	@Test
	public void testRandomComparisonProbabilities() throws Exception {
		create(2000, 2, 4000, 10);
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
		for (int i=0; i<=10; i++) {
			//get a reference
			int numClassesRef = 0;
			int refibit = -1;
			while (numClassesRef < i+2) {
				refibit = (int)Math.round(Math.random()*numIndividuals);
				numClassesRef = (int)icc.getICStatsForIndividual(refibit).getN();
			}
			String refindiv = kb.getIndividualId(refibit);
			ICDistribution refDistro = new ICDistribution(icc.getICStatsForIndividual(refibit), 0.001);		

			for (int j=0; j<=3; j++) {
				//get a candidate
				int numClasses = 0;
				int randomibit = -1;
				while (numClasses < 5) {
					randomibit = (int)Math.round(Math.random()*numIndividuals);
					numClasses = (int)icc.getICStatsForIndividual(randomibit).getN();
				}
				String indiv = kb.getIndividualId(randomibit);
				ICDistribution randomDistro = new ICDistribution(icc.getICStatsForIndividual(randomibit), 0.001);		

				double pvalue = 0.0;
				pvalue= randomDistro.tTest(refDistro);
				LOG.info("I="+refindiv+" v "+indiv+" |C|=("+numClassesRef+","+numClasses+") (tTest), p="+pvalue);

				pvalue= randomDistro.oneWayAnovaPValue(refDistro);
				LOG.info("I="+refindiv+" v "+indiv+" |C|=("+numClassesRef+","+numClasses+") (anova), p="+pvalue);

				pvalue= randomDistro.kolmogorovSmirnovTest(refDistro);
				LOG.info("I="+refindiv+" v "+indiv+" |C|=("+numClassesRef+","+numClasses+") (K-S), p="+pvalue);

			}
		}
	}
	
	
	private void create(int numClasses, int avgParents, int numIndividuals, int avgClassesPerIndividual) throws OWLOntologyCreationException, NoRootException {
		System.gc();
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals, avgClassesPerIndividual).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);
		icc = new ICStatsCalculator(kb);
		LOG.info("creating ICStoreSummary");
		icc.calculateICSummary();
		LOG.info("created ICStoreSummary");
		LOG.info("Summary="+icc.toString());
	}
	
	
	
}
