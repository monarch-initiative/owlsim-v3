package org.monarchinitiative.owlsim.compute.runner;

import static org.junit.Assert.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Tests performance of RunEngine
 * 
 * @author cjm
 *
 */
public class RunEngineTest {

	private Logger LOG = Logger.getLogger(RunEngineTest.class);


	@Test
	public void runnerTest() throws IOException, InstantiationException, IllegalAccessException, OWLOntologyCreationException, UnknownFilterException, IncoherentStateException {
		RunConfiguration rc = loadConfiguration("runner/run.json");
		//System.out.println(rc.getDescription());
		RunEngine re = new RunEngine(rc);
		re.execute();
		re.toJsonFile("target/run-results.json");

		RunConfiguration resultObj = re.getRunConfiguration();
		
		List<Match> pwMatches = resultObj.getPairwiseJobs().get(0).getMatchSet().getMatches();
		// this job has an id filter that guarantees exactly one result
		assertTrue(pwMatches.size() == 1);
		assertTrue(pwMatches.get(0).getMatchLabel().equals("ind-big-heart-big-brain"));

		List<Match> searchMatches = resultObj.getSearchJobs().get(0).getMatchSet().getMatches();
		assertTrue(searchMatches.size() > 10);
		// self-match
		assertTrue(searchMatches.get(0).getMatchLabel().equals("ind-small-heart-small-brain"));
		assertTrue(searchMatches.get(0).getScore() > 99.0);

	}


	protected RunConfiguration loadConfiguration(String fn) throws IOException {
		URL rfn = Resources.getResource(fn);
		return RunnerUtil.generateRunConfigurationFromJsonFile(rfn.getFile());
		
	}

}
