package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * See: PMID:23660285
 * 
 * This differs from the described algorithm in that only IC scores (not
 * jaccard) is used.
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
	@Override
	public MatchSet findMatchProfileImpl(ProfileQuery q) {

		// input query profile
		Set<String> qClassIds = q.getQueryClassIds();
		int qsize = qClassIds.size();
		queryClassArray = qClassIds.toArray(new String[qsize]);

		// array (in same order as queryClassArray) in which each element
		// is the set of superclasses of the indexed class
		EWAHCompressedBitmap queryProfileBMArr[] = getProfileSetBM(queryClassArray);
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);

		MatchSet mp = MatchSetImpl.create(q);

		// ---
		// calculate optimal match, based on matching of profile to itself;
		// has two components, maxIC and average of each phenotype in profile to
		// itself
		double maxScoreOfOptimalTarget = getScore(queryProfileBM, queryProfileBM);
		double avgScoreOfOptimalTarget = 0;

		for (int j = 0; j < qsize; j++) {
			EWAHCompressedBitmap queryBM = queryProfileBMArr[j];
			avgScoreOfOptimalTarget += getScore(queryBM, queryBM);
		}
		avgScoreOfOptimalTarget /= (double) qsize;
		// end of optimal target score calculation
		// ---

		// obtain target set and iterate through each one
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = getTypesBM(itemId);

			// calculate maximum IC
			double maxScore = getScore(queryProfileBM, targetProfileBM);

			EWAHCompressedBitmap targetProfileDirectBM = getDirectTypesBM(itemId);
			int tsize = targetProfileDirectBM.cardinality();

			// note: this is an experimental implementation that
			// does not make use of a MICA cache; it may be replaced by
			// a version that uses a cache later.
			double score = 0;

			// find best match for every class j in query profile
			for (int j = 0; j < qsize; j++) {
				EWAHCompressedBitmap queryBM = queryProfileBMArr[j];
				// find best match for Qj; we can optimize here;
				// rather than iterating through all Ti in T, we can
				// take the profile as a whole, as this is guaranteed the same
				// for maxIC
				// (note this optimization would not work for other metrics)
				// TODO: include the matching phenotypes plus LCS in the
				// results;
				// note this won't work with the existing optimization
				score += getScore(queryBM, targetProfileBM);

			}
			// find best match for every t in target profile
			int[] targetDirectTypeArr = targetProfileDirectBM.toArray();
			for (int j = 0; j < tsize; j++) {
				EWAHCompressedBitmap targetBM = knowledgeBase.getSuperClassesBM(targetDirectTypeArr[j]);
				// see notes above
				score += getScore(targetBM, queryProfileBM);
			}

			// calculate average for all comparisons
			score = score / (qsize + tsize);

			double combinedPercentageScore = ((100 * (maxScore / maxScoreOfOptimalTarget))
					+ (100 * (score / avgScoreOfOptimalTarget))) / 2;

			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			Match m = MatchImpl.create(itemId, label, combinedPercentageScore);
			mp.add(m);
		}
		mp.sortMatches();
		return mp;
	}

	// similarity score between two profiles: we use the MICA
	//
	// TODO - use the phenodigm score, which is the
	double getScore(EWAHCompressedBitmap qbm, EWAHCompressedBitmap tbm) {
		ClassInformationContentPair mica = getMicaCalculator().getMostInformativeCommonAncestorWithIC(qbm, tbm);
		if (mica == null) {
			LOG.error("No MICA between " + qbm + " -vs- " + tbm);
			return 0;
		}
		return mica.ic;
	}

}
