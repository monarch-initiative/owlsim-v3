package org.monarchinitiative.owlsim.eval.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.google.common.collect.Lists;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * <p>This will choose a random set of N elements from the set of attributes.</p>
 * <ul><li>If no {@code setLength} is provided, it will default to 3.  </li>
 * <li>If the {@code setLength > atts.cardinality()} of the set, it will return a 
 * set of length {@code atts.size()-1}. </li>
 * <li>If no {@code numSets} is provided, it will default to 1.  Otherwise, 
 * it will return as many unique sets as possible <= numSets.</li>
 * </ul>
 * There is no recursive option, for now.
 * @author nlw
 *
 */
public class RandomChooseNSimulatedData extends AbstractSimulatedData {

	private static Logger LOG = Logger.getLogger(RandomChooseNSimulatedData.class);
	private int numSets = 1;
	private int setLength = 3;

	public RandomChooseNSimulatedData(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}
	
	public void setNumSets(int n) {
		this.numSets = n;
	}
	
	public int getNumSets() {
		return this.numSets;
	}

	public void setSetLength(int n) {
		this.setLength = n;
	}
	
	public int getSetLength() {
		return this.setLength;
	}

	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts, int setLength, int numSets)
			throws Exception {
		this.setLength = setLength;
		this.numSets = numSets;

		return createAttributeSets(atts);
	}

	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts)
			throws Exception {
		
		return makeSetofRandomSubsets(atts);
	}


	/**
	 * If {@code setLength} > {@code atts.size()} , it will generate random sets of length {@code atts.size()-1}.
	 * @param atts
	 * @param setLength
	 * @param numSets
	 * @return
	 */
	private EWAHCompressedBitmap[] makeSetofRandomSubsets(EWAHCompressedBitmap atts) {
		Set<EWAHCompressedBitmap> sets = new HashSet<EWAHCompressedBitmap>();
		int setLength = this.setLength;
		int numSets = this.numSets;
		int subsetCounter = 0;
		int subsetTries = 0; //to cut off the loop
		
		if (setLength >= atts.cardinality()) {
			LOG.info("You've requested a random set to be the >= size as the starting set ("+setLength+">"+atts.cardinality()+"). Resetting to n-1.");
			setLength = atts.cardinality()-1;
		}
		
		//the number of random sets cannot be > n choose k, so reset it to that
		int maxNumSets = choose(atts.cardinality(),setLength);
		if (numSets > maxNumSets) {
			LOG.info("You've requested more sets ("+numSets+") than is possible for choose("+atts.cardinality()+","+setLength+"). You will get the maximum of "+maxNumSets+".");
			numSets = maxNumSets;
		}
		
		//doing this in a for loop here is slightly more efficient then having to remake the list every time
		//also, because we might generate the same set more than once, we will
		//do this in a while loop instead of for loop
		while ((subsetCounter < numSets) ) { //&& (subsetTries < 7*maxNumSets)
			Set<Integer> randomSet = makeRandomSubset(atts, setLength);
			sets.add(EWAHUtils.convertIndexSetToBitmap(randomSet));
//			LOG.info("Set size="+sets.size());
			subsetCounter++;
		}
		return sets.toArray(new EWAHCompressedBitmap[sets.size()]);
	}

	/**
	 * Will generate a random set of attributes of length 
	 * as defined in this object.
	 * @param atts
	 * @return
	 */
	public Set<Integer> makeRandomSubset(Set<Integer> atts) {
		return makeRandomSubset(atts, this.setLength);
	}
	
	public Set<Integer> makeRandomSubset(EWAHCompressedBitmap bm) {
		return makeRandomSubset(bm, this.setLength);
	}
	
	/**
	 * Will generate a random set of attributes of length {@code setLength}.
	 * If {@code setLength} >= {@code atts.size()} , it will generate a random 
	 * set of length {@code setLength-1}.
	 * @param atts
	 * @param setLength
	 * @return
	 */
	public Set<Integer> makeRandomSubset(EWAHCompressedBitmap bm, int setLength) {
		List<Integer> atts = bm.getPositions();
		return makeRandomSubset(atts, setLength);		
	}

	public Set<Integer> makeRandomSubset(Set<Integer> atts, int setLength) {
		return makeRandomSubset(Lists.newArrayList(atts), setLength);
	}
	
	public Set<Integer> makeRandomSubset(List<Integer> atts, int setLength) {
		Collections.shuffle(atts);
		if (setLength >= atts.size()) {
			LOG.info("You've requested a random set to be the >= size as the starting set ("+setLength+">"+atts.size()+"). Resetting to n-1.");
			setLength = atts.size()-1;
		}
		Set<Integer> randomSet = new HashSet<Integer>(atts.subList(0, setLength));
		return randomSet;		
	}

}
