package org.monarchinitiative.owlsim.compute.eval.data;

import java.util.HashMap;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.eval.data.BackgroundData;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.prefixcommons.CurieUtil;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class MakeBackgroundDataTest {

	
	private Logger LOG = Logger.getLogger(MakeBackgroundDataTest.class);
	
	private BMKnowledgeBase kb;
	
	@Test
	public void basicTest() throws OWLOntologyCreationException {
		LOG.info("Creating ontology");
		this.create(2000,2,4000);
		LOG.info("Finished creating ontology");
	
		BackgroundData bkgd = new BackgroundData(kb);
		
		bkgd.setMaxLengthFromInstances();
		
		int n=100;
		LOG.info("Creating "+n+" random datasets with maxLength="+bkgd.getMaxLength());
		EWAHCompressedBitmap[] bms = bkgd.createRandom(n);
		//figure out the average length
		SummaryStatistics ss = new SummaryStatistics();
		for (int i=0; i<bms.length; i++) {
			ss.addValue(bms[i].cardinality()); 
		}
		LOG.info("Created "+bms.length+" sets with average length="+ss.getMean());
		Assert.assertTrue("created "+bms.length, bms.length==n);
		
		LOG.info("Creating "+n+" random weighted datasets");
		bms = bkgd.createRandomWeightedByInstances(n);
		//figure out the average length
		ss = new SummaryStatistics();
		for (int i=0; i<bms.length; i++) {
			ss.addValue(bms[i].cardinality()); 
		}
		LOG.info("Created "+bms.length+" sets with average length="+ss.getMean());
		Assert.assertTrue("created "+bms.length, bms.length==n);
		
	}
	
	private void create(int numClasses, int avgParents, int numIndividuals) throws OWLOntologyCreationException {
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf, new CurieUtil(new HashMap<String, String>()));
	}
	
}
