package org.monarchinitiative.owlsim.io;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

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
    private final List<String> individualAssociationTsvs;
    private final Map<String, Collection<String>> individualAssociations;
    private final Set<String> labelTsvs;

    private OntologySourceData(Builder builder) {
        this.ontologies = distinctImmutableListOf(builder.ontologies);
        this.dataOntologies = distinctImmutableListOf(builder.dataOntologies);
        this.curies = ImmutableMap.copyOf(builder.curies);
        this.individualAssociationTsvs = distinctImmutableListOf(builder.individualAssociationTsvs);
        this.individualAssociations = ImmutableMap.copyOf(builder.individualAssociations);
        this.labelTsvs = ImmutableSet.copyOf(builder.labelTsvs);
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

    public Set<String> getLabelTsvs() {
        return labelTsvs;
    }

    public List<String> getIndividualAssociationTsvs() {
        return individualAssociationTsvs;
    }

    public Map<String, Collection<String>> getIndividualAssociations() {
        return individualAssociations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OntologySourceData that = (OntologySourceData) o;
        return Objects.equals(ontologies, that.ontologies) &&
                Objects.equals(dataOntologies, that.dataOntologies) &&
                Objects.equals(curies, that.curies) &&
                Objects.equals(individualAssociationTsvs, that.individualAssociationTsvs) &&
                Objects.equals(individualAssociations, that.individualAssociations) &&
                Objects.equals(labelTsvs, that.labelTsvs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ontologies, dataOntologies, curies, individualAssociationTsvs, individualAssociations);
    }

    @Override
    public String toString() {
        return "OntologySourceData{" +
                "ontologies=" + ontologies +
                ", dataOntologies=" + dataOntologies +
                ", curies=" + curies +
                ", individualAssociationTsvs=" + individualAssociationTsvs +
                ", individualAssociations=" + individualAssociations +
                ", labelTsvs=" + labelTsvs +
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
        private List<String> individualAssociationTsvs = new ArrayList<>();
        private Map<String, Collection<String>> individualAssociations = new HashMap<>();
        private Set<String> labelTsvs = Collections.emptySet();

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

        public Builder individualAssociationsTsv(String path) {
            individualAssociationTsvs.add(path);
            return this;
        }

        public Builder individualAssociationsTsv(String... paths) {
            individualAssociationTsvs.addAll(Arrays.asList(paths));
            return this;
        }

        public Builder individualAssociationsTsv(Collection<String> paths) {
            individualAssociationTsvs.addAll(paths);
            return this;
        }

        public Builder individualAssociations(Map<String, ? extends Collection<String>> mappings) {
            individualAssociations.putAll(mappings);
            return this;
        }
        
        public Builder labelTsvs(Set<String> labelTsvs) {
            this.labelTsvs = labelTsvs;
            return this;
        }

        public OntologySourceData build() {
            if(ontologies.isEmpty()) {
                throw new OntologyLoadException("No ontology defined.");
            }
            if (curies.isEmpty() && hasIndividualAssociationData()) {
                throw new OntologyLoadException("Cannot load individual class associations when curies have not been defined.");
            }
            return new OntologySourceData(this);
        }

        private boolean hasIndividualAssociationData() {
            return !individualAssociationTsvs.isEmpty() || !individualAssociations.isEmpty();
        }
    }
}
