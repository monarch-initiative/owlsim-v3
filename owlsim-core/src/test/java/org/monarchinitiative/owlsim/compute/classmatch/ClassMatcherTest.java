package org.monarchinitiative.owlsim.compute.classmatch;

import static org.junit.Assert.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.AbstractMICAStoreTest;
import org.monarchinitiative.owlsim.compute.mica.MICAStore;
import org.monarchinitiative.owlsim.compute.mica.impl.MICAStoreImpl;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.compute.stats.KBStatsCalculator;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.monitoring.runtime.instrumentation.common.com.google.common.io.Resources;

public class ClassMatcherTest {

    protected BMKnowledgeBase kb;
    protected ClassMatcher classMatcher;
    private Logger LOG = Logger.getLogger(ClassMatcherTest.class);

    protected void load(String fn, String... ontfns) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
        OWLLoader loader = new OWLLoader();
        LOG.info("Loading: "+fn);
        loader.load(IRI.create(Resources.getResource(fn)));
        for (String ontfn : ontfns) {
            URL res = getClass().getResource(ontfn);
            LOG.info("RES="+res);
            loader.loadOntologies(res.getFile());
        }
        kb = loader.createKnowledgeBaseInterface();
        classMatcher = new ClassMatcher(kb);
    }
    
    @Test
    public void selfTest() throws OWLOntologyCreationException, URISyntaxException, NoRootException {
        load("mp-subset.ttl");
        LabelMapper lm = kb.getLabelMapper();
        
        List<SimpleClassMatch> matches = classMatcher.matchOntologies("MP", "MP");
        
        int numNonSelfMatches = 0;
        for (SimpleClassMatch m : matches) {
            if (!m.getQueryClassId().equals(m.getMatchClassId())) {
                numNonSelfMatches++;
            }
        }
        assertEquals(0, numNonSelfMatches);
    }

}
