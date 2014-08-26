package org.monarchinitiative.owlsim.compute.kb.impl;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.impl.BasicQueryImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.Sets;
import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Tests a OWLAPI implementation of a KB
 * 
 * Also tests:
 *  - LabelMapper
 *  
 *
 */
public  class AbstractOwlTest {

	private Logger LOG = Logger.getLogger(AbstractOwlTest.class);

	protected BMKnowledgeBase kb;
	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException {
		OWLLoader loader = new OWLLoader();
		loader.load(IRI.create(Resources.getResource(fn)));
		kb = loader.createKnowledgeBaseInterface();
	}

	protected void checkContains(EWAHCompressedBitmap bm,
			String c) throws NonUniqueLabelException {
		
		for (int ix : bm.getPositions()) {
			Set<String> cids = kb.getClassIds(ix);
			if (cids.contains(c)) {
				Assert.assertTrue(true);
				return;
			}
		}
		Assert.assertTrue("Expected: "+c, false);
	}
	
	protected void checkNotContains(EWAHCompressedBitmap bm,
			String c) throws NonUniqueLabelException {
		
		for (int ix : bm.getPositions()) {
			Set<String> cids = kb.getClassIds(ix);
			if (cids.contains(c)) {
				Assert.assertFalse(true);
				return;
			}
		}
		Assert.assertFalse("Expected: NOT "+c, false);
	}

	protected void checkContainsLabel(EWAHCompressedBitmap bm,
			String a) throws NonUniqueLabelException {
		checkContains(bm, kb.getLabelMapper().lookupByUniqueLabel(a));
	}

	protected void check(EWAHCompressedBitmap superClassesBM, int exp) {
		for (int ix : superClassesBM.getPositions()) {
			String sid = kb.getClassId(ix);
			LOG.info(sid);
		}
		Assert.assertEquals(exp, superClassesBM.getPositions().size());

	}


}
