package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * See: PMID:23660285
 * 
 * This differs from the described algorithm in that only IC scores (not jaccard) is used.
 * 
 * @author cjm
 *
 */
public class PhenodigmICProfileMatcher extends AbstractSemanticSimilarityProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(PhenodigmICProfileMatcher.class);
	private String[] queryClassArray;
	
	/**
	 * @param kb
	 */
	@Inject
	public PhenodigmICProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new PhenodigmICProfileMatcher(kb);
	}
	
	@Override
	public String getShortName() {
		return "phenodigm";
	}
	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		
		
		Set<String> qClassIds = q.getQueryClassIds();
		int qsize = qClassIds.size();
		queryClassArray = qClassIds.toArray(new String[qsize]);
		
		// array (in same order as queryClassArray) in which each element
		// is th set of superclasses of the indexed class
		EWAHCompressedBitmap queryProfileBMArr[] = getProfileSetBM(queryClassArray);
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);

		MatchSet mp =  MatchSetImpl.create(q);
		
		// optimal match
		double maxScoreOfOptimalTarget = getScore(queryProfileBM, queryProfileBM);
		double avgScoreOfOptimalTarget = 0;
		
		for (int j = 0; j<qsize; j++) {
			EWAHCompressedBitmap queryBM = queryProfileBMArr[j];
			avgScoreOfOptimalTarget += getScore(queryBM, queryBM);
		}
		avgScoreOfOptimalTarget /= (double)qsize;
		// end of optimal target
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			double maxScore = getScore(queryProfileBM, targetProfileBM);
			
			EWAHCompressedBitmap targetProfileDirectBM = knowledgeBase.getDirectTypesBM(itemId);
			int tsize = targetProfileDirectBM.cardinality();
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			
			// note: this is an experimental implementation that
			// does not make use of a MICA cache; it may be replaced by
			// a version that uses a cache later.
			double score = 0;

			// find best match for every q in query profile
			for (int j = 0; j<qsize; j++) {
				EWAHCompressedBitmap queryBM = queryProfileBMArr[j];
				// TODO - this isn't quite right. need to compare Q vs Q
				score += getScore(queryBM, targetProfileBM);
			}
			// find best match for every t in target profile
			int[] targetDirectTypeArr = targetProfileDirectBM.toArray();
			for (int j = 0; j<tsize; j++) {
				EWAHCompressedBitmap targetBM = knowledgeBase.getSuperClassesBM(targetDirectTypeArr[j]);
				// TODO - this isn't quite right. need to compare Q vs Q
				score += getScore(targetBM, queryProfileBM);
			}
			score = score / (qsize + tsize);
			double combinedPercentageScore = 
					((100 * (maxScore / maxScoreOfOptimalTarget)) + (100 * (score / avgScoreOfOptimalTarget)))/2;
			

			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			Match m = MatchImpl.create(itemId, label, combinedPercentageScore);
			mp.add(m);
		}
		mp.sortMatches();
		return mp;
	}


	// TODO - use the phenodigm score, which is the 
	double getScore(EWAHCompressedBitmap qbm, EWAHCompressedBitmap tbm) {
		ClassInformationContentPair mica = 
				getMicaCalculator().getMostInformativeCommonAncestorWithIC(qbm,
						tbm);
		return  mica.ic;
	}


}
