package org.monarchinitiative.owlsim.model.match;

import java.util.List;

/**
 * Represents all matches for a query against a set of individuals
 * 
 * @author cjm
 *
 */
public interface MatchSet {

	/**
	 * @return query that produced the match set
	 */
	public BasicQuery getQuery();
	
	/**
	 * @return all matches
	 */
	public List<Match> getMatches();

	// TODO - iterator
	
	/**
	 * @param createMatch
	 */
	public void add(Match match);

	/**
	 * sort all matches, best match first
	 */
	public void sortMatches();
	
	// TODO - record kb metadata
	public ExecutionMetadata getExecutionMetadata();
	public void setExecutionMetadata(ExecutionMetadata executionMetadata);
	

	// TODO - filters
	
}
