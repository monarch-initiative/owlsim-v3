package org.monarchinitiative.owlsim.model.match;

import java.util.List;
import java.util.Map;

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
     * @param profileQuery a profile query
     */
    public void setQuery(ProfileQuery query);
	
	/**
	 * @return all matches
	 */
	public List<Match> getMatches();
	
	/**
     * @param matches List of matches
     */
    public void setMatches(List<Match> matches);

	/**
	 * @return all matches with rank
	 */
	public List<Match> getMatchesWithRank(int rank);
	
	/**
	 * @param rank
	 * @return all matches <= rank
	 */
	public List<Match> getMatchesWithOrBelowRank(int rank);
	
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

	public MethodMetadata getMethodMetadata();
	public void setMethodMetadata(MethodMetadata methodMetadata);

	/**
	 * Truncate the match set to the top n hits
	 * 
	 * @param limit
	 */
	public void truncate(int limit);
	
	/**
	 * Matches for all reference individuals
	 * 
	 * This is preserved even after truncation
	 * 
	 * @return map between match id and match
	 */
	public Map<String, Match> getReferenceMatches();
		
	public DescriptiveStatistics getScores();

	/**
	 * @param background - distribution of scores to compare for significance
	 */
	public void calculateMatchSignificance(DescriptiveStatistics background);

	// TODO - filters
	
}
