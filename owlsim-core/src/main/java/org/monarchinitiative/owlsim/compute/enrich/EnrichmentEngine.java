package org.monarchinitiative.owlsim.compute.enrich;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

public interface EnrichmentEngine {
    
    /***
     * @return a short, URL friendly (no spaces, please) description of the engine
     */
    String getShortName();
    
    /**
     * @return kb
     */
    public BMKnowledgeBase getKnowledgeBase();
    
    /**
     * @return configuration
     */
    public EnrichmentConfig getEnrichmentConfig();
    
    /**
     * Some matchers require a costly precomputation step. Subsequent calls should
     * have no effect.
     * 
     * To ensure that all precomputations are performed ahead of time, call
     * this after initialization of the enrichment object
     */
    public void precompute();
    
    /**
     * Calculates whether classId is enriched in annotations of query
     * 
     * @param query
     * @param classId
     * @return enrichment result for class
     */
    public EnrichmentResult calculateEnrichmentAgainstClass(EnrichmentQuery query, String classId);
    
    /**
     * Calculates enrichment for all classes in kb
     * 
     * @param query
     * @return set of filtered enrichment results
     */
    public EnrichmentResultSet calculateEnrichmentAgainstKb(EnrichmentQuery query);

    /**
     * Calculates enrichment for a specified set of classes
     * 
     * @param query
     * @param targetClassIds
     * @return set of filtered enrichment results
     */
    EnrichmentResultSet calculateEnrichmentAgainstKb(EnrichmentQuery query,
            Set<String> targetClassIds);


}
