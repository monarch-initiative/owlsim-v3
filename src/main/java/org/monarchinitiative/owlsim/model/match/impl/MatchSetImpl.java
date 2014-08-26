package org.monarchinitiative.owlsim.model.match.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.monarchinitiative.owlsim.model.match.ExecutionMetadata;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;

/**
 * A collection of matches for a single query profile against a set of
 * individuals
 * 
 * @author cjm
 *
 */
public class MatchSetImpl implements MatchSet {
	private BasicQuery query;
	private List<Match> matches; // TODO - make this neutral
	ExecutionMetadata executionMetadata;
	private boolean isSorted;
	
	/**
	 * constructor
	 */
	public MatchSetImpl(BasicQuery query) {
		super();
		isSorted = false;
		this.query = query;
		matches = new ArrayList<Match>();
	}
	
	public static MatchSet create(BasicQuery query) {
		return new MatchSetImpl(query);
	}
	

	public BasicQuery getQuery() {
		return query;
	}



	public void setQuery(BasicQuery query) {
		this.query = query;
	}

	public ExecutionMetadata getExecutionMetadata() {
		return executionMetadata;
	}

	public void setExecutionMetadata(ExecutionMetadata executionMetadata) {
		this.executionMetadata = executionMetadata;
	}

	/**
	 * @return matches
	 */
	public List<Match> getMatches() {
		return matches;
	}

	/**
	 * assumes already sorted or ranked
	 * @return matches
	 */
	public List<Match> getMatchesWithRank(int rank) {
		List<Match> ms = new ArrayList<Match>();
		for (Match m : matches) {
			if (m.getRank() == rank)
				ms.add(m);
		}
		return ms;
	}

	/**
	 * @param matches
	 */
	public void setMatches(List<Match> matches) {
		this.matches = matches;
	}
	
	
	@Override
	public void add(Match match) {
		this.matches.add(match);
	}

	/**
	 * Sorts the matches by probability, highest probability first
	 */
	public void sortMatches() {
		Collections.sort(matches, MatchComparator);
		isSorted = true;
		rankMatches();
	}
	
	private static Comparator<Match> MatchComparator = 
			new Comparator<Match>() {
		public int compare(Match c1, Match c2) {
			if (c1.getScore() < c2.getScore()) return 1;
			if (c1.getScore() > c2.getScore()) return -1;
			return 0;
		}  
	};

	public void rankMatches() {
		if (!isSorted)
			sortMatches();
		int rank = 0;
		Double lastScore = null;
		for (Match m : matches) {
			double s = m.getScore();
			// TODO - avoid double equality test
			if (lastScore == null ||
					s != lastScore) {
				rank++;
			}
			m.setRank(rank);
			lastScore = s;
					
		}
	}
	


	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < matches.size()) {
			sb.append(matches.get(i).toString());
			sb.append("\n");
			i++;
			if (i > 8) {
				sb.append("...truncating "+matches.size()+" results\n");
				break;
			}
			
		}
		return sb.toString();
	}

}
