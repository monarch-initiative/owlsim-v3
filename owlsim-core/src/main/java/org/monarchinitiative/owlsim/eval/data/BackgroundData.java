package org.monarchinitiative.owlsim.eval.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.googlecode.javaewah.EWAHCompressedBitmap;


public class BackgroundData {

	private static Logger LOG = Logger.getLogger(BackgroundData.class);

	int maxLength = 20;
	Random rand = new Random(); 
	BMKnowledgeBase knowledgeBase;

	public BackgroundData(BMKnowledgeBase knowledgeBase) {
		this.knowledgeBase = knowledgeBase;
	}
	
	public void setMaxLength(int n) {
		this.maxLength = n;
	}
	
	public int getMaxLength() {
		return this.maxLength;
	}
	
	public void setMaxLengthFromInstances() {
		int m = 0;
		Set<String> individuals = knowledgeBase.getIndividualIdsInSignature();

		for (String iid : individuals) {
			int x=0;
			x = knowledgeBase.getDirectTypesBM(iid).cardinality();
			m = Math.max(m, x);
		}
		LOG.info("Set max length from instances: "+m);
		this.maxLength = m;
	}

	/**
	 * Will create {@code n} {@link EWAHCompressedBitmap}s to represent
	 * individuals with a random set of classes.  A random quantity of 
	 * classes will be added to each bitmap, maximally determined by the number
	 * of classes of the individuals in the knowledgeBase.  
	 * @param n Number of sets to create
	 * @return
	 */
	public EWAHCompressedBitmap[] createRandom(int n) {
		EWAHCompressedBitmap[] newbms = new EWAHCompressedBitmap[n];
		int max = knowledgeBase.getIndividualCountPerClassArray().length;
		for (int i=0; i<n; i++) {
			EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
			int setLength = rand.nextInt(this.maxLength) + 1; 
			Set<Integer> ibits = new HashSet<Integer>();
			for (int j=0; j<setLength; j++) {
				ibits.add(rand.nextInt(max));
			}
			bm = EWAHUtils.converIndexSetToBitmap(ibits);
//			LOG.info("Created random set with length "+setLength);
			newbms[i] = bm;
		}		
		return newbms;
	}
	
	/**
	 * Will create {@code n} {@link EWAHCompressedBitmap}s to represent
	 * individuals associated with a random set of classes.  A random quantity of 
	 * classes will be added to each bitmap, maximally determined by the number
	 * of classes of the individuals in the knowledgeBase.  
	 * This will leverage only the classes that are actually
	 * associated with the instances, and at the same frequency
	 * that they are observed, in the knowledgeBase (as opposed to random).
	 * @param n
	 * @return
	 */
	public EWAHCompressedBitmap[] createRandomWeightedByInstances(int n) {
		EWAHCompressedBitmap[] newbms = new EWAHCompressedBitmap[n];

		//not sure if this is direct or not?
		//first, make a list that represents all the classes (in bits) used
		//from the individuals in the knowledgeBase, expanded
		//based on the frequency that they are utilized
		int[] classCount = knowledgeBase.getIndividualCountPerClassArray();
		ArrayList<Integer> allClasses = new ArrayList<Integer>();
		for (int i=0; i<classCount.length; i++) {
			for (int j=0; j<classCount[i]; j++) {
				allClasses.add(i);
			}
		}
		
		//make bitmaps, randomly drawing only from those classes
		//that are utilized
		Collections.shuffle(allClasses);		
		int max = allClasses.size();
		for (int i=0; i<n; i++) {
			EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
			int setLength = rand.nextInt(this.maxLength) + 1; 
			Set<Integer> ibits = new HashSet<Integer>();
			for (int j=0; j<setLength; j++) {
				ibits.add(allClasses.get(rand.nextInt(max)));
			}
			bm = EWAHUtils.converIndexSetToBitmap(ibits);
//			LOG.info("Created random set with length "+setLength);
			newbms[i] = bm;
		}				
		return newbms;
	}
	
}
