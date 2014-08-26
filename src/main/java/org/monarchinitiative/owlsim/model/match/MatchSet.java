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

	/**
	 * @return all matches with rank
	 */
	public List<Match> getMatchesWithRank(int rank);

	// TODO - iterator
	
	/**
	 * @param createMatch
	 */
	public void add(Match match);

	/**
	 * sort all matches, best match first
	 */
	public void sortMatches();
	
	/**
	 * rank sorted matches, best has rank=1, 
	 * identical scores share ranks
	 */
	public void rankMatches();
	
	// TODO - record kb metadata
	public ExecutionMetadata getExecutionMetadata();
	public void setExecutionMetadata(ExecutionMetadata executionMetadata);
	

	// TODO - filters
	
}
