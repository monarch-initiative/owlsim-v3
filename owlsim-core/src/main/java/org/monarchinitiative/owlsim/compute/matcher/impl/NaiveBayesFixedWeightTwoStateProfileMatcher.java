package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Calculate likelihood of query 'mutating' into target, assuming each
 * node in the ontology is independent (after pre-computing ancestor nodes
 * for query and target), using chain rule
 * 
 * p(C1=c1) * p(C2=c2) * ... p(Cn=cn)
 * 
 * Where p(Ci=ci) takes on one of 4 possibilities, depending on state
 * of query and state of target, corresponding to probability of misclassification.
 * 
 * 
 * 
 * 
 * 
 * @author cjm
 *
 */
public class NaiveBayesFixedWeightTwoStateProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(NaiveBayesFixedWeightTwoStateProfileMatcher.class);
	
	// set this to more than 1 for frequency-aware;
	// a value of 0 defaults to frequency-unaware
    private int kLeastFrequent = 0;


	@Deprecated
	private double defaultFalsePositiveRate = 0.002; // alpha

	@Deprecated
	private double defaultFalseNegativeRate = 0.10; // beta
	
	/**
	 * A tuple of (weight, Classes)
	 *
	 */
	private class WeightedTypesBM {
	    // bitmap representing a set of classes assumed to be on
	    final EWAHCompressedBitmap typesBM;
	    
	    // probability of the state in which all such classes are on
	    final double weight;
	    
        public WeightedTypesBM(EWAHCompressedBitmap typesBM, Double weight) {
            super();
            this.typesBM = typesBM;
            this.weight = weight;
        }
	}

	// TODO - replace when tetsing is over
	//private double[] defaultFalsePositiveRateArr =  new double[]{0.002};
	//private double[] defaultFalseNegativeRateArr = new double[] {0.10};
	private double[] defaultFalsePositiveRateArr =  new double[]{1e-10,0.0005,0.001,0.005,0.01};
	private double[] defaultFalseNegativeRateArr = new double[] {1e-10,0.005,0.01,0.05,0.1,0.2,0.4,0.8,0.9};
	
	// for maps a pair of (Individual, InterpretationIndex) to a set of inferred (self, direct, indirect) types
	private Map<Integer,Map<Integer,WeightedTypesBM>> individualToInterpretationToTypesBM = new HashMap<>();

    @Inject
	protected NaiveBayesFixedWeightTwoStateProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}

	/**
	 * @param kb
	 * @return new instance
	 */
	public static NaiveBayesFixedWeightTwoStateProfileMatcher create(BMKnowledgeBase kb) {
		return new NaiveBayesFixedWeightTwoStateProfileMatcher(kb);
	}

	public boolean isUseBlanket() {
		return true;
	}

	@Override
	public String getShortName() {
		return "naive-bayes-fixed-weight-two-state";
	}
	
	

	/**
     * @return the kLeastFrequent
     */
    public int getkLeastFrequent() {
        return kLeastFrequent;
    }

    /**
     * The default for this should be 0. When 0, the behavior is as for frequency unaware
     * (i.e. every instance-class association with frequency info will be treated as normal instance-class)
     * 
     * When k>1, will make use of the k least frequent annotations in probabilistic calculation
     * 
     * @param kLeastFrequent the kLeastFrequent to set
     */
    public void setkLeastFrequent(int kLeastFrequent) {
        // reset cache
        individualToInterpretationToTypesBM = new HashMap<>();
        this.kLeastFrequent = kLeastFrequent;
    }

    /**
	 * Extends the query profile - for every node c, all the direct parents of c are in
	 * the query profile, then add c to the query profile.
	 * 
	 * We use this to reduce the size of the network when testing for probabilities
	 * 
	 * TODO: fully evaluate the consequences of using this method
	 * 
	 * @param q
	 * @return
	 */
	private EWAHCompressedBitmap getQueryBlanketBM(ProfileQuery q) {
		EWAHCompressedBitmap onQueryNodesBM = getProfileBM(q);
		Set<Integer> nodesWithOnParents = new HashSet<Integer>();

		// there may be more efficient ways of doing this, but this is
		// called once at the start of the search...
		for (String cid : knowledgeBase.getClassIdsInSignature()) {
			int cix = knowledgeBase.getClassIndex(cid);
			EWAHCompressedBitmap supsBM = knowledgeBase.getDirectSuperClassesBM(cid);
			int nParents = supsBM.cardinality();
			if (supsBM.andCardinality(onQueryNodesBM) == nParents) {
				nodesWithOnParents.add(cix);
			}
		}

		return onQueryNodesBM.or(EWAHUtils.convertIndexSetToBitmap(nodesWithOnParents));
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		//double fpr = getFalsePositiveRate();
		//double fnr = getFalseNegativeRate();
		double sumOfProbs = 0.0;

		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap queryBlanketProfileBM = getQueryBlanketBM(q);
		LOG.info("|OnQueryNodes|="+queryProfileBM.cardinality());
		LOG.info("|QueryNodesWithOnParents|="+queryBlanketProfileBM.cardinality());

		
		//int numClassesConsidered = knowledgeBase.getClassIdsInSignature().size();
		int numClassesConsidered;
		if (isUseBlanket()) {
			numClassesConsidered = queryBlanketProfileBM.cardinality();
		}
		else {
			numClassesConsidered = knowledgeBase.getClassIdsInSignature().size();
		}

		MatchSet mp = MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double pvector[] = new double[indIds.size()];
		String indArr[] = new String[indIds.size()];
		int n=0;
		
		
		for (String itemId : indIds) {
		    
		    int effectiveK = kLeastFrequent;
	        int twoToTheK = (int) Math.pow(2, kLeastFrequent);
	        int numWeightedTypes = knowledgeBase.getDirectWeightedTypes(itemId).size();
	        if (numWeightedTypes < kLeastFrequent) {
	            twoToTheK = (int) Math.pow(2, numWeightedTypes);
	            effectiveK = numWeightedTypes;
	        }
		    
		    double cumulativePr = 0;
		    for (int comboIndex = 0; comboIndex < twoToTheK; comboIndex++) {
		        
		        Double comboPr = null;
		        EWAHCompressedBitmap targetProfileBM;
		        if (kLeastFrequent == 0) {
		            targetProfileBM = knowledgeBase.getTypesBM(itemId);
		        }
		        else {
		            WeightedTypesBM wtbm = getTypesFrequencyAware(itemId, comboIndex, effectiveK);
		            comboPr = wtbm.weight;
		            targetProfileBM = wtbm.typesBM;
		        }
		    
		        // any node which has an off query parent is discounted
		        targetProfileBM = targetProfileBM.and(queryBlanketProfileBM);
		        LOG.debug("TARGET PROFILE for "+itemId+" "+targetProfileBM);


		        // two state model.
		        // mapping to Bauer et al: these correspond to mxy1, x=Q, y=H/T
		        int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
		        int numInQueryAndNOTInTarget = queryProfileBM.andNotCardinality(targetProfileBM);
		        int numNOTInQueryAndInTarget = targetProfileBM.andNotCardinality(queryProfileBM);
		        int numNOTInQueryAndNOTInTarget = 
		                numClassesConsidered - (numInQueryAndInTarget + numInQueryAndNOTInTarget + numNOTInQueryAndInTarget);

		        double p = 0.0;
		        // TODO: optimize this
		        // integrate over a Dirichlet prior for alpha & beta, rather than gridsearch
		        // this can be done closed-form
		        for (double fnr : defaultFalseNegativeRateArr) {
		            for (double fpr : defaultFalsePositiveRateArr) {

		                double pQ1T1 = Math.pow(1-fnr,  numInQueryAndInTarget);
		                double pQ0T1 = Math.pow(fnr,  numNOTInQueryAndInTarget);
		                double pQ1T0 = Math.pow(fpr,  numInQueryAndNOTInTarget);
		                double pQ0T0 = Math.pow(1-fpr,  numNOTInQueryAndNOTInTarget);



		                //LOG.debug("pQ1T1 = "+(1-fnr)+" ^ "+ numInQueryAndInTarget+" = "+pQ1T1);
		                //LOG.debug("pQ0T1 = "+(fnr)+" ^ "+ numNOTInQueryAndInTarget+" = "+pQ0T1);
		                //LOG.debug("pQ1T0 = "+(fpr)+" ^ "+ numInQueryAndNOTInTarget+" = "+pQ1T0);
		                //LOG.debug("pQ0T0 = "+(1-fpr)+" ^ "+ numNOTInQueryAndNOTInTarget+" = "+pQ0T0);
		                //TODO: optimization. We can precalculate the logs for different integers
		                p += 
		                        Math.exp(Math.log(pQ1T1) + Math.log(pQ0T1) + Math.log(pQ1T0) + Math.log(pQ0T0));

		            }
		        }
		        
		        if (comboPr != null) {
		            p *= comboPr;
		        }
		        cumulativePr += p;
		    }
		    pvector[n] = cumulativePr;
		    indArr[n] = itemId;
		    
			sumOfProbs += cumulativePr;
			n++;
			LOG.debug("p for "+itemId+" = "+cumulativePr);
		    
		}
		for (n = 0; n<pvector.length; n++) {
			double p = pvector[n] / sumOfProbs;
			String id = indArr[n];
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(id);
			mp.add(createMatch(id, label, p));
		}
		mp.sortMatches();
		return mp;
	}
	
	// for a value of n such that: 0 <= n < 2^k
	// where n represents a particular combination of k boolean values, t1, ..., tk,
	// each representing the truth value for whether the class t_i  is indexed for a
	// given individual i.
	//
	// t1..tk will be the k least frequent annotations for this individual
	//
	// uses caching
	private WeightedTypesBM getTypesFrequencyAware(String itemId, int n, int effectiveK) {
	    Integer iix = knowledgeBase.getIndividualIndex(itemId);
	    if (!individualToInterpretationToTypesBM.containsKey(iix)) {
	        individualToInterpretationToTypesBM.put(iix, new HashMap<>());
	    }
	    Map<Integer, WeightedTypesBM> m = individualToInterpretationToTypesBM.get(iix);
 	    if (m.containsKey(n)) {
	        // use cached value
	        return m.get(n);
	    }
	    
	    // default direct type map.
	    // note that associations with frequency annotations are includes here alongside
	    // normal associations
	    EWAHCompressedBitmap dtmap = knowledgeBase.getDirectTypesBM(itemId);
	    
	    // associations with frequency info
	    // map is from ClassIndex -> Weight
        Map<Integer, Integer> wmap = knowledgeBase.getDirectWeightedTypes(itemId);
        
        // sort with least frequent first
	    List<Integer> sortedTypeIndices = new ArrayList<>(wmap.keySet());
	    sortedTypeIndices.sort( (Integer i, Integer j) -> wmap.get(i) - wmap.get(j));
	    
	    EWAHCompressedBitmap mask = new EWAHCompressedBitmap();
	    double pr = 1.0;
	    for (int i=0; i< effectiveK; i++) {
	        Integer iClassIx = sortedTypeIndices.get(i);
	        Double w = wmap.get(iClassIx) / 100.0;
	        //LOG.info("Class "+iClassIx +" which is "+i+"-least frequent has weight "+w+" for individual "+itemId+" in combo "+n);
	        if ( (n >> i) % 2 == 0) {
	            mask.set(iClassIx);            
	            pr *= 1-w;
	        }
	        else {
	            pr *= w;
	        }
	    }
        //LOG.info("Instance "+itemId+" in combo "+n+" has Pr = "+pr);

	    EWAHCompressedBitmap dtmapMasked = dtmap.xor(mask);
	    EWAHCompressedBitmap inferredTypesBM = knowledgeBase.getSuperClassesBM(dtmapMasked);
	    WeightedTypesBM wtbm = new WeightedTypesBM(inferredTypesBM, pr);
	    m.put(n, wtbm);
	    return wtbm;
	}

	/**
	 * @return probability a query class is a false positive
	 */
	@Deprecated
	public double getFalsePositiveRate() {
		return defaultFalsePositiveRate;		
	}

	/**
	 * @return probability absence of a query class is a false negative
	 */
	@Deprecated
	public double getFalseNegativeRate() {
		return defaultFalseNegativeRate;		
	}


	public void compare(String qid, String tid) {
		ProfileQuery q = createProfileQuery(qid);
		ProfileQuery t = createProfileQuery(tid);
		
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		EWAHCompressedBitmap targetProfileBM = getProfileBM(t);
		EWAHCompressedBitmap queryBlanketProfileBM = getQueryBlanketBM(q);
		targetProfileBM = targetProfileBM.and(queryBlanketProfileBM);

		
		int numClassesConsidered = queryBlanketProfileBM.cardinality();
		
		int numInQuery = queryProfileBM.cardinality();
		int numInTarget = targetProfileBM.cardinality();
		
		
		int numInQueryAndInTarget = queryProfileBM.andCardinality(targetProfileBM);
		int numInQueryAndNOTInTarget = queryProfileBM.andNotCardinality(targetProfileBM);
		int numNOTInQueryAndInTarget = targetProfileBM.andNotCardinality(queryProfileBM);
		int numNOTInQueryAndNOTInTarget = 
				numClassesConsidered - (numInQueryAndInTarget + numInQueryAndNOTInTarget + numNOTInQueryAndInTarget);

		// TODO: return appropriate data structure; this is currently only used for testing
		// LAST = fnr \t fpr
		System.out.println(qid+"\t"+tid+"\t"+numInQueryAndInTarget+
				"\t"+numInQueryAndNOTInTarget+"\t"+numNOTInQueryAndInTarget+
				"\t"+numNOTInQueryAndNOTInTarget+
				"\t"+numNOTInQueryAndInTarget/(double)numInTarget+"\t"+
				"\t"+numInQueryAndNOTInTarget/(double)numInQuery);

	}

}
