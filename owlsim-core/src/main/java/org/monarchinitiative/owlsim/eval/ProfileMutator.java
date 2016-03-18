package org.monarchinitiative.owlsim.eval;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.io.JSONWriter;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.ProfileQueryImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Permutes a profile
 * 
 * @author cjm
 *
 */
public class ProfileMutator {

	private Logger LOG = Logger.getLogger(ProfileMutator.class);
	private boolean writeToStdout = false;
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

	private int getRandomPosition(EWAHCompressedBitmap bm) {
		List<Integer> ixs = bm.getPositions();
		if (ixs.size() == 0) {
			LOG.error("bm is empty!");
		}
		return ixs.get((int) (Math.random() * ixs.size()));
	}

	public ProfileQuery addBranch(BMKnowledgeBase kb, ProfileQuery q, double prAdd) {
		Set<String> qcids = q.getQueryClassIds();
		int N = qcids.size();
		// pick a random annotated node
		if (q.getQueryClassIds().size() == 0) {
			return q;
		}
		int aix = getRandomPosition(kb.getSuperClassesBM(qcids));

		// pick a random subclass of it
		int cix = getRandomPosition(kb.getSubClasses(aix));

		for (int i=0; i<N; i++) {
			if (Math.random() < prAdd) {
				int ix = getRandomPosition(kb.getSubClasses(cix));
				qcids.add(kb.getClassId(ix));
			}
		}
		return ProfileQueryImpl.create(new HashSet<String>(qcids));
	}

	public ProfileQuery removeBranch(BMKnowledgeBase kb, ProfileQuery q, double pr) {
		List<String> qcids = new ArrayList<String>(q.getQueryClassIds());
		int n = qcids.size();
		if (n==0) {
			LOG.warn("EMPTY");
			return q;
		}
		int x = (int) (Math.random() * n);
		String xid = qcids.get(x);
		EWAHCompressedBitmap parentsBM = kb.getSuperClassesBM(xid);
		List<Integer> pixs = parentsBM.getPositions();
		int rmix = pixs.get((int) (Math.random() * pixs.size()));
		Set<String> rmids = new HashSet<String>();
		for (String qcid : qcids) {
			if (kb.getSuperClassesBM(qcid).getPositions().contains(rmix)) {
				if (Math.random() < pr * ((n - rmids.size())/(double)n) ) {
					rmids.add(qcid);
					LOG.info("REMOVED: "+qcid);
				}
			}
		}
		qcids.removeAll(rmids);
		return ProfileQueryImpl.create(new HashSet<String>(qcids));
	}


}
