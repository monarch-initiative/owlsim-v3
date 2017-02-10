package org.monarchinitiative.owlsim.compute.enrich.impl;

import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResult;

public class EnrichmentResultImpl implements EnrichmentResult {
    
    private final double p;
    private final double pCorrected;
    private final double pUnderRepresentedCorrected;
    private final String enrichedClassId;
    private final int numShared;
    private final int enrichedCardinality;
    private final int sampleCardinality;
    Integer rank = null;

    public static EnrichmentResult create(double p, double pCorrected, double pUnderRepresentedCorrected,
            String enrichedClassId, int numShared, int enrichedCardinality, int sampleCardinality) {
        return new EnrichmentResultImpl(p, pCorrected, pUnderRepresentedCorrected, enrichedClassId, numShared,
                enrichedCardinality,
                sampleCardinality);
    }
  

   
    public EnrichmentResultImpl(double p, double pCorrected, double pUnderRepresentedCorrected,
            String enrichedClassId, int numShared, int enrichedCardinality,
            int sampleCardinality) {
        super();
        this.p = p;
        this.pCorrected = pCorrected;
        this.pUnderRepresentedCorrected = pUnderRepresentedCorrected;
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
     * @return the pUnderRepresentedCorrected
     */
    public double getpUnderRepresentedCorrected() {
        return pUnderRepresentedCorrected;
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
                + ", pUnderRepresentedCorrected=" + pUnderRepresentedCorrected
                + ", enrichedClassId=" + enrichedClassId + ", numShared="
                + numShared + ", enrichedCardinality=" + enrichedCardinality
                + ", sampleCardinality=" + sampleCardinality + "]";
    }


 
    

}
