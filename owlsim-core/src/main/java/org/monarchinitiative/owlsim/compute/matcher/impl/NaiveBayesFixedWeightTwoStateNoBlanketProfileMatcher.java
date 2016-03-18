package org.monarchinitiative.owlsim.compute.matcher.impl;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * @author cjm
 *
 */
public class NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher extends NaiveBayesFixedWeightTwoStateProfileMatcher {

	@Inject
	private NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher create(BMKnowledgeBase kb) {
		return new NaiveBayesFixedWeightTwoStateNoBlanketProfileMatcher(kb);
	}


	public boolean isUseBlanket() {
		return false;
	}
	
	@Override
	public String getShortName() {
		return "naive-bayes-fixed-weight-two-state-NOBLANKET";
	}

	
}
