package org.monarchinitiative.owlsim.kb;

import java.io.File;
import java.util.Collection;

import javax.inject.Singleton;

import org.monarchinitiative.owlsim.kb.bindings.IndicatesOwlDataOntologies;
import org.monarchinitiative.owlsim.kb.bindings.IndicatesOwlOntologies;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class KnowledgeBaseModule extends AbstractModule {

	private final ImmutableCollection<String> ontologyUris;
	private final ImmutableCollection<String> ontologyDataUris;

	public KnowledgeBaseModule(Collection<String> ontologyUris, Collection<String> ontologyDataUris) {
		this.ontologyUris = new ImmutableSet.Builder<String>().addAll(ontologyUris).build();
		this.ontologyDataUris = new ImmutableSet.Builder<String>().addAll(ontologyDataUris).build();
	}

	@Override
	protected void configure() {
		bind(BMKnowledgeBase.class).to(BMKnowledgeBaseOWLAPIImpl.class).in(Singleton.class);
		bind(OWLReasonerFactory.class).to(ElkReasonerFactory.class);
		bind(OWLOntologyManager.class).toInstance(OWLManager.createOWLOntologyManager());
	}

	OWLOntology loadOntology(OWLOntologyManager manager, String uri) throws OWLOntologyCreationException {
		if (uri.startsWith("http")) {
			return manager.loadOntology(IRI.create(uri));
		}
		else {
			File file = new File(uri);
			return manager.loadOntologyFromOntologyDocument(file);
		}
	}

	OWLOntology mergeOntologies(OWLOntologyManager manager, Collection<String> uris) throws OWLOntologyCreationException {
		OWLOntology ontology = manager.createOntology();
		for (String uri: uris) {
			manager.addAxioms(ontology, loadOntology(manager, uri).getAxioms());
		}
		return ontology;
	}

	@Provides
	@IndicatesOwlOntologies
	@Singleton
	OWLOntology getOwlOntologies(OWLOntologyManager manager) throws OWLOntologyCreationException {
		return mergeOntologies(manager, ontologyUris);
	}

	@Provides
	@IndicatesOwlDataOntologies
	@Singleton
	OWLOntology getOwlDataOntologies(OWLOntologyManager manager) throws OWLOntologyCreationException {
		return mergeOntologies(manager, ontologyDataUris);
	}

}
