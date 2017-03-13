package org.monarchinitiative.owlsim.compute.classmatch;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.monarchinitiative.owlsim.compute.mica.impl.NoRootException;
import org.monarchinitiative.owlsim.io.OwlKnowledgeBase;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class ClassMatcherTest {

    protected BMKnowledgeBase kb;
    protected ClassMatcher classMatcher;
    private Logger LOG = Logger.getLogger(ClassMatcherTest.class);

    protected void load(String fn, String... ontfns) throws OWLOntologyCreationException, URISyntaxException, NoRootException {
//        OWLLoader loader = new OWLLoader();
//        LOG.info("Loading: "+fn);
//        loader.load(IRI.create(Resources.getResource(fn)));
//        for (String ontfn : ontfns) {
//            URL res = getClass().getResource(ontfn);
//            LOG.info("RES="+res);
//            loader.ontologies(res.getFile());
//        }
//        kb = loader.createKnowledgeBaseInterface();
        kb = OwlKnowledgeBase.loader()
                .loadOntology(filePath(fn))
                .loadOntologies(Arrays.stream(ontfns).map(ontfn -> filePath(ontfn)).collect(Collectors.toList()))
                .createKnowledgeBase();
        classMatcher = new ClassMatcher(kb);
    }

    private String filePath(String filename) {
        return Paths.get("src/test/resources/", filename).toString();
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
