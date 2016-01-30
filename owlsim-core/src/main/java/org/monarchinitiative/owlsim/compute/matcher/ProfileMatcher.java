package org.monarchinitiative.owlsim.compute.matcher;

import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
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
	 * Some matchers require a costly precomputation step. Subsequent calls should
	 * have no effect.
	 * 
	 * To ensure that all precomputations are performed ahead of time, call
	 * this after initialization of the matcher object
	 */
	public void precompute();
	
	/**
	 * Translate the phenotype profile of an individual to a ProfileQuery object
	 * 
	 * @param individualId
	 * @return profile query corresponding to individual
	 */
	public ProfileQuery createProfileQuery(String individualId);


	/**
	 * As {@link createProfileQuery}, but ignore all negated statements
	 * 
	 * @param individualId
	 * @return profile query corresponding to individual
	 */
	public ProfileQuery createPositiveProfileQuery(String individualId);

	
	
	/**
	 * Use q to scan all individuals in the kb that match the filter,
	 * returned a sorted scored list of matches
	 * 
	 * @param q
	 * @return scored matches
	 * @throws UnknownFilterException 
	 * @throws IncoherentStateException 
	 */
	MatchSet findMatchProfile(ProfileQuery q) throws UnknownFilterException, IncoherentStateException;

	/**
	 * Finds matches for a given individual already known to the kb
	 * 
	 * @param q
	 * @param alpha - pvalue cutoff
	 * @return scored matches
	 * @throws UnknownFilterException 
	 * @throws IncoherentStateException 
	 */
	MatchSet findMatchProfile(ProfileQuery q, double alpha) throws UnknownFilterException, IncoherentStateException;

	/**
	 * 
	 * 
	 * @param individualId
	 * @return scored matches
	 * @throws UnknownFilterException
	 * @throws IncoherentStateException 
	 */
	public MatchSet findMatchProfile(String individualId) throws UnknownFilterException, IncoherentStateException;

	
	/**
	 * @return ontology interface
	 */
	BMKnowledgeBase getKnowledgeBase();



}
