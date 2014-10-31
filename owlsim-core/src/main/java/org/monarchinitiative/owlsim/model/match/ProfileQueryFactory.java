package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;

public class ProfileQueryFactory {

	public static ProfileQuery createQuery(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		ProfileQueryImpl q = new ProfileQueryImpl(qids);
		return q;
	}

	public static ProfileQuery createQuery(Set<String> ids) {
		ProfileQueryImpl q = new ProfileQueryImpl(ids);
		return q;
	}

	public static ProfileQuery createQueryWithNegation(Set<String> ids, Set<String> negatedIds) {
		return QueryWithNegationImpl.create(ids, negatedIds);
	}

	public static ProfileQuery createQueryWithNegation(Set<String> labels, Set<String> negatedLabels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		Set<String> nqids = labelMapper.lookupByUniqueLabels(negatedLabels);
		QueryWithNegation q = QueryWithNegationImpl.create(qids, nqids);
		return q;
	}

}
