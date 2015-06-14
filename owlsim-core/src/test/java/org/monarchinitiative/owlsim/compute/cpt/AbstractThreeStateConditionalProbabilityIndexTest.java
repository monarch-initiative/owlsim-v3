package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;
import java.util.Map;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.compute.cpt.impl.NodeProbabilities;
import org.monarchinitiative.owlsim.compute.cpt.impl.ThreeStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class AbstractThreeStateConditionalProbabilityIndexTest  {

	private Logger LOG = Logger.getLogger(AbstractThreeStateConditionalProbabilityIndexTest.class);

	protected BMKnowledgeBase kb;
	protected ThreeStateConditionalProbabilityIndex cpi;
	protected boolean writeToStdout = false;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException, IncoherentStateException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		LOG.info("Loading: "+fn);
		kb = loader.createKnowledgeBaseInterface();
		LOG.info("creating CPI");
		long t1 = System.currentTimeMillis();
		cpi = ThreeStateConditionalProbabilityIndex.create(kb);
		for (String ci : kb.getClassIdsInSignature()) {
			LOG.info("CLASS: "+ci+" "+kb.getClassIndex(ci));
			LOG.info(" SUPS: "+kb.getDirectSuperClassesBM(ci));
			LOG.info(" INDS: "+kb.getIndividualsBM(ci).getPositions());
			for (int ix : kb.getIndividualsBM(ci).getPositions()) {
				LOG.info(" IND: "+kb.getIndividualId(ix));
			}
		}
		cpi.calculateConditionalProbabilities(kb);
		long t2 = System.currentTimeMillis();
		LOG.info("created CPI");
		LOG.info("Duration: "+(t2-t1));
	}


	protected void examineCPT(String[] expectedMatches, int numExpected) {
		int numFound = 0;
		for (String ci : kb.getClassIdsInSignature()) {
			LOG.info("CLASS: "+ci);
			int clsIndex = kb.getClassIndex(ci);
			LOG.info("  INSTANCES: "+kb.getIndividualsBM(ci));
			for (int i=0; i < cpi.getNumberOfParentStates(clsIndex); i++) {
				
				numFound  += getMatches(ci, i, expectedMatches);


			}
		}
		Assert.assertEquals(numExpected, numFound);		
	}

	protected int getMatches(String ci, int i, String[] matchStrings) {
		int numFound = 0;

		int clsIndex = kb.getClassIndex(ci);

		//LOG.info("stateIx="+i);
		Map<Integer, Character> psm = cpi.getParentsToStateMapping(clsIndex, i);
		//LOG.info("PSM="+psm);
		StringBuilder sb = new StringBuilder();
		for (int pix : psm.keySet()) {
			String pid = kb.getClassId(pix);
			sb.append(pid+" = "+psm.get(pix)+" ; ");
		}
		NodeProbabilities cprd = cpi.getConditionalProbabilityDistribution(clsIndex, i);

		LOG.info("PRD="+cprd);

		double cpOn = cprd.prOn;
		long cppOn = Math.round(cpOn * 100);
		double cpOff = cprd.prOff;
		long cppOff = Math.round(cpOff * 100);
		String rptOn =  "Pr(" + ci + " =ON |  "+sb.toString()+" ) = "+cppOn + " %";
		String rptOff = "Pr(" + ci + " =OFF |  "+sb.toString()+" ) = "+cppOff + " %";
		LOG.info(rptOn);
		LOG.info(rptOff);

		for (String m : matchStrings) {
			if (rptOn.equalsIgnoreCase(m))
				numFound ++;
			if (rptOff.equalsIgnoreCase(m))
				numFound ++;
		}

		return numFound;
	}



}
