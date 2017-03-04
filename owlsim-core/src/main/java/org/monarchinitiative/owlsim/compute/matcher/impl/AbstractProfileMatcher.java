package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.inference.TestUtils;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.kb.filter.AnonIndividualFilter;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.FilterEngine;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MethodMetadata;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.ExecutionMetadataImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;

import com.google.common.base.Preconditions;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * common methods and variables for all ProfileMatchers
 * 
 * @author cjm
 *
 */
public abstract class AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(AbstractProfileMatcher.class);

	protected BMKnowledgeBase knowledgeBase;
	private FilterEngine filterEngine;

	/**
	 * @param knowledgeBase
	 */
	public AbstractProfileMatcher(BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
		this.filterEngine = FilterEngine.create(knowledgeBase);
	}

	/**
	 * @return ontology interface
	 */
	public BMKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}

	private void setKnowledgeBase(BMKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}

	public void precompute() {
	}

	/**
	 * all positive nodes in query plus their ancestors
	 * 
	 * @param q
	 * @return
	 */
	protected EWAHCompressedBitmap getProfileBM(ProfileQuery q) {
		return knowledgeBase.getSuperClassesBM(q.getQueryClassIds());
	}

	protected EWAHCompressedBitmap getDirectProfileBM(ProfileQuery q) {
		Set<Integer> positions = new HashSet<Integer>();
		for (String cid : q.getQueryClassIds()) {
			positions.add(knowledgeBase.getClassIndex(cid));
		}
		return EWAHUtils.convertIndexSetToBitmap(positions);
	}

	// given an array of class IDs c1...cn, return an array S1...Sn,
	// where Si is the set of superclasses (direct and indirect) of ci,
	// stored as a bitmap
	protected EWAHCompressedBitmap[] getProfileSetBM(String[] qcids) {
		EWAHCompressedBitmap[] bms = new EWAHCompressedBitmap[qcids.length];
		for (int i = 0; i < qcids.length; i++) {
			String qc = qcids[i];
			Preconditions.checkNotNull(qc);
			Preconditions.checkNotNull(knowledgeBase.getClassIndex(qc));
			bms[i] = knowledgeBase.getSuperClassesBM(qc);
		}
		return bms;
	}

	// a negated profile implicitly includes subclasses
	protected EWAHCompressedBitmap getNegatedProfileBM(ProfileQuery q) {
		if (!(q instanceof QueryWithNegation)) {
			return new EWAHCompressedBitmap();
		}
		QueryWithNegation nq = (QueryWithNegation) q;
		Set<Integer> bits = new HashSet<Integer>();
		for (String id : nq.getQueryNegatedClassIds()) {
			int ci = knowledgeBase.getClassIndex(id);
			bits.addAll(knowledgeBase.getSubClasses(ci).getPositions());
		}
		return EWAHUtils.convertIndexSetToBitmap(bits);
	}

	protected EWAHCompressedBitmap getDirectNegatedProfileBM(QueryWithNegation q) {
		Set<Integer> bits = new HashSet<Integer>();
		// TODO: less dumb implementation...
		for (String id : q.getQueryNegatedClassIds()) {
			int ci = knowledgeBase.getClassIndex(id);
			bits.add(ci);
		}
		return EWAHUtils.convertIndexSetToBitmap(bits);
	}

	protected Match createMatch(String matchId, String matchLabel, double s) {
		return MatchImpl.create(matchId, matchLabel, s);
	}

	/**
	 * @param filter
	 * @return list of individuals that satisfy filter
	 * @throws UnknownFilterException
	 */
	protected List<String> getFilteredIndividualIds(Filter filter) throws UnknownFilterException {
		return filterEngine.applyFilter(filter);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher#
	 * createProfileQuery(java.lang.String)
	 */
	public ProfileQuery createProfileQuery(String individualId) {
		return createProfileQuery(individualId, null);
	}

	public ProfileQuery createPositiveProfileQuery(String individualId) {
		return createProfileQuery(individualId, false);
	}

	public ProfileQuery createProfileQueryWithNegation(String individualId) {
		return createProfileQuery(individualId, true);
	}

	public ProfileQuery createProfileQuery(String individualId, Boolean isUseNegation) {
		Preconditions.checkNotNull(individualId);
		EWAHCompressedBitmap bmi = knowledgeBase.getDirectTypesBM(individualId);
		EWAHCompressedBitmap nbmi = knowledgeBase.getDirectNegatedTypesBM(individualId);
		Set<String> qcids = knowledgeBase.getClassIds(bmi);
		Set<String> nqcids = knowledgeBase.getClassIds(nbmi);
		ProfileQuery q;
		if (isUseNegation == null) {
			if (nqcids.size() == 0) {
				q = ProfileQueryImpl.create(qcids);
			} else {
				q = QueryWithNegationImpl.create(qcids, nqcids);
			}
		} else {
			if (isUseNegation) {
				q = QueryWithNegationImpl.create(qcids, nqcids);
			} else {
				q = ProfileQueryImpl.create(qcids);
			}
		}
		return q;
	}

	public ProfileQuery createProfileQueryFromClasses(Set<String> qcids, Set<String> nqcids) {
		ProfileQuery q;
		if (nqcids != null && nqcids.size() == 0) {
			q = ProfileQueryImpl.create(qcids);
		} else {
			q = QueryWithNegationImpl.create(qcids, nqcids);
		}
		return q;
	}

	public MatchSet findMatchProfile(String individualId) throws IncoherentStateException {
		ProfileQuery q = createProfileQuery(individualId);
		return findMatchProfile(q);
	}

	public MatchSet findMatchProfile(ProfileQuery q) throws IncoherentStateException {
		MatchSet ms = findMatchProfileAll(q);
		int limit = q.getLimit() == null ? 200 : q.getLimit();
		if (limit > -1) {
			ms.truncate(limit);
		}
		return ms;
	}

	public MatchSet findMatchProfile(ProfileQuery q, double alpha) throws IncoherentStateException {
		MatchSet ms = findMatchProfileAll(q);

		// use all matches as "background"
		// TODO this is a naive assumption, needs refactor
		DescriptiveStatistics ds = ms.getScores();
		MatchSet significantMatchingSet = MatchSetImpl.create(q);

		for (Match m : ms.getMatches()) {
			double p = TestUtils.tTest(m.getScore(), ds);
			if (p < alpha) {
				m.setSignificance(p);
				significantMatchingSet.add(m);
			}
		}
		return ms;
	}

	// additional layer of indirection above Impl, adds standard metadata
	private MatchSet findMatchProfileAll(ProfileQuery q) throws IncoherentStateException {
		long t1 = System.currentTimeMillis();
		MatchSet ms = findMatchProfileImpl(q); // implementing class
		long t2 = System.currentTimeMillis();
		ms.setExecutionMetadata(ExecutionMetadataImpl.create(t1, t2));
		LOG.info("t(ms)=" + ms.getExecutionMetadata().getDuration());
		MethodMetadata mmd = new MethodMetadata();
		mmd.methodName = getShortName();
		ms.setMethodMetadata(mmd);
		return ms;
	}

	public Match compareProfilePair(ProfileQuery q, ProfileQuery t)
			throws UnknownFilterException, IncoherentStateException {
		AnonIndividualFilter filter = new AnonIndividualFilter(t);
		q.setFilter(filter);
		MatchSet matchSet = findMatchProfile(q);
		return matchSet.getMatches().get(0);
	}

	// handling of anonymous individuals

	private boolean isAnonymousIndividual(String individualId) {
		return individualId.startsWith(AnonIndividualFilter.PREFIX);
	}

	protected EWAHCompressedBitmap getDirectTypesBM(String individualId) {
		if (isAnonymousIndividual(individualId)) {
			Set<String> cids = AnonIndividualFilter.getClassIdsFromExpression(individualId);
			return knowledgeBase.getClassesBM(cids);
		} else
			return knowledgeBase.getDirectTypesBM(individualId);
	}

	protected EWAHCompressedBitmap getTypesBM(String individualId) {
		if (isAnonymousIndividual(individualId)) {
			Set<String> cids = AnonIndividualFilter.getClassIdsFromExpression(individualId);
			return knowledgeBase.getSuperClassesBM(cids);
		} else
			return knowledgeBase.getTypesBM(individualId);
	}

	protected abstract MatchSet findMatchProfileImpl(ProfileQuery q) throws IncoherentStateException;
}
