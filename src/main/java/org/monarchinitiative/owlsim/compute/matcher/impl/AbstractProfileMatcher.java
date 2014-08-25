package org.monarchinitiative.owlsim.compute.matcher.impl;

import javax.inject.Inject;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.Query;
import org.monarchinitiative.owlsim.model.match.QueryWithNegation;
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

	
	/**
	 * @param knowledgeBase
	 */
	@Inject
	public AbstractProfileMatcher(BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
	}



	public AbstractProfileMatcher() {
		super();
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



	protected EWAHCompressedBitmap getProfileBM(Query q) {
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


}
