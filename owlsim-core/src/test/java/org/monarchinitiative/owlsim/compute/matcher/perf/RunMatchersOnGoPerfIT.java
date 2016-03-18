package org.monarchinitiative.owlsim.compute.matcher.perf;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.AbstractProfileMatcherTest;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesTransitionTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.JaccardSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightThreeStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator;
import org.monarchinitiative.owlsim.eval.TestQuery;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.io.ReadMappingsUtil;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * 
 * 
 * @author cjm
 *
 */
public class RunMatchersOnGoPerfIT extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(RunMatchersOnGoPerfIT.class);



	@Test
	public void testHomologyMULTI() throws Exception {
		load();
		Set<ProfileMatcher> pms = new HashSet<>();
		pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
		//pms.add(NaiveBayesFixedWeightThreeStateProfileMatcher.create(kb));
		//pms.add(BayesianNetworkProfileMatcher.create(kb));
		pms.add(PhenodigmICProfileMatcher.create(kb));
		pms.add(MaximumInformationContentSimilarityProfileMatcher.create(kb));
		pms.add(JaccardSimilarityProfileMatcher.create(kb));
		testHomology(pms);
	}
	
	@Test
	public void testHomologyBN() throws Exception {
		load();
		Set<ProfileMatcher> pms = new HashSet<>();
		pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
		pms.add(BayesianNetworkProfileMatcher.create(kb));
		testHomology(pms);
	}



	@Test
	public void testHomologyBlanket() throws Exception {
		load();
		Set<ProfileMatcher> pms = new HashSet<>();
		pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));
		pms.add(NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher.create(kb));
		testHomology(pms);
	}

	
	
	/**
	*
	 */	
	public void testHomology(Set<ProfileMatcher> pms ) throws Exception {
		Map<String, String> mappings = ReadMappingsUtil.readPairwiseMappingsFromTsv(getClass().getResource("/data/mammal-homol.tsv").getPath());
		int numInds = kb.getIndividualIdsInSignature().size();
		LOG.info("NumInds = "+numInds);
		assertTrue(numInds > 0);
		
		//LOG.getRootLogger().setLevel(Level.WARN);

		for (String g1 : mappings.keySet()) {
			if (!kb.getIndividualIdsInSignature().contains(g1)) {
				System.err.println("SKIPPING: "+g1);
				continue;
			}
			String g2 = mappings.get(g1);
			if (!kb.getIndividualIdsInSignature().contains(g2)) {
				System.err.println("SKIPPING: "+g2);
				continue;
			}
			for (ProfileMatcher pm : pms) {
				//System.out.println("Q:"+g1);
				//System.out.println(pm);
				MatchSet mp = pm.findMatchProfile(g1);
				Match m = mp.getMatchesWithId(g2);
				Integer rank = null;
				Integer erank = null;
				if (m != null) {
					rank = m.getRank();
					erank = mp.getMatchesWithOrBelowRank(rank).size();
				}
				System.out.println(" "+g1+"\t"+g2+"\t"+pm.getShortName()+"\t"+erank+"\t"+rank+"\t"+mp.getExecutionMetadata().getDuration());
			}
		}
	}
	
	
	@Test
	public void estimateAccuracy() throws Exception {
		load();
		Map<String, String> mappings = ReadMappingsUtil.readPairwiseMappingsFromTsv(getClass().getResource("/data/mammal-homol.tsv").getPath());
		NaiveBayesFixedWeightTwoStateProfileMatcher pm = NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb);
		for (String g1 : mappings.keySet()) {
			if (!kb.getIndividualIdsInSignature().contains(g1)) {
				continue;
			}
			String g2 = mappings.get(g1);
			if (!kb.getIndividualIdsInSignature().contains(g2)) {
				continue;
			}
			pm.compare(g1, g2);
		}
	}

	private void load() throws OWLOntologyCreationException, IOException {
		OWLLoader loader = new OWLLoader();
		loader.load(getClass().getResource("/ontologies/go.obo").getFile());
		loader.loadDataFromTsv(getClass().getResource("/data/mgi.assocs").getFile());
		loader.loadDataFromTsv(getClass().getResource("/data/human.assocs").getFile());
		kb = loader.createKnowledgeBaseInterface();
		
	}

}
