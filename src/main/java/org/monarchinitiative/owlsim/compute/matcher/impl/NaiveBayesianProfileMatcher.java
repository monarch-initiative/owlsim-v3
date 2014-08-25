package org.monarchinitiative.owlsim.compute.matcher.impl;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Query;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a probability of being the match,
 * based on multiplying the probabilities of the set of all classes being on/off, given
 * the item is true.
 * 
 * TODO: this is INCOMPLETE
 * 
 * @author cjm
 *
 */
public class NaiveBayesianProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {
	
	//private Logger LOG = Logger.getLogger(NaiveBayesianProfileMatcher.class);

	private double[][] likelihoods; // p( feature_i | label_j )
	private double[] priors; // p(label_j)
	
	/**
	 * @param kb
	 */
	public NaiveBayesianProfileMatcher(BMKnowledgeBase kb) {
		super();
		this.knowledgeBase = kb;
	}
	


	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSetImpl findMatchProfile(Query q) {
		// TODO
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		
		MatchSetImpl mp = new MatchSetImpl();
		mp.setQuery(q);
		
		mp.sortMatches();
		return mp;
	}





}