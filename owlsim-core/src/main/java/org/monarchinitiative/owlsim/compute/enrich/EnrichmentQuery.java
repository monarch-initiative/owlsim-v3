package org.monarchinitiative.owlsim.compute.enrich;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.filter.Filter;

public interface EnrichmentQuery {
    
    public String getClassId();
    public Set<String> getIndividualIds();
    
    /**
     * @return query filter
     */
    public Filter getFilter();
    
    public void setFilter(Filter f);
    
    public Integer getLimit();
    public void setLimit(Integer limit);


}
