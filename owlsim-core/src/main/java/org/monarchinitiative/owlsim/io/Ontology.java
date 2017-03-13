package org.monarchinitiative.owlsim.io;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.prefixcommons.CurieUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.zip.GZIPInputStream;

/**
 * OWL API wrapper to facilitate building OWLOntology objects to load into the {@link org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Ontology {

    private static final Logger logger = Logger.getLogger(Ontology.class);

    //OWLOntology is a mutable object
    private final OntologySourceData sourceData;
    private final CurieUtil curieUtil;
    private final OWLOntology owlOntology;

    private final OWLOntologyManager ontologyManager;

    private Ontology(OntologySourceData sourceData, Concurrency concurrency) {
        Objects.requireNonNull(sourceData, "Unable to create Ontology without data sources.");
        this.sourceData = sourceData;
        this.curieUtil = new CurieUtil(sourceData.getCuries());
        this.ontologyManager = createOntologyManager(concurrency);
        this.owlOntology = createEmptyOntology(ontologyManager);
        loadOwlOntology();
    }

    /**
     * Loads an ontology using a concurrent OWLOntologyManager.
     *
     * @param sourceData
     * @return An Ontology created from the source data provided.
     */
    public static Ontology load(OntologySourceData sourceData) {
        return new Ontology(sourceData, Concurrency.CONCURRENT);
    }

    /**
     * Loads an ontology using an OWLOntologyManager using the concurrency type specified.
     *
     * @param sourceData
     * @param concurrency
     * @return An Ontology created from the source data provided.
     */
    public static Ontology load(OntologySourceData sourceData, Concurrency concurrency) {
        return new Ontology(sourceData, useConcurrentIfNull(concurrency));
    }

    private static Concurrency useConcurrentIfNull(Concurrency concurrency) {
        return concurrency == null ? Concurrency.CONCURRENT : concurrency;
    }

    public OWLOntology getOwlOntology() {
        return owlOntology;
    }

    public OntologySourceData getSourceData() {
        return sourceData;
    }

    public CurieUtil getCurieUtil() {
        return curieUtil;
    }

    private void loadOwlOntology() {
        //Order matters here - don't change it.
        mergeOntologies(sourceData.getOntologies());
        mergeOntologies(sourceData.getDataOntologies());
        loadDataFromTsv(sourceData.getDataTsvs());
        loadDataFromPairwiseMappings(sourceData.getPairwiseMappings());
        logger.info("Ontology loaded");
    }

    private OWLOntologyManager createOntologyManager(Concurrency concurrencyType) {
        if (concurrencyType == Concurrency.NON_CONCURRENT) {
            logger.info("Using non-concurrent OWL ontology manager");
            return OWLManager.createOWLOntologyManager();
        }
        logger.info("Using concurrent OWL ontology manager");
        return OWLManager.createConcurrentOWLOntologyManager();
    }

    private OWLOntology createEmptyOntology(OWLOntologyManager ontologyManager) {
        try {
            return ontologyManager.createOntology();
        } catch (OWLOntologyCreationException e) {
            throw new OntologyLoadException(e);
        }
    }

    private OWLOntology mergeOntology(String uri) {
        OWLOntology loadedOntology = loadOwlOntology(uri);
        addAxioms(loadedOntology.getAxioms());
        return owlOntology;
    }

    private OWLOntology mergeOntologies(Collection<String> uris) {
        uris.forEach(uri -> mergeOntology(uri));
        return owlOntology;
    }

    private ChangeApplied addAxiom(OWLAxiom axiom) {
        return ontologyManager.addAxiom(owlOntology, axiom);
    }

    private ChangeApplied addAxioms(Set<OWLAxiom> axioms) {
        return ontologyManager.addAxioms(owlOntology, axioms);
    }

    private OWLOntology loadOwlOntology(String uri) {
        UrlValidator urlValidator = UrlValidator.getInstance();
        if (urlValidator.isValid(uri)) {
            return loadRemoteOntology(IRI.create(uri));
        } else if (uri.endsWith(".gz")) {
            return loadGzippedOntology(Paths.get(uri));
        } else {
            return loadOwlOntologyFromDocument(Paths.get(uri));
        }
    }

    private OWLOntology loadRemoteOntology(IRI iri) {
        return loadOwlOntology(iri);
    }

    private OWLOntology loadGzippedOntology(Path path) {
        logger.info("Loading gzipped ontology from " + path);
        try (InputStream is = new GZIPInputStream(new FileInputStream(path.toFile()))) {
            return loadOwlOntologyFromDocument(is);
        } catch (IOException e) {
            throw new OntologyLoadException(e);
        }
    }

    private OWLOntology loadOwlOntology(IRI iri) {
        try {
            logger.info("Loading ontology from IRI" + iri.getShortForm());
            return ontologyManager.loadOntology(iri);
        } catch (OWLOntologyCreationException e) {
            throw new OntologyLoadException(e);
        }
    }

    private OWLOntology loadDataFromTsv(Collection<String> paths) {
        paths.forEach(this::loadDataFromTsv);
        return owlOntology;
    }

    private OWLOntology loadDataFromTsv(String path) {
        if (path.endsWith(".gz")) {
            return loadDataFromTsvGzip(path);
        }
        Path file = Paths.get(path);
        logger.info("Reading tsv data from " + path);
        try {
            Files.lines(file).forEach(line -> loadLineIntoDataOntology(line));
        } catch (IOException e) {
            throw new OntologyLoadException(e);
        }
        return owlOntology;
    }

    private OWLOntology loadDataFromTsvGzip(String path) {
        Path file = Paths.get(path);
        logger.info("Reading gzipped tsv data from " + file);
        try (GZIPInputStream gis = new GZIPInputStream(new FileInputStream(file.toFile()));
             BufferedReader bf = new BufferedReader(new InputStreamReader(gis, Charset.forName("UTF-8")))
        ) {
            bf.lines().forEach(line -> loadLineIntoDataOntology(line));
        } catch (IOException e) {
            throw new OntologyLoadException(e);
        }
        return owlOntology;
    }

    private void loadDataFromPairwiseMappings(Map<String, String> pairwiseMappings) {
        pairwiseMappings.forEach(this::addInstanceOf);
    }

    private void loadLineIntoDataOntology(String line) {
        String[] vals = line.split("\t", 2);
        String[] terms = vals[1].split(";");
        for (String t : terms) {
            addInstanceOf(vals[0], t);
        }
    }

    private void addInstanceOf(String individual, String ontologyClass) {
//                logger.info("Adding axiom " + individual + ": " + ontologyClass);
        OWLDataFactory owlDataFactory = ontologyManager.getOWLDataFactory();
        OWLClass owlClass = owlDataFactory.getOWLClass(toIri(ontologyClass));
        OWLNamedIndividual owlNamedIndividual = owlDataFactory.getOWLNamedIndividual(toIri(individual));
        OWLClassAssertionAxiom axiom = owlDataFactory.getOWLClassAssertionAxiom(owlClass, owlNamedIndividual);
        addAxiom(axiom);
    }

    private IRI toIri(String id) {
        return IRI.create(curieUtil.getIri(id).orElse(id));
    }

    private OWLOntology loadOwlOntologyFromDocument(Path path) {
        try {
            logger.info("Loading ontology from document " + path);
            return ontologyManager.loadOntologyFromOntologyDocument(path.toFile());
        } catch (OWLOntologyCreationException e) {
            throw new OntologyLoadException(e);
        }
    }

    private OWLOntology loadOwlOntologyFromDocument(InputStream is) {
        try {
            return ontologyManager.loadOntologyFromOntologyDocument(is);
        } catch (OWLOntologyCreationException e) {
            logger.error("Unable to create ontology" + e);
            throw new OntologyLoadException(e);
        }
    }
}

