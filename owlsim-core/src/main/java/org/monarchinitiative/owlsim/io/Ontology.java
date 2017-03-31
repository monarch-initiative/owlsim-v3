package org.monarchinitiative.owlsim.io;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.validator.routines.UrlValidator;
import org.apache.log4j.Logger;
import org.prefixcommons.CurieUtil;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.parameters.ChangeApplied;

import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

/**
 * OWL API wrapper to facilitate building OWLOntology objects to load into the
 * {@link org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl}
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class Ontology {

	private static final Logger logger = Logger.getLogger(Ontology.class);

	// OWLOntology is a mutable object
	private final OntologySourceData sourceData;
	private final CurieUtil curieUtil;
	private final OWLOntology owlOntology;

	private final OWLOntologyManager ontologyManager;
	private final OWLDataFactory owlDataFactory;

	private Ontology(OntologySourceData sourceData, Concurrency concurrency) {
		Objects.requireNonNull(sourceData, "Unable to create Ontology without data sources.");
		this.sourceData = sourceData;
		this.curieUtil = new CurieUtil(sourceData.getCuries());
		this.ontologyManager = createOntologyManager(concurrency);
		this.owlOntology = createEmptyOntology(ontologyManager);
		this.owlDataFactory = ontologyManager.getOWLDataFactory();
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
	 * Loads an ontology using an OWLOntologyManager using the concurrency type
	 * specified.
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

	/**
	 * @param curie
	 * @return
	 */
	public OWLClass getOWLClass(String curie) {
		return owlDataFactory.getOWLClass(toIri(curie));
	}

	public OWLNamedIndividual getOWLNamedIndividual(String curie) {
		return owlDataFactory.getOWLNamedIndividual(toIri(curie));
	}

	public String toCurie(IRI iri) {
		String iriString = iri.toString();
		return curieUtil.getCurie(iriString).orElse(iriString);
	}

	private void loadOwlOntology() {
		// Order matters here - don't change it.
		mergeOntologies(sourceData.getOntologies());
		mergeOntologies(sourceData.getDataOntologies());
		loadIndividualAssociationsFromTsv(sourceData.getIndividualAssociationTsvs());
		loadIndividualAssociationsFromMap(sourceData.getIndividualAssociations());
		loadLabelFromTsv(sourceData.getLabelTsvs());
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

	private OWLOntology loadIndividualAssociationsFromTsv(Collection<String> paths) {
		paths.forEach(this::loadIndividualAssociationsFromTsv);
		return owlOntology;
	}

	private OWLOntology loadIndividualAssociationsFromTsv(String path) {
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
				BufferedReader bf = new BufferedReader(new InputStreamReader(gis, Charset.forName("UTF-8")))) {
			bf.lines().forEach(line -> loadLineIntoDataOntology(line));
		} catch (IOException e) {
			throw new OntologyLoadException(e);
		}
		return owlOntology;
	}

	private void loadIndividualAssociationsFromMap(Map<String, Collection<String>> individuals) {
		if (!individuals.isEmpty()) {
			logger.info("Loading individuals from map");
		}
		// e.g. 'ORPHA:710':
		// ['HP:0000194','HP:0000218','HP:0000262','HP:0000303','HP:0000316']
		individuals.forEach(addIndividual());
	}

	private BiConsumer<String, Collection<String>> addIndividual() {
		return (individual, annotations) -> {
			for (String curie : annotations) {
				addInstanceOf(individual, curie);
			}
		};
	}

	private void loadLineIntoDataOntology(String line) {
		String[] vals = line.split("\t", 2);
		String[] terms = vals[1].split(";");
		for (String t : terms) {
			addInstanceOf(vals[0], t);
		}
	}

	private void addInstanceOf(String individual, String ontologyClass) {
		Objects.requireNonNull(individual, "Individual identifier cannot be null. Check your input.");
		Objects.requireNonNull(ontologyClass, "Class identifier(s) cannot be null. Check your input.");
		if (!ontologyClass.isEmpty()) {
			// logger.info("Adding axiom " + individual + ": " + ontologyClass);
			OWLClass owlClass = getOWLClass(ontologyClass);
			OWLNamedIndividual owlNamedIndividual = getOWLNamedIndividual(individual);
			OWLClassAssertionAxiom axiom = owlDataFactory.getOWLClassAssertionAxiom(owlClass, owlNamedIndividual);
			addAxiom(axiom);
		}
	}

	private ChangeApplied addAxiom(OWLAxiom axiom) {
		return ontologyManager.addAxiom(owlOntology, axiom);
	}

	IRI toIri(String id) {
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

	private void loadLabelFromTsv(Set<String> labelTsvs) {
		for (String labelsTsv : labelTsvs) {
			Path file = Paths.get(labelsTsv);
			logger.info("Reading tsv data from " + labelsTsv);
			try {
				Files.lines(file).forEach(line -> {
					System.out.println(line);
					String[] vals = line.split("\t", 2);
					addLabel(vals[0], vals[1]);
				});
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// we may want to load labels directly from memory
	private void loadLabelFromMap(Map<String, String> labelMap) {
		for (Entry<String, String> entry : labelMap.entrySet()) {
			addLabel(entry.getKey(), entry.getValue());
		}
	}

	private void addLabel(String s, String l) {
		OWLAnnotationAssertionAxiom axiom = owlDataFactory.getOWLAnnotationAssertionAxiom(owlDataFactory.getRDFSLabel(),
				IRI.create(curieUtil.getIri(s).orElse(s)), owlDataFactory.getOWLLiteral(l));
		ontologyManager.addAxiom(owlOntology, axiom);

	}
}
