package org.monarchinitiative.owlsim.compute.stats;

import java.util.Set;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.Filter;
import org.monarchinitiative.owlsim.kb.filter.IdFilter;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.ProfileQueryFactory;

import com.googlecode.javaewah.EWAHCompressedBitmap;


/**
 * This calculator can be used to evaluate matcher results
 * in the context of the knowledgebase.
 * 
 * @author nicole
 *
 */
public class KBMatcherCalculator {

	ProfileMatcher profileMatcher; 
	BMKnowledgeBase kb;
	DescriptiveStatistics[] matchScores;
	
	
	public KBMatcherCalculator(ProfileMatcher pm) {
		this.profileMatcher = pm;
		this.kb = pm.getKnowledgeBase();
		matchScores = new DescriptiveStatistics[kb.getIndividualIdsInSignature().size()];
	}
	
	
	/**
	 * Calculate the comparison of all x all
	 * individuals.  Since all individuals are being
	 * compared, and will be necessary for evaluating statistics
	 * we will store in an array.
	 * @throws IncoherentStateException 
	 * @throws UnknownFilterException 
	 */
	public void computeIxI() throws UnknownFilterException, IncoherentStateException {
		Set<String> individualIds = kb.getIndividualIdsInSignature();
		for (String iid : individualIds) {
			int ibit = kb.getIndividualIndex(iid);
			EWAHCompressedBitmap ibm = kb.getDirectTypesBM(iid);		
			Set<String> iids = kb.getClassIds(ibm);
			ProfileQuery q = ProfileQueryFactory.createQuery(iids);	
			//compare against all other individuals
			q.setLimit(-1);
			MatchSet ms = profileMatcher.findMatchProfile(q);
			matchScores[ibit] = ms.getScores();
		}
	}
	
	/**
	 * Calculate the comparison of all x all
	 * for a selected subset of individuals.
	 * @throws IncoherentStateException 
	 * @throws UnknownFilterException 
	 */
	public void computeIxI(Set<String> individualIds) throws UnknownFilterException, IncoherentStateException {
		Filter idFilter = new IdFilter(individualIds);
		
		for (String iid : individualIds) {
			int ibit = kb.getIndividualIndex(iid);
			EWAHCompressedBitmap ibm = kb.getDirectTypesBM(iid);		
			Set<String> iids = kb.getClassIds(ibm);
			ProfileQuery q = ProfileQueryFactory.createQuery(iids);	
			q.setFilter(idFilter);
			//compare against selected individuals
			q.setLimit(-1);
			MatchSet ms = profileMatcher.findMatchProfile(q);
			matchScores[ibit] = ms.getScores();
		}
	}
	
	//evaluate significance of matches
	
	
	
}
