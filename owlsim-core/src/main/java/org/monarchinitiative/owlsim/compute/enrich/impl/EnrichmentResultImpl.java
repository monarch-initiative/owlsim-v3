package org.monarchinitiative.owlsim.compute.enrich.impl;

import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;

public class EnrichmentResultImpl implements EnrichmentResult {
    
    private final double p;
    private final double pCorrected;
    private final String enrichedClassId;
    private final int numShared;
    private final int enrichedCardinality;
    private final int sampleCardinality;
    Integer rank = null;

    public static EnrichmentResult create(double p, double pCorrected,
            String enrichedClassId, int numShared, int enrichedCardinality, int sampleCardinality) {
        return new EnrichmentResultImpl(p, pCorrected, enrichedClassId, numShared,
                enrichedCardinality,
                sampleCardinality);
    }
  

   
    public EnrichmentResultImpl(double p, double pCorrected,
            String enrichedClassId, int numShared, int enrichedCardinality,
            int sampleCardinality) {
        super();
        this.p = p;
        this.pCorrected = pCorrected;
        this.enrichedClassId = enrichedClassId;
        this.numShared = numShared;
        this.enrichedCardinality = enrichedCardinality;
        this.sampleCardinality = sampleCardinality;
    }



    /**
     * @return the p
     */
    public double getP() {
        return p;
    }

    /**
     * @return the pCorrected
     */
    public double getpCorrected() {
        return pCorrected;
    }
    
    
    

    /**
     * @return the rank
     */
    public Integer getRank() {
        return rank;
    }


    /**
     * @param rank the rank to set
     */
    public void setRank(int rank) {
        this.rank = rank;
    }


    /**
     * @return the enrichedClassId
     */
    public String getEnrichedClassId() {
        return enrichedClassId;
    }


    @Override
    public double getScore() {
        return -Math.log(pCorrected);
    }



    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EnrichmentResultImpl [p=" + p + ", pCorrected=" + pCorrected
                + ", enrichedClassId=" + enrichedClassId + ", numShared="
                + numShared + ", enrichedCardinality=" + enrichedCardinality
                + ", sampleCardinality=" + sampleCardinality + "]";
    }


 
    

}
