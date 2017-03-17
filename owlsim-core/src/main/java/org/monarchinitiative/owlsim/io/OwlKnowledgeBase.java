package org.monarchinitiative.owlsim.io;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.impl.BMKnowledgeBaseOWLAPIImpl;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

import java.io.File;
import java.util.Collection;
import java.util.Map;

/**
 * A convenience wrapper to enable easy loading of a {@link BMKnowledgeBase} from OWL ontologies and data files.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public final class OwlKnowledgeBase {

    private static final Logger logger = Logger.getLogger(OwlKnowledgeBase.class);

    private OwlKnowledgeBase() {
        //class is uninstantiable
    }

    public static Loader loader() {
        return new Loader();
    }

    public static class Loader {

        private Concurrency concurrencyType = Concurrency.CONCURRENT;
        private OWLReasonerFactory owlReasonerFactory = new ElkReasonerFactory();

        //TODO: do we want to expose this or keep it here? Chris mentioned we might want a way of keeping track of what the original source data was.
        //So this is where it is. It's so tightly coupled this is literally a conjoined twin at the moment.
        private OntologySourceData.Builder sourceDataBuilder = OntologySourceData.builder();

        private Loader() {
            //uses the static load() method in the parent class
        }

        /**
         * @param curies
         * @return
         */
        public Loader loadCuries(Map<String, String> curies) {
            sourceDataBuilder.curies(curies);
            return this;
        }

        /**
         * Loads an OWL/OBO ontology from a file.
         *
         * @param file
         */
        public Loader loadOntology(File file) {
            sourceDataBuilder.ontology(file);
            return this;
        }

        /**
         * Loads an OWL/OBO ontology from a path.
         *
         * @param path
         */
        public Loader loadOntology(String path) {
            sourceDataBuilder.ontology(path);
            return this;
        }

        public Loader loadOntologies(String... paths) {
            sourceDataBuilder.ontologies(paths);
            return this;
        }

        /**
         * Loads, and merges the OWL/OBO ontologies from the paths given. These can be remote, local uncompressed or
         * gzipped.
         *
         * @param paths
         */
        public Loader loadOntologies(Collection<String> paths) {
            sourceDataBuilder.ontologies(paths);
            return this;
        }

        public Loader loadDataFromOntology(String path) {
            sourceDataBuilder.dataOntology(path);
            return this;
        }

        public Loader loadDataFromOntologies(String... paths) {
            sourceDataBuilder.dataOntologies(paths);
            return this;
        }

        public Loader loadDataFromOntologies(Collection<String> paths) {
            sourceDataBuilder.dataOntologies(paths);
            return this;
        }

        public Loader loadIndividualAssociationsFromTsv(String path) {
            sourceDataBuilder.individualAssociationsTsv(path);
            return this;
        }

        public Loader loadIndividualAssociationsFromTsv(String... paths) {
            sourceDataBuilder.individualAssociationsTsv(paths);
            return this;
        }

        public Loader loadIndividualAssociationsFromTsv(Collection<String> paths) {
            sourceDataBuilder.individualAssociationsTsv(paths);
            return this;
        }

        public Loader loadIndividualAssociations(Map<String, ? extends Collection<String>> data) {
            sourceDataBuilder.individualAssociations(data);
            return this;
        }

        /**
         * Creates an {@link OWLOntologyManager} that is configured with the standard parsers and storers and provides
         * locking for concurrent access (default).
         */
        public Loader useConcurrentOntologyManager() {
            concurrencyType = Concurrency.CONCURRENT;
            return this;
        }

        /**
         * Creates an {@link OWLOntologyManager} that is configured with standard parsers,
         * storers etc.
         */
        public Loader useStandardOntologyManager() {
            concurrencyType = Concurrency.NON_CONCURRENT;
            return this;
        }

        /**
         * Allows overriding of the default {@link ElkReasonerFactory}
         *
         * @param owlReasonerFactory a concrete implementation of the {@link OWLReasonerFactory}
         */
        public Loader useReasonerFactory(OWLReasonerFactory owlReasonerFactory) {
            this.owlReasonerFactory = owlReasonerFactory;
            return this;
        }

        /**
         * @return handle for a Bitmap-based Knowledge Base
         */
        public BMKnowledgeBase createKnowledgeBase() {

            OntologySourceData sourceData = sourceDataBuilder.build();
            Ontology ontology = Ontology.load(sourceData, concurrencyType);

            return BMKnowledgeBaseOWLAPIImpl.create(ontology, owlReasonerFactory);
        }

    }


}
