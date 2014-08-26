package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a maximum Information Content score
 * 
 * @author cjm
 *
 */
public class MaximumInformationContentSimilarityProfileMatcher 
	extends AbstractSemanticSimilarityProfileMatcher 
	implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(MaximumInformationContentSimilarityProfileMatcher.class);

	
	/**
	 * @param kb
	 */
	//@Inject
	private MaximumInformationContentSimilarityProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static MaximumInformationContentSimilarityProfileMatcher create(BMKnowledgeBase kb) {
		return new MaximumInformationContentSimilarityProfileMatcher(kb);
	}
	
	
	

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSetImpl findMatchProfile(BasicQuery q) {
		
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		//LOG.info("QUERY PROFILE for "+q+" "+queryProfileBM.getPositions());
		
		// TODO
		MatchSetImpl mp = new MatchSetImpl();
		mp.setQuery(q);
		
		// TODO: customize target set
		Set<String> indIds = knowledgeBase.getIndividualIdsInSignature();
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			
			//LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			ClassInformationContentPair mica = 
					getMicaCalculator().getMostInformativeCommonAncestorWithIC(queryProfileBM,
							targetProfileBM);
			//LOG.info("mica="+mica);
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			mp.add(createMatch(itemId, label, mica.ic));
		}
		mp.sortMatches();
		return mp;
	}


}
