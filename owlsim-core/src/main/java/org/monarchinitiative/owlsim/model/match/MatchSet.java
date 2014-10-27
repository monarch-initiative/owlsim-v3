package org.monarchinitiative.owlsim.model.match;

import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

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
	public ProfileQuery getQuery();
	
	/**
	 * @return all matches
	 */
	public List<Match> getMatches();

	/**
	 * @return all matches with rank
	 */
	public List<Match> getMatchesWithRank(int rank);
	
	/**
	 * @param matchId
	 * @return Match with identical matchId
	 */
	public Match getMatchesWithId(String matchId);
	
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

	public void truncate(int limit);
		
	public DescriptiveStatistics getScores();

	/**
	 * @param background - distribution of scores to compare for significance
	 */
	public void calculateMatchSignificance(DescriptiveStatistics background);

	// TODO - filters
	
}
