package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
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
public class AbstractTwoStateConditionalProbabilityIndexTest  {

	private Logger LOG = Logger.getLogger(AbstractTwoStateConditionalProbabilityIndexTest.class);

	protected BMKnowledgeBase kb;
	protected ConditionalProbabilityIndex cpi;
	protected boolean writeToStdout = false;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException, IncoherentStateException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		LOG.info("Loading: "+fn);
		kb = loader.createKnowledgeBaseInterface();
		LOG.info("creating CPI");
		long t1 = System.currentTimeMillis();
		cpi = TwoStateConditionalProbabilityIndex.create(kb);
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
		List<String> priorProbStrs = new ArrayList<String>();
		for (int pix : psm.keySet()) {
			String pid = kb.getClassId(pix);
			priorProbStrs.add(pid+" = "+psm.get(pix));
			//sb.append(pid+" = "+psm.get(pix)+" ; ");
		}
		Collections.sort(priorProbStrs);
		
		String ppStr = priorProbStrs.stream().collect(Collectors.joining(", "));
		Double cp = cpi.getConditionalProbabilityChildIsOn(clsIndex, i);
		long cpp = Math.round(cp * 100);
		String rpt = "Pr(" + ci + " |  "+ppStr+" ) = 0."+cpp;
		LOG.info(rpt);


		for (String m : matchStrings) {
			if (rpt.equalsIgnoreCase(m))
				numFound ++;
		}

		return numFound;
	}



}
