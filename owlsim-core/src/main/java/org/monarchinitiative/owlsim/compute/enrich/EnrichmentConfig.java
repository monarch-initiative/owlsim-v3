package org.monarchinitiative.owlsim.compute.enrich;

import javax.inject.Inject;

public class EnrichmentConfig {
    
    public enum AnalysisType {OVER, UNDER, BOTH};
    
    public Double overThreshold = null;
    public Double underThreshold = null;
    
    public AnalysisType analysisType = AnalysisType.BOTH;

}
