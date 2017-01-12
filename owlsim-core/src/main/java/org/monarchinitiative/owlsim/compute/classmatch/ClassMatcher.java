package org.monarchinitiative.owlsim.compute.classmatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Finds matches between classes in the KB
 * 
 * @author cjm
 *
 */
public class ClassMatcher {

    BMKnowledgeBase kb;
    
   
    @Inject
    public ClassMatcher(BMKnowledgeBase kb) {
        super();
        this.kb = kb;
    }

    /**
     * Find best match for every class in ont1, where the best
     * match is in ont2
     * 
     * @param qOnt
     * @param tOnt
     * @return
     */
    public List<SimpleClassMatch> matchOntologies(String qOnt, String tOnt) {
        Set<String> qids = getClassIdsByOntology(qOnt);
        Set<String> tids = getClassIdsByOntology(tOnt);
        return matchClassSets(qids, tids);
    }

    public List<SimpleClassMatch> matchClassSets(Set<String> qids,
            Set<String> tids) {
        ArrayList<SimpleClassMatch> matches = new ArrayList<>();
        for (String q : qids) {
            matches.add(getBestMatch(q, tids));
        }
        return matches;
    }

    private SimpleClassMatch getBestMatch(String q, Set<String> tids) {
        EWAHCompressedBitmap qbm = kb.getSuperClassesBM(q);
        double bestEqScore = 0.0;
        String best = null;
        for (String t : tids) {
            EWAHCompressedBitmap tbm = kb.getSuperClassesBM(t);
            int numInQueryAndInTarget = qbm.andCardinality(tbm);
            int numInQueryOrInTarget = qbm.orCardinality(tbm);
            double eqScore = numInQueryAndInTarget / (double) numInQueryOrInTarget;
            if (eqScore > bestEqScore) {
                bestEqScore = eqScore;
                best = t;
            }
        }
            
        EWAHCompressedBitmap tbm = kb.getSuperClassesBM(best);
        int numInQueryAndInTarget = qbm.andCardinality(tbm);
        double subClassScore = numInQueryAndInTarget / (double) qbm.cardinality();
        double superClassScore = numInQueryAndInTarget / (double) tbm.cardinality();
            
        LabelMapper lm = kb.getLabelMapper();
        return new SimpleClassMatch(q, best,
                lm.getArbitraryLabel(q),
                lm.getArbitraryLabel(best),
                bestEqScore,
                subClassScore,
                superClassScore);
    }

    public Set<String> getClassIdsByOntology(String ont) {
        return kb.getClassIdsInSignature().stream().filter(x -> isIn(x, ont)).collect(Collectors.toSet());
    }

    public boolean isIn(String id, String ont) {
        // TODO - use curie util
        return id.startsWith(ont+":") || id.contains("/"+ont+"_");      
    }

}
