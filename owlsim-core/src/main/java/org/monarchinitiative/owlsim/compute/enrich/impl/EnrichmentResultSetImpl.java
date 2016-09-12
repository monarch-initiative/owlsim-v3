package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;

public class EnrichmentResultSetImpl implements EnrichmentResultSet {

    boolean isSorted = false;
    private List<EnrichmentResult> results;
    
    public EnrichmentResultSet create(List<EnrichmentResult> results) {
        return new EnrichmentResultSetImpl(results);
    }
    
    public EnrichmentResultSetImpl(List<EnrichmentResult> results) {
        this.results = results;
    }
    /**
     * @return the results
     */
    public List<EnrichmentResult> getResults() {
        return results;
    }
    
    /**
     * Sorts the results by probability, highest probability first
     */
    public void sortResults() {
        Collections.sort(results, ResultComparator);
        isSorted = true;
        rankResults();
    }
    
    private static Comparator<EnrichmentResult> ResultComparator = 
            new Comparator<EnrichmentResult>() {
        public int compare(EnrichmentResult c1, EnrichmentResult c2) {
            if (c1.getScore() < c2.getScore()) return 1;
            if (c1.getScore() > c2.getScore()) return -1;
            return 0;
        }  
    };

    // TODO: abstract over this and analogous rankMatches method
    public void rankResults() {
        if (!isSorted)
            sortResults();
        int rank = 0;
        Double lastScore = null;
        for (EnrichmentResult m : results) {
//            if (query.getReferenceIndividualIds() != null &&
//                    query.getReferenceIndividualIds().contains(m.getResultId())) {
//                referenceResults.put(m.getResultId(), m);
//            }
            double s = m.getScore();
            // TODO - avoid double equality test
            if (lastScore == null ||
                    s != lastScore) {
                rank++;
            }
            m.setRank(rank);
            lastScore = s;
                    
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EnrichmentResultSetImpl [isSorted=" + isSorted + ", results="
                + results + "]";
    }
    
    
}
