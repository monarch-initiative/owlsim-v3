package org.monarchinitiative.owlsim.eval;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.*;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Compare all by all
 * 
 * @author cjm
 *
 */
public class CompareAllByAll {
	
	private Logger LOG = Logger.getLogger(CompareAllByAll.class);
	private boolean writeToStdout = true;
	ProfileMatcher profileMatcher;
	
	
	
	public CompareAllByAll(ProfileMatcher profileMatcher) {
		super();
		this.profileMatcher = profileMatcher;
	}

	public int[][] compareAllByAll() {
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
