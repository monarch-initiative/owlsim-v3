package org.monarchinitiative.owlsim.compute.classmatch;

public class SimpleClassMatch {
    
    private String queryClassId;
    private String matchClassId;
    private String queryClassLabel;
    private String matchClassLabel;
    
    private double eqScore;
    private double subClassScore;
    private double superClassScore;
    public SimpleClassMatch(String queryClassId, String matchClassId,
            String queryClassLabel, String matchClassLabel, double eqScore,
            double subClassScore, double superClassScore) {
        super();
        this.queryClassId = queryClassId;
        this.matchClassId = matchClassId;
        this.queryClassLabel = queryClassLabel;
        this.matchClassLabel = matchClassLabel;
        this.eqScore = eqScore;
        this.subClassScore = subClassScore;
        this.superClassScore = superClassScore;
    }
    /**
     * @return the queryClassId
     */
    public String getQueryClassId() {
        return queryClassId;
    }
    /**
     * @param queryClassId the queryClassId to set
     */
    public void setQueryClassId(String queryClassId) {
        this.queryClassId = queryClassId;
    }
    /**
     * @return the matchClassId
     */
    public String getMatchClassId() {
        return matchClassId;
    }
    /**
     * @param matchClassId the matchClassId to set
     */
    public void setMatchClassId(String matchClassId) {
        this.matchClassId = matchClassId;
    }
    /**
     * @return the queryClassLabel
     */
    public String getQueryClassLabel() {
        return queryClassLabel;
    }
    /**
     * @param queryClassLabel the queryClassLabel to set
     */
    public void setQueryClassLabel(String queryClassLabel) {
        this.queryClassLabel = queryClassLabel;
    }
    /**
     * @return the matchClassLabel
     */
    public String getMatchClassLabel() {
        return matchClassLabel;
    }
    /**
     * @param matchClassLabel the matchClassLabel to set
     */
    public void setMatchClassLabel(String matchClassLabel) {
        this.matchClassLabel = matchClassLabel;
    }
    /**
     * @return the eqScore
     */
    public double getEqScore() {
        return eqScore;
    }
    /**
     * @param eqScore the eqScore to set
     */
    public void setEqScore(double eqScore) {
        this.eqScore = eqScore;
    }
    /**
     * @return the subClassScore
     */
    public double getSubClassScore() {
        return subClassScore;
    }
    /**
     * @param subClassScore the subClassScore to set
     */
    public void setSubClassScore(double subClassScore) {
        this.subClassScore = subClassScore;
    }
    /**
     * @return the superClassScore
     */
    public double getSuperClassScore() {
        return superClassScore;
    }
    /**
     * @param superClassScore the superClassScore to set
     */
    public void setSuperClassScore(double superClassScore) {
        this.superClassScore = superClassScore;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SimpleClassMatch [queryClassId=" + queryClassId
                + ", matchClassId=" + matchClassId + ", queryClassLabel="
                + queryClassLabel + ", matchClassLabel=" + matchClassLabel
                + ", eqScore=" + eqScore + ", subClassScore=" + subClassScore
                + ", superClassScore=" + superClassScore + "]";
    }
    
    
    

}
