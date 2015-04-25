package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;

/**
 * methods for finding a scored set of candidates given a class query profile
 * 
 *
 * @author cjm
 *
 */
public interface ProfileMatcher {

	/***
	 * @return a short, URL friendly (no spaces, please) description of the matcher
	 */
	String getShortName();
	
	/**
	 * Use q to scan all individuals in the kb that match the filter,
	 * returned a sorted scored list of matches
	 * 
	 * @param q
	 * @return scored matches
	 * @throws UnknownFilterException 
	 */
	MatchSet findMatchProfile(ProfileQuery q) throws UnknownFilterException;

	/**
	 * Finds matches for a given individual already known to the kb
	 * 
	 * @param q
	 * @param alpha - pvalue cutoff
	 * @return scored matches
	 * @throws UnknownFilterException 
	 */
	MatchSet findMatchProfile(ProfileQuery q, double alpha) throws UnknownFilterException;

	/**
	 * 
	 * 
	 * @param individualId
	 * @return scored matches
	 * @throws UnknownFilterException
	 */
	public MatchSet findMatchProfile(String individualId) throws UnknownFilterException;

	
	/**
	 * @return ontology interface
	 */
	BMKnowledgeBase getKnowledgeBase();
}
