package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;

public class BasicQueryFactory {
	
	public static BasicQuery createQuery(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		BasicQueryImpl q = new BasicQueryImpl(qids);
		return q;
	}
	
	public static BasicQuery createQuery(Set<String> ids) {
		BasicQueryImpl q = new BasicQueryImpl(ids);
		return q;
	}

	public static BasicQuery createQueryWithNegation(Set<String> labels, Set<String> negatedLabels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		Set<String> nqids = labelMapper.lookupByUniqueLabels(negatedLabels);
		QueryWithNegation q = QueryWithNegationImpl.create(qids, nqids);
		return q;
	}

	

}
