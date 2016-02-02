package org.monarchinitiative.owlsim.compute.cpt;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.impl.DefaultSimplePairwiseConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.impl.TwoStateConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
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
public class DefaultSImpleConditionalProbabilityIndexTest  {

	private Logger LOG = Logger.getLogger(DefaultSImpleConditionalProbabilityIndexTest.class);

	protected BMKnowledgeBase kb;
	protected SimplePairwiseConditionalProbabilityIndex cpi;
	protected boolean writeToStdout = false;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException, NoRootException, IncoherentStateException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		LOG.info("Loading: "+fn);
		kb = loader.createKnowledgeBaseInterface();
		LOG.info("creating CPI");
		long t1 = System.currentTimeMillis();
		cpi = DefaultSimplePairwiseConditionalProbabilityIndex.create(kb);
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
		
		String leafId = kb.getClassId(kb.getClassIndex("http://x.org/leaf"));
		String x1Id = kb.getClassId(kb.getClassIndex("http://x.org/x1"));
		String x2Id = kb.getClassId(kb.getClassIndex("http://x.org/x2"));
		int[] na = kb.getIndividualCountPerClassArray();

		for (String cid : kb.getClassIdsInSignature()) {
			int cix = kb.getClassIndex(cid);
			for (String did : kb.getClassIdsInSignature()) {
				int dix = kb.getClassIndex(did);
				Double p = cpi.getConditionalProbabilityChildIsOn(cix, dix);
				System.out.println(cid + " | "+ did +" == "+p);
				int numCandP = kb.getIndividualsBM(cix).andCardinality(kb.getIndividualsBM(dix));
				System.out.println(kb.getIndividualsBM(cix).cardinality());
				System.out.println(kb.getIndividualsBM(cix).cardinality());
				System.out.println(na[cix]);
				System.out.println(na[dix]);
				System.out.println("   AND == "+numCandP);
				for (int x : kb.getIndividualsBM(cix).getPositions()) {
					System.out.println(" XX:" + kb.getIndividualId(x));
				}
				Assert.assertTrue(p >= 0.0);
				Assert.assertTrue(p <= 1.0);
				if (cid.equals(did)) {
					Assert.assertTrue(p > 0.999);
				}
			}
			
		}

		//examineCPT(expectedMatches, 4);
	}


}
