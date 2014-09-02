/**
 * A Knowledge Base interface for accessing an ontology of features used to describe entities.
 * 
 * Here a KB is conceived of
 * a Directed Acyclic Graph (DAG) of 'classes' (attributes) and 'individuals' (elements or items),
 * with the latter associated with the former, possible with some frequency information,
 * and/or negation
 * 
 * The KB makes use of {@link com.googlecode.javaewah.EWAHCompressedBitmap} as a means of
 * representing sets of features or entities. These can be mapped back and forth between
 * domain objects
 * 
 * @author cjm
 *
 */
package org.monarchinitiative.owlsim.kb;