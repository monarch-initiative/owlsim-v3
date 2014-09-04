/**
 * Framework for computing matches between a query profile and a knowledge base.
 * 
 * 
 * 
 * The following are some key classes
 * <ul>
 *  <li> {@link org.monarchinitiative.owlsim.kb.BMKnowledgeBase} - representation of background knowledge
 *  <li> {@link org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher} - matches query profile against target profiles in KB
 *  <li> {@link org.monarchinitiative.owlsim.eval.ProfileMatchEvaluator} - engine for evaluating ProfileMatchers
 *
 * </ul>
 * 
 * <h2>Running owlsim</h2>
 * 
 * Currently there is no CLI and no Service Layer. Writing the service layer (Jersey/JAX-RS) will be the next step.
 * 
 * For now, examples are run from src/test - either junit tests or integration tests
 * 
 * <h2>Formats</h2>
 * 
 * Currently the only input format expected is RDF/OWL - see {@link org.monarchinitiative.owlsim.io.OWLLoader} - both for ontologies and for associations.
 * M/br>
 * The only output format supported is json. See {@link org.monarchinitiative.owlsim.io.JSONWriter}
 * 
 */
package org.monarchinitiative.owlsim;