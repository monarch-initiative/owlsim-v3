package org.monarchinitiative.owlsim.eval;

import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;

/**
 * Utility object for doing all-by-all comparisons.
 * 
 * @author cjm
 *
 */
public class CompareAllByAll {
	
	private Logger LOG = Logger.getLogger(CompareAllByAll.class);
	private boolean writeToStdout = false;
	ProfileMatcher profileMatcher;
	
	
	
	public CompareAllByAll(ProfileMatcher profileMatcher) {
		super();
		this.profileMatcher = profileMatcher;
	}

	/**
	 * Perform all-by-all comparison of all individuals.
	 * 
	 * The result is a 2D array indexed by the index-value of each individual
	 * 
	 * @return
	 * @throws UnknownFilterException
	 * @throws IncoherentStateException
	 */
	public int[][] compareAllByAll() throws UnknownFilterException, IncoherentStateException {
		BMKnowledgeBase kb = profileMatcher.getKnowledgeBase();
		Set<String> indIds = kb.getIndividualIdsInSignature();
		int n = indIds.size();
		int[][] sm = new int[n][n];
		for (int i=0; i<n; i++) {
			// this is a contorted way of doing things...
			String ni = kb.getIndividualId(i);
			MatchSet matchSet = profileMatcher.findMatchProfile(ni);
			for (Match match : matchSet.getMatches()) {
				String mid = match.getMatchId();
				int mix = kb.getIndividualIndex(mid);
				sm[i][mix] = match.getPercentageScore();
			}
		}
		return sm;
	}
	

}
