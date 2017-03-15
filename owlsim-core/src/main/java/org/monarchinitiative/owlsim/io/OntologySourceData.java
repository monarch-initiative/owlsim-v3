package org.monarchinitiative.owlsim.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.semanticweb.owlapi.model.OWLOntology;

import java.io.File;
import java.util.*;

/**
 * Simple container for storing the original data sources used for constructing the {@link OWLOntology} and the
 * {@link BMKnowledgeBase}.
 *
 * @author Jules Jacobsen <j.jacobsen@qmul.ac.uk>
 */
public class OntologySourceData {

    private final List<String> ontologies;
    private final List<String> dataOntologies;

    private final Map<String, String> curies;
    //TODO: shouldn't this be individualsTsvs?
    private final List<String> dataTsvs;
    private final Map<String, Collection<String>> individuals;
    //TODO - labels?

    private OntologySourceData(Builder builder) {
        this.ontologies = distinctImmutableListOf(builder.ontologies);
        this.dataOntologies = distinctImmutableListOf(builder.dataOntologies);
        this.curies = ImmutableMap.copyOf(builder.curies);
        this.dataTsvs = distinctImmutableListOf(builder.dataTsvs);
        this.individuals = ImmutableMap.copyOf(builder.individuals);
    }

    private ImmutableList<String> distinctImmutableListOf(List<String> list) {
        return list.stream().distinct().collect(ImmutableList.toImmutableList());
    }

    public List<String> getOntologies() {
        return ontologies;
    }

    public List<String> getDataOntologies() {
        return dataOntologies;
    }

    public Map<String, String> getCuries() {
        return curies;
    }

    public List<String> getDataTsvs() {
        return dataTsvs;
    }

    public Map<String, Collection<String>> getIndividuals() {
        return individuals;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OntologySourceData that = (OntologySourceData) o;
        return Objects.equals(ontologies, that.ontologies) &&
                Objects.equals(dataOntologies, that.dataOntologies) &&
                Objects.equals(curies, that.curies) &&
                Objects.equals(dataTsvs, that.dataTsvs) &&
                Objects.equals(individuals, that.individuals);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ontologies, dataOntologies, curies, dataTsvs, individuals);
    }

    @Override
    public String toString() {
        return "OntologySourceData{" +
                "ontologies=" + ontologies +
                ", dataOntologies=" + dataOntologies +
                ", curies=" + curies +
                ", dataTsvs=" + dataTsvs +
                ", individuals=" + individuals +
                '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> ontologies = new ArrayList<>();
        private List<String> dataOntologies = new ArrayList<>();
        //Curies need to be supplied if people are adding data using TSV files or pairwise mappings using curies.
        private Map<String, String> curies = Collections.emptyMap();
        private List<String> dataTsvs = new ArrayList<>();
        private Map<String, Collection<String>> individuals = new HashMap<>();

        private Builder(){
            //use the static method.
        }

        /**
         * @param curies
         * @return
         */
        public Builder curies(Map<String, String> curies) {
            this.curies = curies;
            return this;
        }

        /**
         * Loads an OWL/OBO ontology from a file.
         *
         * @param file
         */
        public Builder ontology(File file) {
            ontologies.add(file.getAbsolutePath());
            return this;
        }

        /**
         * Loads an OWL/OBO ontology from a path.
         *
         * @param path
         */
        public Builder ontology(String path) {
            this.ontologies.add(path);
            return this;
        }

        public Builder ontologies(String... paths) {
            this.ontologies.addAll(Arrays.asList(paths));
            return this;
        }

        /**
         * Loads, and merges the OWL/OBO ontologies from the paths given. These can be remote, local uncompressed or
         * gzipped.
         *
         * @param paths
         */
        public Builder ontologies(Collection<String> paths) {
            this.ontologies.addAll(paths);
            return this;
        }


        public Builder dataOntology(String path) {
            this.dataOntologies.add(path);
            return this;
        }

        public Builder dataOntologies(String... paths) {
            this.dataOntologies.addAll(Arrays.asList(paths));
            return this;
        }

        public Builder dataOntologies(Collection<String> paths) {
            this.dataOntologies.addAll(paths);
            return this;
        }

        public Builder dataTsv(String path) {
            dataTsvs.add(path);
            return this;
        }

        public Builder dataTsv(String... paths) {
            dataTsvs.addAll(Arrays.asList(paths));
            return this;
        }

        public Builder dataTsv(Collection<String> paths) {
            dataTsvs.addAll(paths);
            return this;
        }

        public Builder data(Map<String, ? extends Collection<String>> mappings) {
            individuals.putAll(mappings);
            return this;
        }

        public OntologySourceData build() {
            if(ontologies.isEmpty()) {
                throw new OntologyLoadException("No ontology defined.");
            }
            if (curies.isEmpty() && hasNonOntologyData()) {
                throw new OntologyLoadException("Cannot load TSV data sources or pairwise mappings when curies have not been defined.");
            }
            return new OntologySourceData(this);
        }

        private boolean hasNonOntologyData() {
            return !dataTsvs.isEmpty() || !individuals.isEmpty();
        }
    }
}
