package org.monarchinitiative.owlsim.eval;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;

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
