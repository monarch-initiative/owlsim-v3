package org.monarchinitiative.owlsim.compute.matcher.impl;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator;
import org.monarchinitiative.owlsim.compute.mica.impl.MostInformativeCommonAncestorCalculatorImpl;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

/**
 * common methods and variables for all ProfileMatcher that implement semantic
 * similarity techniques, i.e. those involving a MRCA
 * 
 * @author cjm
 *
 */
public abstract class AbstractSemanticSimilarityProfileMatcher extends AbstractProfileMatcher {

	// private Logger LOG =
	// Logger.getLogger(AbstractSemanticSimilarityProfileMatcher.class);

	private MostInformativeCommonAncestorCalculator micaCalculator;

	/**
	 * @param knowledgeBase
	 */
	public AbstractSemanticSimilarityProfileMatcher(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
		micaCalculator = new MostInformativeCommonAncestorCalculatorImpl(knowledgeBase);
	}

	/**
	 * @return object used for calculation of most informative common ancestors
	 */
	public MostInformativeCommonAncestorCalculator getMicaCalculator() {
		return micaCalculator;
	}

	/**
	 * @param micaCalculator
	 */
	private void setMicaCalculator(MostInformativeCommonAncestorCalculator micaCalculator) {
		this.micaCalculator = micaCalculator;
	}

}
