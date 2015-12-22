package org.monarchinitiative.owlsim.compute.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.IdFilter;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Executes a set of jobs specified by {@link RunConfiguration}
 * 
 * @author cjm
 *
 */
public class RunEngine {

	private Logger LOG = Logger.getLogger(RunEngine.class);

	RunConfiguration runConfiguration;
	ProfileMatcher profileMatcher;
	BMKnowledgeBase kb;

	Class[] matcherClasses = {
			PhenodigmICProfileMatcher.class,
			BayesianNetworkProfileMatcher.class
	};

	/**
	 * @param runConfiguration
	 */
	public RunEngine(RunConfiguration runConfiguration) {
		super();
		this.runConfiguration = runConfiguration;
	}



	public RunConfiguration getRunConfiguration() {
		return runConfiguration;
	}



	public void setRunConfiguration(RunConfiguration runConfiguration) {
		this.runConfiguration = runConfiguration;
	}



	protected BMKnowledgeBase createKnowledgeBase(List<String> fns) throws OWLOntologyCreationException {
		OWLLoader loader = null;
		for (String fn : fns) {
			if (loader == null) {
				loader = new OWLLoader();
				loader.load(fn);
			}
			else {
				loader.loadOntologies(fn);
			}
		}
		return loader.createKnowledgeBaseInterface();
	}

	public ProfileQuery createProfileQuery(Job job) {
		ProfileQuery q = null;
		if (job.getQueryIndividual() != null) {
			q = profileMatcher.createProfileQuery(job.getQueryIndividual());
		}
		if (job.getQueryClassIds() != null) {
			if (job.getNegatedQueryClassIds() != null) {
				q = QueryWithNegationImpl.create(job.getQueryClassIds(), 
						job.getNegatedQueryClassIds());
			}
			else {
				q = ProfileQueryImpl.create(job.getQueryClassIds());
			}
		}

		return q;
	}

	public void execute() throws IOException, InstantiationException, IllegalAccessException, OWLOntologyCreationException, UnknownFilterException, IncoherentStateException {
		kb = createKnowledgeBase(runConfiguration.getOntologyInputs());
		profileMatcher = createProfileMatcher();
		for (PairwiseJob job : runConfiguration.getPairwiseJobs()) {
			job.setId();
			ProfileQuery q = createProfileQuery(job);
			Filter filter = IdFilter.create(job.getTargetIndividual());
			q.setFilter(filter);
			//job.getTargetIndividual();
			MatchSet mp = profileMatcher.findMatchProfile(q);
			job.setMatchSet(mp);
			//LOG.info(mp);

		}
		for (SearchJob job : runConfiguration.getSearchJobs()) {
			job.setId();
			ProfileQuery q = createProfileQuery(job);
			//job.getTargetIndividual();
			MatchSet mp = profileMatcher.findMatchProfile(q);
			job.setMatchSet(mp);
			//LOG.info(mp);

		}
		return;
	}

	/**
	 * @return map between names of profile matcher (e.g. phenodigm) and an implementing class
	 */
	public Map<String,ProfileMatcher> getProfileMatcherMap() {
		Map<String,ProfileMatcher> pmm = new HashMap<>();
		for (int i=0; i<matcherClasses.length; i++) {
			Class c = matcherClasses[i];
			System.out.println(c);
			Class[] args = {BMKnowledgeBase.class};
			try {
				Constructor constr = 
						c.getDeclaredConstructor(args);
				ProfileMatcher matcher = (ProfileMatcher) constr.newInstance(kb);
				System.out.println(matcher.getShortName() +"==>"+ matcher);
				pmm.put(matcher.getShortName(), matcher);
			}
			catch (Exception e) {
				System.err.println(e.getStackTrace());
			}

		}
		return pmm;
	}

	public ProfileMatcher createProfileMatcher() throws IOException, InstantiationException, IllegalAccessException {
		String requestedMatcher = runConfiguration.getTool();
		ProfileMatcher matcher = null;
		if (requestedMatcher == null) {
			requestedMatcher = "phenodigm";
		}
		Map<String, ProfileMatcher> pmm = getProfileMatcherMap();
		if (!pmm.containsKey(requestedMatcher)) {
			System.err.println("NO SUCH MATCHER:"+requestedMatcher);
		}
		return pmm.get(requestedMatcher);
	}

	public void toJsonFile(String fn) throws FileNotFoundException {
		JSONWriter jsonWriter = new JSONWriter(fn);
		jsonWriter.write(runConfiguration);
	}


	public String toJsonString() {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		return gson.toJson(runConfiguration);		
	}


	/**
	 * 
	 * Usage:
	 * <pre>
	 * owlsim3 cfg.json out.json
	 * </pre>
	 * 
	 * @param args
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws OWLOntologyCreationException
	 * @throws UnknownFilterException
	 * @throws IncoherentStateException
	 */
	public static void main(String[] args) throws IOException, InstantiationException, IllegalAccessException, OWLOntologyCreationException, UnknownFilterException, IncoherentStateException {
		String cfgFile = args[0];
		String outFile = args.length > 1 ? args[1] : "owlsim-output.json";
		RunConfiguration rc = RunnerUtil.generateRunConfigurationFromJsonFile(cfgFile);
		System.out.println(rc.getDescription());
		RunEngine re = new RunEngine(rc);
		re.execute();
		//LOG.info("Saving to: "+outFile);
		re.toJsonFile(outFile);

	}

}


