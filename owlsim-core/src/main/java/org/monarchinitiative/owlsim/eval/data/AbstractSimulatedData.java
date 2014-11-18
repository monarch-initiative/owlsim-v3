package org.monarchinitiative.owlsim.eval.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;


public abstract class AbstractSimulatedData implements SimulatedData {

	private static Logger LOG = Logger.getLogger(AbstractSimulatedData.class);

	private BMKnowledgeBase knowledgeBase;
	private Boolean recursive;


	/**
	 * @param g
	 * @param owlsim
	 */
	public AbstractSimulatedData(BMKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
		this.recursive = false;
	}

	public void setRecursive(Boolean r) {
		this.recursive = r;
	}
	
	public Map<Integer,EWAHCompressedBitmap[]> createAssociations() throws Exception {
		return createAssociations(knowledgeBase.getIndividualIdsInSignature());
	}

	public Map<Integer,EWAHCompressedBitmap[]> createAssociations(Set<String> individualIds) throws Exception {
		LOG.info("Creating simulated data from "+individualIds.size()+" individuals. Recursive: "+recursive.toString());

		Map<Integer,EWAHCompressedBitmap[]> bms = new HashMap<Integer,EWAHCompressedBitmap[]>();
		//iterate through all of the individuals, and make a simulated
		//set for each
		for (String iid : individualIds) {
			int ibit = knowledgeBase.getIndividualIndex(iid);
			EWAHCompressedBitmap cids = knowledgeBase.getDirectTypesBM(iid);
			LOG.info("Creating simulated data for "+iid+" with "+cids.cardinality()+" attributes: "+cids.getPositions().toString());
			bms.put(ibit,createAttributeSets(cids));
		}
		return bms;
	}		
		
	public int choose(int n, int k) {
		if (k == 0) return 1;
		return (n * choose(n - 1, k - 1)) / k;
	}
	
	public EWAHCompressedBitmap[] selectNSubsets(EWAHCompressedBitmap[] attributeSets, int n) {
		EWAHCompressedBitmap[] newbm = new EWAHCompressedBitmap[n];
		Random rand = new Random();
		for (int i=0; i<n; i++) {
			newbm[i] = attributeSets[rand.nextInt(attributeSets.length)];
		}
		return newbm;
	}
	
	public BMKnowledgeBase getKnowledgeBase() {
		return knowledgeBase;
	}
		
}
