package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.FilterEngine;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
import org.monarchinitiative.owlsim.model.match.impl.ExecutionMetadataImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * common methods and variables for all ProfileMatchers 
 * 
 * @author cjm
 *
 */
public abstract class AbstractProfileMatcher implements ProfileMatcher {
	
	//private Logger LOG = Logger.getLogger(AbstractProfileMatcher.class);

	protected BMKnowledgeBase knowledgeBase;
	private FilterEngine filterEngine;

	
	/**
	 * @param knowledgeBase
	 */
	@Inject
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
	

	
	@Inject
	private void setKnowledgeBase(BMKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}



	protected EWAHCompressedBitmap getProfileBM(BasicQuery q) {
		return knowledgeBase.getSuperClassesBM(q.getQueryClassIds());
	}

	protected EWAHCompressedBitmap[] getProfileSetBM(String[] qcids) {
		EWAHCompressedBitmap[] bms = new EWAHCompressedBitmap[qcids.length];
		for (int i=0; i<qcids.length; i++) {
			bms[i] = knowledgeBase.getSuperClassesBM(qcids[i]);
		}
		return bms;
	}

	protected EWAHCompressedBitmap getNegatedProfileBM(QueryWithNegation q) {
		return knowledgeBase.getSuperClassesBM(q.getQueryNegatedClassIds());
	}

	public Match createMatch(String matchId, String matchLabel, double s) {
		return MatchImpl.create(matchId, matchLabel, s);
	}

	protected List<String> getFilteredIndividualIds(Filter filter) throws UnknownFilterException {
		return filterEngine.applyFilter(filter);
	}
	
	public MatchSet findMatchProfile(BasicQuery q) {
		long t1 = System.currentTimeMillis();
		MatchSet ms = findMatchProfileImpl(q);
		long t2 = System.currentTimeMillis();
		ms.setExecutionMetadata(ExecutionMetadataImpl.create(t1, t2));
		return ms;
	}

	protected abstract MatchSet findMatchProfileImpl(BasicQuery q);
}
