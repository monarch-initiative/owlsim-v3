package org.monarchinitiative.owlsim.compute.runner;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;

import org.apache.jena.atlas.logging.Log;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.MatcherMapModule;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.AbstractProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.impl.PhenodigmICProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.KnowledgeBaseModule;
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
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;

/**
 * Executes a set of jobs specified by {@link RunConfiguration}
 * 
 * @author cjm
 *
 */
public class RunEngine {

	//private Logger LOG = Logger.getLogger(RunEngine.class);

	RunConfiguration runConfiguration;
	MatcherModule mm = new MatcherModule();
	ProfileMatcher profileMatcher;
	BMKnowledgeBase kb;
	Class[] matcherClasses = {
			PhenodigmICProfileMatcher.class,
			BayesianNetworkProfileMatcher.class
	};

	/**
	 * @param runConfiguration
	 */
	@Inject
	public RunEngine(RunConfiguration runConfiguration, Map<String, ProfileMatcher> map) {
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
	 * 
	 * TODO: incomplete 
	 * @return
	 * @throws IOException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public ProfileMatcher createProfileMatcher() throws IOException, InstantiationException, IllegalAccessException {
		String requestedMatcher = runConfiguration.getTool();
		ProfileMatcher matcher = null;
		if (requestedMatcher == null) {
			requestedMatcher = "phenodigm";
		}

		for (int i=0; i<matcherClasses.length; i++) {
			Class c = matcherClasses[i];
			System.out.println(c);
			String sn = ((ProfileMatcher) c.newInstance()).getShortName();

			if (sn.equals(requestedMatcher)) {
				Class[] args = {BMKnowledgeBase.class};
				try {
					Constructor constr = 
							c.getDeclaredConstructor(args);
						matcher = (ProfileMatcher) constr.newInstance(kb);
				}
				catch (Exception e) {
					System.err.println(e.getStackTrace());
				}
			}
		}

		/*
		Map<String, ProfileMatcher> mmap = mm.getMatchers();
		for (String shortName : mmap.keySet()) {
			if (shortName.equals(requestedMatcher)) {
				matcher = mmap.get(shortName);
				((AbstractProfileMatcher) matcher).setKnowledgeBase(kb);
			}
		}
		 */
		//return PhenodigmICProfileMatcher.create(kb);
		//return matcher.getClass().create(kb);
		return matcher;
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
		Injector i = Guice.createInjector(
				new KnowledgeBaseModule(rc.getOntologyInputs(), null),
				new MatcherMapModule(),
				new RunEngineConfModule(rc));
		
		RunEngine re = i.getInstance(RunEngine.class);
		re.execute();
		//LOG.info("Saving to: "+outFile);
		re.toJsonFile(outFile);

	}

	static class RunEngineConfModule extends AbstractModule {

		private final RunConfiguration rc;

		public RunEngineConfModule(RunConfiguration rc) throws IOException {
			this.rc = rc;
		}
		
		@Provides
		@Singleton
		public RunConfiguration providesConf() {
			return rc;
		}

		@Override
		protected void configure() {
			// empty
		}
	}
}


