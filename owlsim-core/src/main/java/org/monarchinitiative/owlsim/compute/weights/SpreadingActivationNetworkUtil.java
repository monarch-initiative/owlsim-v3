package org.monarchinitiative.owlsim.compute.weights;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.WeightedQuery;

public class SpreadingActivationNetworkUtil {
	
	protected BMKnowledgeBase knowledgeBase;

	public WeightedQuery translateToWeightedProfile(ProfileQuery q) {
		if (q instanceof QueryWithNegation)
			return translateNegatableToWeightedProfile((QueryWithNegation)q);
		else
			return translatePositiveToWeightedProfile(q);
	}
	public WeightedQuery translateNegatableToWeightedProfile(QueryWithNegation q) {
		knowledgeBase.getSuperClassesBM(q.getQueryClassIds());
		return null;
	}
	public WeightedQuery translatePositiveToWeightedProfile(ProfileQuery q) {
		return null;
	}
	
}
