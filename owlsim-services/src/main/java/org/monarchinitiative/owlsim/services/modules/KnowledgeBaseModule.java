package org.monarchinitiative.owlsim.services.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.classmatch.ClassMatcher;
import org.monarchinitiative.owlsim.compute.enrich.impl.HypergeometricEnrichmentEngine;
import org.monarchinitiative.owlsim.compute.matcher.impl.BayesianNetworkProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator;
import org.monarchinitiative.owlsim.compute.mica.impl.MostInformativeCommonAncestorCalculatorImpl;
import org.monarchinitiative.owlsim.io.OwlKnowledgeBase;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class KnowledgeBaseModule extends AbstractModule {

	Logger logger = Logger.getLogger(KnowledgeBaseModule.class);

	private final BMKnowledgeBase bmKnowledgeBase;

	public KnowledgeBaseModule(Collection<String> ontologyUris, Collection<String> ontologyDataUris, Set<String> dataTsvs,  Set<String> labelTsvs, Map<String, String> curies) {

		logger.info("Loading ontologyUris:");
		ontologyUris.forEach(logger::info);
		logger.info("Loading ontologyDataUris:");
		ontologyDataUris.forEach(logger::info);
		logger.info("Loading individual associations Tsvs:");
		dataTsvs.forEach(logger::info);
		logger.info("Loading curies:");
		curies.entrySet().forEach(logger::info);
		logger.info("Loading labels:");
		//labels.entrySet().forEach(logger::info);

		//The OwlKnowledgeBase.Loader uses the ELKReasonerFactory and Concurrency.CONCURRENT as defaults.
		this.bmKnowledgeBase = OwlKnowledgeBase.loader()
				.loadOntologies(ontologyUris)
				.loadDataFromOntologies(ontologyDataUris)
				.loadIndividualAssociationsFromTsv(dataTsvs)
				.loadCuries(curies)
				.loadLabelsFromTsv(labelTsvs)
				.createKnowledgeBase();

		logger.info("Created BMKnowledgebase");
	}

	@Override
	protected void configure() {
	}

	@Provides
	@Singleton
	BMKnowledgeBase provideBMKnowledgeBaseOWLAPIImpl() {
		return bmKnowledgeBase;
	}

	@Provides
	MostInformativeCommonAncestorCalculator getMostInformativeCommonAncestorCalculator(BMKnowledgeBase knowledgeBase) {
        return new MostInformativeCommonAncestorCalculatorImpl(knowledgeBase);
	}
	
	@Provides
	HypergeometricEnrichmentEngine getHypergeometricEnrichmentEngine(BMKnowledgeBase knowledgeBase) {
	    return new HypergeometricEnrichmentEngine(knowledgeBase);
	}
	
	@Provides
	BayesianNetworkProfileMatcher getBayesianNetworkProfileMatcher(BMKnowledgeBase knowledgeBase) {
	    return BayesianNetworkProfileMatcher.create(knowledgeBase);
	}
	
	@Provides
	ClassMatcher getClassMatcher(BMKnowledgeBase knowledgeBase) {
	    return new ClassMatcher(knowledgeBase);
	}

}
