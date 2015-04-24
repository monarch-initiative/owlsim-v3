package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.mica.AbstractMICAStoreTest;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

/**
 * Tests performance of MICAStore
 * 
 * @author cjm
 *
 */
public class ConditionalProbabilityIndexTest  {

	private Logger LOG = Logger.getLogger(ConditionalProbabilityIndexTest.class);

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

	@Test
	public void cptTest() throws OWLOntologyCreationException, NoRootException, URISyntaxException, NonUniqueLabelException, IncoherentStateException {
		load("SimpleDAG.owl");
		LabelMapper lm = kb.getLabelMapper();
		boolean okx1x2 = false;

		String root = "http://x.org/root";	
		String eroot = "http://x.org/eroot";	
		String x1 = "http://x.org/x1";	
		String x2 = "http://x.org/x2";	
		String leaf = "http://x.org/leaf";	

		int numFound = 0;

		for (String ci : kb.getClassIdsInSignature()) {
			LOG.info("CLASS: "+ci);
			int clsIndex = kb.getClassIndex(ci);
			LOG.info("  INSTANCES: "+kb.getIndividualsBM(ci));
			for (int i=0; i < cpi.getNumberOfParentStates(clsIndex); i++) {
				//LOG.info("stateIx="+i);
				Map<Integer, Character> psm = cpi.getParentsToStateMapping(clsIndex, i);
				//LOG.info("PSM="+psm);
				StringBuilder sb = new StringBuilder();
				for (int pix : psm.keySet()) {
					String pid = kb.getClassId(pix);
					sb.append(pid+" = "+psm.get(pix)+" ; ");
				}
				Double cp = cpi.getConditionalProbability(clsIndex, i);
				long cpp = Math.round(cp * 100);
				String rpt = "Pr(" + ci + " |  "+sb.toString()+" ) = 0."+cpp;
				LOG.info(rpt);

				if (rpt.equals("Pr(http://x.org/leaf |  http://x.org/x2 = u ; http://x.org/ex1 = u ;  ) = 0.20")) {
					numFound++;
				}
				if (rpt.equals("Pr(http://x.org/leaf |  http://x.org/x2 = t ; http://x.org/ex1 = u ;  ) = 0.33")) {
					numFound++;
				}
				if (rpt.equals("Pr(http://x.org/leaf |  http://x.org/x2 = u ; http://x.org/ex1 = t ;  ) = 0.33")) {
					numFound++;
				}
				if (rpt.equals("Pr(http://x.org/leaf |  http://x.org/x2 = t ; http://x.org/ex1 = t ;  ) = 0.50")) {
					numFound++;
				}

			}
		}
		Assert.assertEquals(4, numFound);
	}


}
