/**
 * Computation of Most Informative Common Ancestors (MICAs)
 * <br/>
 * Definitions:
 * <pre>
 * CA(a,b) = { x | x &in; Anc*(a), x &in; Anc*(b) }
 * MICA(a,b) = { x | x &in; MICA(a,b), not exists y, y &in; MICA(a,b), IC(y) > IC(x) }
 * </pre>
 * 
 * 
 * @author cjm
 *
 */
package org.monarchinitiative.owlsim.compute.mica;