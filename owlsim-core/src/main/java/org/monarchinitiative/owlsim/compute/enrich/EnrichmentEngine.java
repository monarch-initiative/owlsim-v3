package org.monarchinitiative.owlsim.compute.enrich;

public interface EnrichmentEngine {
    
    /***
     * @return a short, URL friendly (no spaces, please) description of the engine
     */
    String getShortName();
    
    /**
     * Some matchers require a costly precomputation step. Subsequent calls should
     * have no effect.
     * 
     * To ensure that all precomputations are performed ahead of time, call
     * this after initialization of the enrichment object
     */
    public void precompute();
    
    public EnrichmentResult calculateEnrichmentAgainstClass(EnrichmentQuery query, String classId);
    public EnrichmentResultSet calculateEnrichmentAgainstKb(EnrichmentQuery query);


}
