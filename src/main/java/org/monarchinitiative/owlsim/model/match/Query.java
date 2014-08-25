package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.impl.QueryImpl;

import com.google.inject.ImplementedBy;

/**
 * Represents a profile query which is a set of classes used to interrogate a knowledge base for the most
 * likely item
 * 
 * 
 * @author cjm
 *
 */
@ImplementedBy(QueryImpl.class)
public interface Query {
	
	/**
	 * @return all (positive) class Ids in query
	 */
	public Set<String> getQueryClassIds();
}
