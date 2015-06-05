package org.monarchinitiative.owlsim.compute.matcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.GridProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.NaiveBayesFixedWeightTwoStateProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator.MatcherComparisonResult;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class ProfileMatcherComparisonTest extends AbstractProfileMatcherTest {

	private Logger LOG = Logger.getLogger(ProfileMatcherComparisonTest.class);

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return PhenodigmICProfileMatcher.create(kb);
	}

	@Test
	public void testPhenodigmVsGrid() throws Exception {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher1 = PhenodigmICProfileMatcher.create(kb);
		ProfileMatcher profileMatcher2 = GridProfileMatcher.create(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		eval.writeJsonTo("target/compare-phenodigm-vs-grid.js");
		MatcherComparisonResult result = eval.compareMatchers(profileMatcher1, profileMatcher2);
		LOG.info("diff: "+result.distance);
		eval.writeJson(result);
		//assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}

	@Test
	public void testPhenodigmVsBN() throws Exception {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		ProfileMatcher profileMatcher1 = PhenodigmICProfileMatcher.create(kb);
		ProfileMatcher profileMatcher2 = BayesianNetworkProfileMatcher.create(kb);
		LabelMapper labelMapper = kb.getLabelMapper();
		MatcherComparisonResult result = eval.compareMatchers(profileMatcher1, profileMatcher2);
		LOG.info("diff: "+result.distance);
		//assertTrue(eval.evaluateTestQuery(profileMatcher, tq));
		
	}
	
	@Test
	public void testAll() throws Exception {
		load();
		//LOG.info("INDS="+kb.getIndividualIdsInSignature());
		Set<ProfileMatcher> pms = new HashSet<ProfileMatcher>();
		pms.add(GridProfileMatcher.create(kb));
		pms.add(PhenodigmICProfileMatcher.create(kb));
		pms.add(BayesianNetworkProfileMatcher.create(kb));
		pms.add(NaiveBayesFixedWeightTwoStateProfileMatcher.create(kb));

		List<MatcherComparisonResult> results = eval.compareAllMatchers(pms);
		for (MatcherComparisonResult r : results) {
			LOG.info(r.matcher1Type + " -vs- "+r.matcher2Type+ " DIST= "+r.distance);
		}
		
	}

	private void load() throws OWLOntologyCreationException {
		loadSimplePhenoWithNegation();
		
	}

}
