package org.monarchinitiative.owlsim.compute.kb.impl;

import java.net.URISyntaxException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

/**
 * Tests a OWLAPI implementation of a KB
 * 
 * Also tests:
 *  - LabelMapper
 *  
 *
 */
public  class EquivalentClassesOwlTest extends AbstractOwlTest {

	private Logger LOG = Logger.getLogger(EquivalentClassesOwlTest.class);
	
	boolean isReasonerSupportsSameAs = false;

	@Test
	public void basicTest() throws OWLOntologyCreationException, URISyntaxException, NonUniqueLabelException {
		load("equivalentClassesTest.owl");

		LOG.info("SIG="+kb.getClassIdsInSignature());
		String x1 ="http://x.org/x1";
		String y1 ="http://x.org/y1";
		
		Assert.assertEquals(kb.getSuperClassesBM(x1), kb.getSuperClassesBM(y1));
		Assert.assertEquals(kb.getDirectSuperClassesBM(x1), kb.getDirectSuperClassesBM(y1));
		
		// note that superclasses includes owl:Thing, and also self.
		// we actually have a total of 2, not 3 supers.
		// this is because equivalence sets are treated as the same node
		check(kb.getSuperClassesBM(x1), 2);
		
		// DIRECT PLUS INDIRECT
		// superclasses is reflexive as well as transitiive.
		// Both self and equivalents are included
		checkContains(kb.getSuperClassesBM(x1), y1);
		checkContains(kb.getSuperClassesBM(x1), x1); // effectively the same as previous check

		checkContains(kb.getSuperClassesBM(y1), x1);
		checkContains(kb.getSuperClassesBM(y1), y1); // effectively the same as previous check
		
		// DIRECT
		check(kb.getDirectSuperClassesBM(x1), 1);
		checkNotContains(kb.getDirectSuperClassesBM(x1), y1);
		checkNotContains(kb.getDirectSuperClassesBM(x1), x1); // effectively same as previous
		checkNotContains(kb.getDirectSuperClassesBM(y1), x1);
		checkNotContains(kb.getDirectSuperClassesBM(y1), y1); // effectively same as previous
		
		// Note: default reasoner is currently Elk, which does
		// not reason over sameAs
		String i1 = "http://x.org/i1";	
		check(kb.getTypesBM(i1), 2); // {Thing, x1=y1}
		check(kb.getDirectTypesBM(i1), 1); // {x1=y1}
		checkContains(kb.getTypesBM(i1), x1);
		checkContains(kb.getTypesBM(i1), y1);

		// NOTE: class indexes are guaranteed to be ordered with most frequent last
		int[] freqs = kb.getIndividualCountPerClassArray();
		Assert.assertEquals(2, freqs.length);
		if (isReasonerSupportsSameAs) {
			// i1=i2 is treated a SINGLE node
			Assert.assertEquals(1, freqs[0]); // 0 : x1=y1, which has as members i1=i2
			Assert.assertEquals(2, freqs[1]); // 1 : Thing, which has as members i1=i2, i0			
		}
		else {
			// i1,i2 is treated a DISTINCT nodes
			Assert.assertEquals(1, freqs[0]); // 0 : x1=y1, which has as members i1
			Assert.assertEquals(3, freqs[1]); // 1 : Thing, which has as members i1, i2, i0
		}
		
		if (isReasonerSupportsSameAs) {
			String i2 = "http://x.org/i2";	
			check(kb.getTypesBM(i2), 2); // {Thing, x1=y1}
			check(kb.getDirectTypesBM(i2), 1); // {x1=y1}
			checkContains(kb.getTypesBM(i2), x1);
			checkContains(kb.getTypesBM(i2), y1);
		}

		String i0 = "http://x.org/i0";	
		check(kb.getTypesBM(i0), 1); // {Thing}
		checkNotContains(kb.getTypesBM(i0), x1);
		checkNotContains(kb.getTypesBM(i0), y1);

	}
	

	




}
