package org.monarchinitiative.owlsim.compute.matcher;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.eval.RandomOntologyMaker;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Tests a ProfileMatcher using a random ontology
 * 
 * @author cjm
 *
 */
public abstract class AbstractProfileMatcherPerfIT {

	protected BMKnowledgeBase kb;
	protected ProfileMatcher profileMatcher;
	private Logger LOG = Logger.getLogger(AbstractProfileMatcherPerfIT.class);
	protected boolean writeToStdout = false;

	private String getRandomIndividual() {
		List<String> inds = new ArrayList<String>(kb.getIndividualIdsInSignature());
		return inds.get((int)(Math.random() * inds.size()));
	}
	
	private BasicQuery getRandomQuery(String ind, double decay) {
		EWAHCompressedBitmap typesBM = kb.getTypesBM(ind);
		Set<String> qcids = new HashSet<String>();
		// TODO - this is not very good
		for (int p : typesBM.getPositions()) {
			if (Math.random() < decay) {
				qcids.add(kb.getClassId(p));
			}
		}
		return BasicQueryImpl.create(qcids);
	}
	
	@Test
	public void testSmallNoDecay1() throws Exception {
		create(100, 1, 50);
		q(1);
		
	}
	
	@Test
	public void testVariableDecay() throws Exception {
		create(1000, 1, 500);
		q(1);
		q(0.8);
		q(0.6);	
	}

	@Test
	public void testLarge() throws Exception {
		create(30000, 1, 9000);
		q(0.6);	
	}
	
	public void q(double decay) throws Exception {
		LOG.info("KB = "+profileMatcher.getKnowledgeBase());
		String tgt = getRandomIndividual();
		LOG.info("Target="+tgt);
		BasicQuery q = getRandomQuery(tgt, decay);
		LOG.info("Query = "+q);
		MatchSet mp = profileMatcher.findMatchProfile(q);

		JSONWriter w = new JSONWriter("target/random-kb-match-results.json");
		w.write(mp);

		if (writeToStdout) {
			System.out.println(mp);
		}
		Match topMatch = mp.getMatches().get(0);
		LOG.info("topMatch="+topMatch+" Expected="+tgt);
	}

	private void create(int numClasses, int avgParents, int numIndividuals) throws OWLOntologyCreationException {
		OWLOntology ontology = 
				RandomOntologyMaker.create(numClasses, avgParents).addRandomIndividuals(numIndividuals).getOntology();
		OWLReasonerFactory rf = new ElkReasonerFactory();
		kb = BMKnowledgeBaseOWLAPIImpl.create(ontology, rf);

		profileMatcher = createProfileMatcher(kb);
	}

	protected abstract ProfileMatcher createProfileMatcher(BMKnowledgeBase kb);

}
