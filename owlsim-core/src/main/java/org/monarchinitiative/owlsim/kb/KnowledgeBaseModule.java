package org.monarchinitiative.owlsim.kb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.zip.GZIPInputStream;

import javax.inject.Singleton;

import org.apache.commons.validator.routines.UrlValidator;
import org.monarchinitiative.owlsim.kb.bindings.IndicatesDataTsvs;
import org.monarchinitiative.owlsim.kb.bindings.IndicatesOwlDataOntologies;
import org.monarchinitiative.owlsim.kb.bindings.IndicatesOwlOntologies;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.HasAxioms;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;
import uk.ac.manchester.cs.owl.owlapi.concurrent.NoOpReadWriteLock;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;

/**
 * TODO - rewrite this
 * 
 * Reduce duplication of code with OWLLoader
 *
 */
public class KnowledgeBaseModule extends AbstractModule {

	private final ImmutableCollection<String> ontologyUris;
    private final ImmutableCollection<String> ontologyDataUris;
    private final ImmutableCollection<String> dataTsvs;
	private final UrlValidator urlValdiator = UrlValidator.getInstance();

	public KnowledgeBaseModule(Collection<String> ontologyUris, 
	        Collection<String> ontologyDataUris, 
	        Set<String> dataTsvs) {
		this.ontologyUris = new ImmutableSet.Builder<String>().addAll(ontologyUris).build();
        this.ontologyDataUris = new ImmutableSet.Builder<String>().addAll(ontologyDataUris).build();
        this.dataTsvs = new ImmutableSet.Builder<String>().addAll(dataTsvs).build();
	}

	@Override
	protected void configure() {
	    
		bind(BMKnowledgeBase.class).to(BMKnowledgeBaseOWLAPIImpl.class).in(Singleton.class);
		bind(OWLReasonerFactory.class).to(ElkReasonerFactory.class);
//		bind(OWLOntologyManager.class).to(OWLOntologyManagerImpl.class);
//		bind(ReadWriteLock.class).to(NoOpReadWriteLock.class);
//        bind(OWLDataFactory.class).to(OWLDataFactoryImpl.class);
		//bind(OWLOntologyManager.class).toInstance(OWLManager.createOWLOntologyManager());
	}

	OWLOntology loadOntology(OWLOntologyManager manager, String uri) throws OWLOntologyCreationException {
		if (urlValdiator.isValid(uri)) {
			return manager.loadOntology(IRI.create(uri));
		} else {
			File file = new File(uri);
			return manager.loadOntologyFromOntologyDocument(file);
		}
	}

	OWLOntology mergeOntologies(OWLOntologyManager manager, Collection<String> uris) throws OWLOntologyCreationException, FileNotFoundException, IOException {
		OWLOntology ontology = manager.createOntology();
		for (String uri: uris) {
			OWLOntology loadedOntology;
			if (uri.endsWith(".gz")) {
		        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(uri));
		        BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		        loadedOntology = manager.loadOntologyFromOntologyDocument(gis);
			}
			else {
			    loadedOntology = loadOntology(manager, uri);
			}
            manager.addAxioms(ontology, loadedOntology.getAxioms());
		}
		return ontology;
	}

	@Provides
	@IndicatesOwlOntologies
	@Singleton
	OWLOntology getOwlOntologies(OWLOntologyManager manager) throws OWLOntologyCreationException, FileNotFoundException, IOException {
		return mergeOntologies(manager, ontologyUris);
	}

    @Provides
    @IndicatesOwlDataOntologies
    @Singleton
    OWLOntology getOwlDataOntologies(OWLOntologyManager manager) throws OWLOntologyCreationException, FileNotFoundException, IOException {
        return mergeOntologies(manager, ontologyDataUris);
    }

    @Provides
    @IndicatesDataTsvs
    @Singleton
    OWLOntology getDataTsvs(OWLOntologyManager manager) throws OWLOntologyCreationException, FileNotFoundException, IOException {
        return mergeOntologies(manager, dataTsvs);
    }

}
