package org.monarchinitiative.owlsim.compute.matcher;

import java.io.FileNotFoundException;

import org.monarchinitiative.owlsim.compute.matcher.impl.MaximumInformationContentSimilarityProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class MaximumInformationContentSimilarityProfileMatcherMPTest extends AbstractProfileMatcherMPTest {

	protected ProfileMatcher createProfileMatcher(BMKnowledgeBase kb) {
		return MaximumInformationContentSimilarityProfileMatcher.create(kb);
	}

	@Override
	public void testSgDiseaseExact() throws OWLOntologyCreationException, FileNotFoundException, NonUniqueLabelException, UnknownFilterException {
		testSgDiseaseExact(DISEASE.sg, null);

	}

	@Override
	public void testSgDiseaseLeaveOneOut() throws Exception {
		testSgDiseaseLeaveOneOut(DISEASE.sg, null);
	}


	@Override
	public void testMultiPhenotypeDisease() throws Exception {
		// ep, pd and foo all have equal ranking by max IC
		testMultiPhenotypeDisease(DISEASE.sg, null); // based on rarity of phenotype
	}


	@Override
	public void testEpDiseaseFuzzy() throws Exception {
		testEpDiseaseFuzzy(null, null);
	}


	@Override
	public void testNervousSystemDisease() throws Exception {
		testNervousSystemDisease(DISEASE.ep, null);
	}
	@Override
	public void testPdDisease() throws Exception {
		testNervousSystemDisease(DISEASE.pd, 100);
		
	}
}
