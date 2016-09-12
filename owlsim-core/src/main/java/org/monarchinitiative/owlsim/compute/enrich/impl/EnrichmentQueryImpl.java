package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.util.Set;

import javax.management.RuntimeErrorException;

import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.kb.filter.Filter;

public class EnrichmentQueryImpl implements EnrichmentQuery {

    private String classId;
    private Set<String> individualIds;
    
    private Filter filter;
    
    private Integer limit;
    
    
   
    public EnrichmentQueryImpl(String classId) {
        super();
        this.classId = classId;
    }
    public EnrichmentQueryImpl(Set<String> individualIds) {
        super();
        this.individualIds = individualIds;
    }
    public static EnrichmentQuery create(String cid) {
        return new EnrichmentQueryImpl(cid);
    }
    public static EnrichmentQuery create(Set<String> individualIds) {
        return new EnrichmentQueryImpl(individualIds);
    }
    
    public static EnrichmentQuery create(String cid,
            Set<String> individualIds) {
        if (cid == null) {
            return new EnrichmentQueryImpl(individualIds);
        }
        else if (individualIds == null || individualIds.size() == 0) {
            return new EnrichmentQueryImpl(cid);           
        }
        else {
            throw new RuntimeErrorException(null, "invalid query: "+cid+" -- "+individualIds);
        }
    }


    /**
     * @return the classId
     */
    public String getClassId() {
        return classId;
    }


    /**
     * @param classId the classId to set
     */
    public void setClassId(String classId) {
        this.classId = classId;
    }

    

  

    /**
     * @return the individualIds
     */
    public Set<String> getIndividualIds() {
        return individualIds;
    }
    /**
     * @param individualIds the individualIds to set
     */
    public void setIndividualIds(Set<String> individualIds) {
        this.individualIds = individualIds;
    }
    /**
     * @return the filter
     */
    public Filter getFilter() {
        return filter;
    }


    /**
     * @param filter the filter to set
     */
    public void setFilter(Filter filter) {
        this.filter = filter;
    }


    /**
     * @return the limit
     */
    public Integer getLimit() {
        return limit;
    }


    /**
     * @param limit the limit to set
     */
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "EnrichmentQueryImpl [classId=" + classId + ", individualIds="
                + individualIds + ", filter=" + filter + ", limit=" + limit
                + "]";
    }
 

    
}
