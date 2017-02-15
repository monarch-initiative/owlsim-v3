package org.monarchinitiative.owlsim.compute.enrich.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

public class EnrichmentAllByAll {

    private Logger LOG = Logger.getLogger(EnrichmentAllByAll.class);

    /**
     * @param engine
     * @param rootCls1
     * @param rootCls2
     * @return list of resultsets
     */
    public List<EnrichmentResultSet> getAllByAll(EnrichmentEngine engine,
            String rootCls1, String rootCls2) {
        
        BMKnowledgeBase kb = engine.getKnowledgeBase();
        
        EWAHCompressedBitmap bm1 = kb.getSubClasses(kb.getClassIndex(rootCls1));
        EWAHCompressedBitmap bm2 = kb.getSubClasses(kb.getClassIndex(rootCls2));
        Set<String> targetClassIds = kb.getClassIds(bm2);
        
        List<EnrichmentResultSet> resultSets = new ArrayList<>();
        for (Integer c1ix : bm1.getPositions()) {
            String c1 = kb.getClassId(c1ix);
            LOG.info("Performing enrichment for individual set:" +c1);
            EnrichmentQuery query = new EnrichmentQueryImpl(c1);
            EnrichmentResultSet rs = engine.calculateEnrichmentAgainstKb(query, targetClassIds);
            if (rs.getResults().size() > 0)
                resultSets.add(rs);
        }
        
        return resultSets;
    }

}
    
   