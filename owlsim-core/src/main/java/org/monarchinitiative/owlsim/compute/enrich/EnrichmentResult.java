package org.monarchinitiative.owlsim.compute.enrich;

public interface EnrichmentResult {
    
    public double getScore();

    public void setRank(int rank);
    
    public String getEnrichedClassId();
    
    public double getpCorrected();
    public double getpUnderRepresentedCorrected();

}
