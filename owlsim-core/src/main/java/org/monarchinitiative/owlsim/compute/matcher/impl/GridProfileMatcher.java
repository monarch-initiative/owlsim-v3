package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.GridMatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return matches,
 * with each match containing a comparison for each class
 * 
 * TODO: this is INCOMPLETE
 * 
 * @author cjm
 *
 */
public class GridProfileMatcher extends AbstractSemanticSimilarityProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(GridProfileMatcher.class);
	private String[] queryClassArray;
	
	/**
	 * @param kb
	 */
	@Inject
	public GridProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new GridProfileMatcher(kb);
	}
	

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		
		
		Set<String> qClassIds = q.getQueryClassIds();
		int qsize = qClassIds.size();
		queryClassArray = qClassIds.toArray(new String[qsize]);
		EWAHCompressedBitmap queryProfileBMArr[] = getProfileSetBM(queryClassArray);
		
		MatchSet mp =  MatchSetImpl.create(q);
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			
			double score = 0;
			ClassInformationContentPair[] qmatchArr = new ClassInformationContentPair[qsize];
			for (int j = 0; j<qsize; j++) {
				EWAHCompressedBitmap queryProfileBM = queryProfileBMArr[j];
				//LOG.info("  QUERY PROFILE for "+queryClassArray[j]+" "+queryProfileBM);
				ClassInformationContentPair mica = 
						getMicaCalculator().getMostInformativeCommonAncestorWithIC(queryProfileBM,
								targetProfileBM);

				// Scores are summed.
				// Equivalent to naive assumption of independence
				// p1 * p2 * ... p_n
				score += mica.ic;
				qmatchArr[j] = mica;
			
			}
			// penalize targets with multiple annotations
			// TODO - allow weighting
			// Note directTypes should be pre-filtered for redundancy, if
			// calculated using an owl reasoner
			score /= Math.sqrt(knowledgeBase.getDirectTypesBM(itemId).cardinality());

			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			Match m = GridMatchImpl.create(itemId, label, score, qmatchArr);
			mp.add(m);
		}
		mp.sortMatches();
		return mp;
	}





}
