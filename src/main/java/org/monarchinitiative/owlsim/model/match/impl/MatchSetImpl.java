package org.monarchinitiative.owlsim.model.match.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.Query;

/**
 * A collection of matches for a single query profile against a set of
 * individuals
 * 
 * @author cjm
 *
 */
public class MatchSetImpl implements MatchSet {
	private Query query;
	private List<Match> matches; // TODO - make this neutral

	/**
	 * constructor
	 */
	public MatchSetImpl() {
		super();
		matches = new ArrayList<Match>();
	}
	
	/**
	 * constructor
	 */
	public MatchSetImpl(Query query) {
		super();
		this.query = query;
		matches = new ArrayList<Match>();
	}
	
	public static MatchSet create(Query query) {
		return new MatchSetImpl(query);
	}
	

	public Query getQuery() {
		return query;
	}



	public void setQuery(Query query) {
		this.query = query;
	}



	/**
	 * @return matches
	 */
	public List<Match> getMatches() {
		return matches;
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
	}
	
	private static Comparator<Match> MatchComparator = 
			new Comparator<Match>() {
		public int compare(Match c1, Match c2) {
			if (c1.getScore() < c2.getScore()) return 1;
			if (c1.getScore() > c2.getScore()) return -1;
			return 0;
		}  
	};

	


	
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
