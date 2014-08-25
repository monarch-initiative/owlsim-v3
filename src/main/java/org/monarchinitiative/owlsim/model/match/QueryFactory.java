package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.model.match.impl.QueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;

public class QueryFactory {
	
	public static Query createQuery(Set<String> labels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		QueryImpl q = new QueryImpl(qids);
		return q;
	}
	
	public static Query createQuery(Set<String> ids) {
		QueryImpl q = new QueryImpl(ids);
		return q;
	}

	public static Query createQueryWithNegation(Set<String> labels, Set<String> negatedLabels, LabelMapper labelMapper) throws NonUniqueLabelException {
		Set<String> qids = labelMapper.lookupByUniqueLabels(labels);
		Set<String> nqids = labelMapper.lookupByUniqueLabels(negatedLabels);
		QueryWithNegation q = new QueryWithNegationImpl(qids, nqids);
		return q;
	}

	

}
