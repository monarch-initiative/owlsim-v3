package org.monarchinitiative.owlsim.compute.enrich;

import java.util.List;

public interface EnrichmentResultSet {
    public void sortResults();
    public List<EnrichmentResult> getResults();
    public EnrichmentQuery getQuery();
    public void rankResults();
    
}
