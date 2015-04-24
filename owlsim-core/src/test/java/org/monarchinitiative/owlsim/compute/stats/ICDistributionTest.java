package org.monarchinitiative.owlsim.compute.stats;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class ICDistributionTest {

	protected BMKnowledgeBase kb;
	private Logger LOG = Logger.getLogger(ICDistributionTest.class);
	protected boolean writeToStdout = false;
	ICStatsCalculator icc;
	private static double cutoff = 0.95;
	OWLOntology ontology;

	@Test
	public void testSelfComparisonTTest() throws Exception {
		Random r = new Random(1);
		create(2000, 2, 4000, 10);
		for (int i=1; i<=10; i++) {
			int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = r.nextInt(numIndividuals);
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
		Random r = new Random(1);
		create(2000, 2, 4000, 10);
		for (int i=1; i<=10; i++) {
			int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = r.nextInt(numIndividuals);
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
		Random r = new Random(1);
		create(2000, 2, 4000, 10);
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
		for (int i=1; i<=25; i++) {
			int numClasses = 0;
			int randomibit = -1;
			while (numClasses < 2) {
				randomibit = r.nextInt(numIndividuals);
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
		Random r = new Random(1);
		create(2000, 2, 4000, 10);
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();
		for (int i=0; i<=10; i++) {
			//get a reference
			int numClassesRef = 0;
			int refibit = -1;
			while (numClassesRef < i+2) {
				refibit = r.nextInt(numIndividuals);
				numClassesRef = (int)icc.getICStatsForIndividual(refibit).getN();
			}
			String refindiv = kb.getIndividualId(refibit);
			ICDistribution refDistro = new ICDistribution(icc.getICStatsForIndividual(refibit), 0.001);		

			for (int j=0; j<=3; j++) {
				//get a candidate
				int numClasses = 0;
				int randomibit = -1;
				while (numClasses < 5) {
					randomibit = r.nextInt(numIndividuals);
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
	
	
	@Test
	public void testSelfBySubsetComparisonTTest() throws Exception {
		Random r = new Random(1);
		Random r2 = new Random(1);
		
		create(2000, 2, 4000, 10);
		int numIndividuals = (int)icc.getICSummaryForAllIndividuals().getN().getN();

		//make distribution in 10 bins
		double binSize = icc.getICSummaryForAllIndividuals().getMax().getMax() / 10;

		for (int i=1; i<=3; i++) {
			int numClasses = 0;
			int randomibit = -1;
			DescriptiveStatistics referenceDS = new DescriptiveStatistics();
			while (numClasses < 2) {
				randomibit = r.nextInt(numIndividuals);
				referenceDS = icc.getICStatsForIndividual(randomibit);
				numClasses = (int)referenceDS.getN();
//				LOG.info("Skipping individual "+randomibit+" with too few classes");
			}
			//first, generate the reference distribution
			ICDistribution referenceDistro = new ICDistribution(referenceDS, binSize);
			LOG.info("IC vals (ref): "+Arrays.toString(referenceDS.getValues()));
			
			String indiv = kb.getIndividualId(randomibit);
			EWAHCompressedBitmap dbm = kb.getDirectTypesBM(indiv);
			
			//get the superclasses
			EWAHCompressedBitmap supersbm = kb.getTypesBM(indiv).andNot(dbm);
			List<Integer> superbits = supersbm.getPositions();

			Set<String> supers = kb.getClassIds(supersbm);
			LOG.info("Supers="+supers);

			numClasses = 0;
			EWAHCompressedBitmap filteredAttsBM = new EWAHCompressedBitmap();
			String classId = null;

			//for the tTest to work, we need at least two items
			while (numClasses < 2) {			
				//select one random superclasses as a filter
				int randomcbit = r2.nextInt(superbits.size());
				classId = kb.getClassId(superbits.get(randomcbit));
				//given one of the random superclasses to filter with,
				//get the subset of classes that are subclasses of the filter
				filteredAttsBM = kb.getFilteredDirectTypesBM(indiv, classId);
				numClasses = kb.getClassIds(filteredAttsBM).size();
				LOG.info("|subclasses of "+classId+"|="+numClasses);
			}
			
			DescriptiveStatistics filteredDS = icc.getICStatsForAttributesByBM(filteredAttsBM);
			ICDistribution filteredDistro = new ICDistribution(filteredDS, binSize);
//			LOG.info("IC vals ("+classId+" filter): "+Arrays.toString(filteredDS.getValues()));
			
			DescriptiveStatistics subsetDS = icc.getICStatsForAttributeSubsetForIndividual(indiv, classId);
			ICDistribution subsetDistro = new ICDistribution(subsetDS, binSize);

//			LOG.info("IC vals ("+classId+" subset): "+Arrays.toString(subsetDistro.getDescriptiveStatistics().getValues()));

			//the filtered and subset methods should give the same results
			double pvalue = subsetDistro.tTest(filteredDistro);
			LOG.info("filtered v subset (tTest), p="+pvalue);

			Assert.assertTrue("filtered v subset (tTest), should have pvalue=1", pvalue==1.0);
			pvalue = subsetDistro.tTest(referenceDistro);

			//if the subset != reference, then presumably it has less elements, 
			//and therefore is not identical, so p<1.0
			if (!subsetDistro.equals(referenceDistro)) {
				LOG.info("I1subset v all (tTest) should have p<1.0+, p="+pvalue);  
				Assert.assertTrue("I1subset v all (tTest) should have p<"+1.0, pvalue<1.0);  //most likely they will not give the same distro
			} else {
				LOG.info("subset == all (tTest) should have p<1.0+, p="+pvalue);  
				Assert.assertTrue("I1subset v all (tTest) should have p=="+1.0, pvalue==1.0);  //most likely they will not give the same distro				
			}
		}
	}	
	
	/**
	 * @param n - depth
	 * @return - a class at the specified depth with which to filter
	 */
	private String fetchRandomClassByDepth(int n) {
		Random r = new Random(1);
		//get direct subclasses of the root
		int index = kb.getRootIndex();
		for (int i=0; i<n; i++) {
			EWAHCompressedBitmap directSubclasses = kb.getStoredDirectSubClassIndex()[index];
			//choose a random one of them to be the class
			List<Integer> classBits = directSubclasses.getPositions();
			LOG.info("Found "+classBits.size()+" subclasses of "+kb.getClassId(index));
			index = (int) r.nextInt(classBits.size());
			LOG.info("Setting index to "+index);
		}
		return kb.getClassId(index);
	}
	
	private void create(int numClasses, int avgParents, int numIndividuals, int avgClassesPerIndividual) throws OWLOntologyCreationException, NoRootException {
		System.gc();
		this.ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals, avgClassesPerIndividual).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);
		icc = new ICStatsCalculator(kb);
		LOG.info("creating ICStoreSummary");
		icc.calculateICSummary();
		LOG.info("created ICStoreSummary");
		LOG.info("Summary="+icc.toString());
	}
	
	
	private Set<String> getClassIdsFromBM(EWAHCompressedBitmap bm) {
		Set<String> cids = new HashSet<String>();
		for (int c : bm) {
			cids.add(kb.getClassId(c));
		}
		return cids;		
	}

	
	
	
}
