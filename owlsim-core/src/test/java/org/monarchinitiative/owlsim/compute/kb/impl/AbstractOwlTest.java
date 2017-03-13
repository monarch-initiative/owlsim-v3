package org.monarchinitiative.owlsim.compute.kb.impl;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.monarchinitiative.owlsim.io.OwlKnowledgeBase;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Tests a OWLAPI implementation of a KB
 * 
 * Also tests:
 *  - LabelMapper
 *  
 *
 */
public class AbstractOwlTest {

	private Logger LOG = Logger.getLogger(AbstractOwlTest.class);

	protected BMKnowledgeBase kb;

	protected void load(String fn) throws OWLOntologyCreationException, URISyntaxException {
//		OWLLoader loader = new OWLLoader();
//		loader.load(IRI.create(Resources.getResource(fn)));
//		kb = loader.createKnowledgeBaseInterface();
		kb = OwlKnowledgeBase.loader().loadOntology(Paths.get("src/test/resources", fn).toString()).createKnowledgeBase();
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
