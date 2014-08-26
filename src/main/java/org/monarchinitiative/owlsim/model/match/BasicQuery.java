package org.monarchinitiative.owlsim.model.match;

import java.util.Set;

import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;

import com.google.inject.ImplementedBy;

/**
 * Represents a profile query which is a set of classes used to interrogate a knowledge base for the most
 * likely item
 * 
 * 
 * @author cjm
 *
 */
@ImplementedBy(BasicQueryImpl.class)
public interface BasicQuery {
	
	/**
	 * @return all (positive) class Ids in query
	 */
	public Set<String> getQueryClassIds();
}
