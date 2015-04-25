package org.monarchinitiative.owlsim.eval;

import com.googlecode.javaewah.EWAHCompressedBitmap;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.*;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;
import org.monarchinitiative.owlsim.model.match.impl.QueryWithNegationImpl;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Permutes a profie
 * 
 * @author cjm
 *
 */
public class ProfileMutator {
	
	private Logger LOG = Logger.getLogger(ProfileMutator.class);
	private boolean writeToStdout = true;
	private JSONWriter jsonWriter;
	
	public void writeJsonTo(String fileName) throws FileNotFoundException {
		jsonWriter = new JSONWriter(fileName);
	}
	
	public ProfileQuery removeMember(ProfileQuery q) {
		List<String> qcids = new ArrayList<String>(q.getQueryClassIds());
		int n = qcids.size();
		int x = (int) (Math.random() * n);
		qcids.remove(x);
		return ProfileQueryImpl.create(new HashSet<String>(qcids));
	}

	// TODO
	public ProfileQuery addMember(ProfileQuery q) {
		List<String> qcids = new ArrayList<String>(q.getQueryClassIds());
		int n = qcids.size();
		int x = (int) (Math.random() * n);
		qcids.remove(x);
		return ProfileQueryImpl.create(new HashSet<String>(qcids));
	}
	

}
