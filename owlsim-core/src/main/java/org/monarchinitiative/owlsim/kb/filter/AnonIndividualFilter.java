package org.monarchinitiative.owlsim.kb.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;

/**
 * A filter for anonymous individuals
 * 
 * Anonymous individuals are stored as simple class expressions,
 * consisting of positive and negative class ids
 * 
 * @author cjm
 */
public class AnonIndividualFilter implements Filter {
    
    public final static String PREFIX = "Types: ";

    Set<String> classIds = new HashSet<>();
    Set<String> negatedClassIds = new HashSet<>();
    
    
    public AnonIndividualFilter(Set<String> classIds,
            Set<String> negatedClassIds) {
        super();
        this.classIds = classIds;
        this.negatedClassIds = negatedClassIds;
    }
    

    public AnonIndividualFilter(ProfileQuery q) {
        super();
        this.classIds = q.getQueryClassIds();
        if (q instanceof QueryWithNegation) 
            this.negatedClassIds = ((QueryWithNegation)q).getQueryNegatedClassIds();
   }


    /**
     * @return the classIds
     */
    public Set<String> getClassIds() {
        return classIds;
    }
    /**
     * @param classIds the classIds to set
     */
    public void setClassIds(Set<String> classIds) {
        this.classIds = classIds;
    }
    /**
     * @return the negatedClassIds
     */
    public Set<String> getNegatedClassIds() {
        return negatedClassIds;
    }
    /**
     * @param negatedClassIds the negatedClassIds to set
     */
    public void setNegatedClassIds(Set<String> negatedClassIds) {
        this.negatedClassIds = negatedClassIds;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String str = PREFIX+String.join(" ", classIds);
        if (negatedClassIds != null && negatedClassIds.size() > 0) {
            return str +
                String.join(" "+
                        negatedClassIds.stream().map(s -> "-"+s).collect(Collectors.toList()));
        }
        else {
            return str;
        }
    }
    
    public static Set<String> getClassIdsFromExpression(String expr) {
        
        expr = expr.replace(PREFIX, "");
        return Arrays.stream(expr.split(" ")).filter(x -> !x.startsWith("-")).
                collect(Collectors.toSet());
    }
   public static Set<String> getNegatedClassIdsFromExpression(String expr) {
        
        expr = expr.replace(PREFIX, "");
        return Arrays.stream(expr.split(" ")).filter(x -> x.startsWith("-")).
                map(x->x.replace("-", "")).
                        collect(Collectors.toSet());
    }
   
	

}
