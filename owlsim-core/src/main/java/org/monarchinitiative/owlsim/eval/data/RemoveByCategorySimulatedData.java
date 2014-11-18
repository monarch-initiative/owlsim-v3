package org.monarchinitiative.owlsim.eval.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.stats.ICStatsCalculator;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * <p>This will return a set containing one attribute set with
 * all attributes that are equal to or subclasses of {@code category}
 * removed.</p>
 * <p>If {@code recursive = true}, then the non-subclasses will
 * be combined with the annotation PowerSet of the classes to be removed.</p>
 * @author nlw
 */
public class RemoveByCategorySimulatedData extends AbstractSimulatedData {

	private static Logger LOG = Logger.getLogger(RemoveByCategorySimulatedData.class);

	private Integer categoryBit = null;
	private Boolean inverse = false;
	private Set<Integer> inverseCategoryBits = new HashSet<Integer>();
	private double sumICthreshold = -1;
	private Boolean includeUnchanged = false;
	private int powerSetThreshold = 1000;
	private Boolean inclusive = true;
	
	
	public RemoveByCategorySimulatedData(BMKnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}

	public void setCategory(int cbit) {
		this.categoryBit = cbit;
	}

	public void setCategory(String category) {
		int cbit = -1;
		try {
			cbit = this.getKnowledgeBase().getClassIndex(category);
		} catch (Exception e) {
			if (e.getClass() == NullPointerException.class) {
				LOG.error("Class "+category+" does not exist");
			}
		}
		this.categoryBit = cbit;
	}
	
	public Integer getCategory() {
		return this.categoryBit;
	}
	
	/**
	 * Set this to true if you want to remove attributes not
	 * under the set category.  By default the {@link setSumICThreshold}
	 * will be set to the sumIC of the set where the category (not the inverse)
	 * is dropped.  You will also need to {@link setInverseCategories}.
	 * @param flag
	 */
	public void setInverse(Boolean flag) {
		this.inverse = flag;
	}
	
	/**
	 * The list of categories to remove, which is the inverted set
	 * of the supplied category
	 * @param cbits
	 */
	public void setInverseCategories(Set<Integer> cbits) {
			this.inverseCategoryBits.addAll(cbits);
	}
/*
	public void setInverseCategories(Set<String> categories) {
		for (String c : categories) {
			int cbit = this.getKnowledgeBase().getClassIndex(c);
			this.inverseCategoryBits.add(cbit);
		}
	}
*/
	
	/**
	 * Set this if you want to use a sumIC score as a cutoff threshold 
	 * when removing attributes.  By default a threshold will not be used.
	 * @param score
	 */
	public void setSumICThreshold(double score) {
		sumICthreshold = score;
	}
	
	/**
	 * Set the maximum number of power sets to return.
	 * threshold defaults to 1000.
	 * @param n
	 */
	public void setPowerSetThreshold(int n) {
		this.powerSetThreshold = n;
	}
	
	/**
	 * Set this to {@code true} if you want to return
	 * derived data sets that are equivalent to the
	 * starting set. Set to {@code false} by default.
	 * @param flag
	 */
	public void setIncludeUnchanged(Boolean flag) {
		this.includeUnchanged = flag;
	}
	
	/**
	 * Set this to {@code true} if you want to remove
	 * subclasses including the specified category class.
	 * If you only want subclasses exclusive of the indicated
	 * category, then set this to {@false}.
	 * Set to {@code true} by default.  
	 * @param flag
	 */
	public void setInclusive(Boolean flag) {
		this.inclusive = flag;
	}
	
	@Override
	public EWAHCompressedBitmap[] createAttributeSets(EWAHCompressedBitmap atts)
			throws Exception {

		//check if category is set
		if ((this.categoryBit == null || this.categoryBit < 0) && (this.inverse==false)) {
			LOG.error("No Category Set.");
			throw new NoCategoryFoundException(null);
		}
		
		if (inverse && (this.inverseCategoryBits.size() == 0)) {
			LOG.error("No inverse categories set.");
			throw new NoCategoryFoundException(null);				
		}

		//to store the set of derived BMs
		Set<EWAHCompressedBitmap> newBMs = new HashSet<EWAHCompressedBitmap>();
		
		//If performance is poor with the fetching subclasses method, we can change it 
		//to iterating over the attributes, getting their superclasses, and seeing if they
		//include the category.
		EWAHCompressedBitmap bitsToRemove =  new EWAHCompressedBitmap();
		if (!this.inverse) {
			//figure out which attributes are subclasses of a given category
			EWAHCompressedBitmap subs = this.getKnowledgeBase().getSubClasses(categoryBit);
			LOG.info("Removing any subclasses of bit "+categoryBit+": "+subs.getPositions());

			//make sure that the category is removed from the list of subclasses when non-inclusive
			if (!this.inclusive) {
				List<Integer> spos = subs.getPositions();
				spos.remove(categoryBit);
				subs = EWAHUtils.convertSortedIndexListToBitmap(spos);
			}
			bitsToRemove = atts.and(subs);
						
		} else {
			//make a mega-subclass BM
			EWAHCompressedBitmap subs = new EWAHCompressedBitmap();
			for (int cbit : this.inverseCategoryBits) {
				EWAHCompressedBitmap theseSubs = this.getKnowledgeBase().getSubClasses(cbit);
				if (!this.inclusive) {
					List<Integer> spos = theseSubs.getPositions();
					spos.remove(categoryBit);
					theseSubs = EWAHUtils.convertSortedIndexListToBitmap(spos);
				}
				subs = subs.or(theseSubs);
			}
			bitsToRemove = atts.and(subs);
		}
		
		if (bitsToRemove.cardinality() == 0) {
			//no attributes fall under this category,
			//resulting sets would be identical to starting set
			LOG.info("No attributes removed from set.");
			if (includeUnchanged) {
				LOG.info("Returning original set.");
				newBMs.add(atts);
			}
			return newBMs.toArray(new EWAHCompressedBitmap[newBMs.size()]);
		}

		if (bitsToRemove.cardinality() == atts.cardinality()) {
			String category = this.getKnowledgeBase().getClassId(categoryBit);
			LOG.info("All attributes in this set are within the category "+category);
			return newBMs.toArray(new EWAHCompressedBitmap[newBMs.size()]);
		} else {
			LOG.info("Removing "+bitsToRemove.cardinality()+" attributes: "+	bitsToRemove.toString());
		}

		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();

		ICStatsCalculator calc = new ICStatsCalculator(this.getKnowledgeBase());
		
		//get the total information to remove, based on those atts to remove
		double sumIC = calc.getICStatsForAttributesByBM(atts).getSum();

		if (!this.inverse) {
			bm = atts.andNot(bitsToRemove);
			double finalSumIC= calc.getICStatsForAttributesByBM(bm).getSum();
			LOG.info("finalIC= "+(finalSumIC)+" removed |n|="+(atts.cardinality()-bm.cardinality()));

			//only want to add the new one if it has something in it
			//this ought to be caught before now.
			if (bm.cardinality() > 0) {
				newBMs.add(bm);
			}
		} else {
			//remove a random set of the attributes until the threshold is passed
			//because this is random, we want a distribution, make several
			//versions of it...say the number of elements possible to remove.
			Set<Integer> s = new HashSet<Integer>();
			double categorySumIC = calc.getICStatsForAttributesByBM(bitsToRemove).getSum();

			LOG.info("Removing sumIC="+(categorySumIC)+" information from "+sumIC+" in derived datasets.");
			//create at least 10 sets to get good stdevs on controls
			//even if they all end up being the same
			int setsToCreate = Math.max(10, bitsToRemove.cardinality());
			ArrayList<Integer> attsToRemoveList = Lists.newArrayList(bitsToRemove.getPositions());
			for (int i=0; i<setsToCreate; i++) {
				s = Sets.newHashSet(atts.getPositions());
				Collections.shuffle(attsToRemoveList);
				double ICtoRemove = categorySumIC;
				int j=0;
				while (ICtoRemove > 0 && s.size() > 1 && j < attsToRemoveList.size()) {
					Integer c = attsToRemoveList.get(j);
					s.remove(c);
					ICtoRemove -= calc.getInformationContentByClassIndex(c);
					j++;
				}
				//make BM from set
				bm = EWAHUtils.converIndexSetToBitmap(s);
				double finalSumIC= calc.getICStatsForAttributesByBM(bm).getSum();
				LOG.info("finalIC= "+(finalSumIC)+" targetIC="+(sumIC-categorySumIC)+" removed |n|="+(atts.cardinality()-bm.cardinality()));
				if (bm.cardinality() > 0) {
					newBMs.add(bm);
				}
			}
		}
		/*
		if (this.recursive && attsToRemove.size() > 1) {
			
			//make power set of all attributes to remove
			PowerSetSimulatedData data = new PowerSetSimulatedData(g, owlsim);
			Set<Set<OWLClass>> powerSet = data.createAttributeSets(attsToRemove);

			if (attsToRemove.size() < atts.size()) {
				//add the powerSets to combine with the others
				//TODO need to make random subset of the powersets when powerSet.length is long
				if (powerSet.size() > powerSetThreshold) {
					LOG.info("Too many combinations; capping at "+powerSetThreshold+".");
					powerSet = this.selectNSubsets(powerSet, powerSetThreshold);
				}
				//always need size of the comboSets < 30
				for (Set<OWLClass> ps : powerSet) {
					if (ps.size() > 0) {
						//don't add the empty sets, cartesianProduct can't handle that
						Set<OWLClass> ts = new HashSet<OWLClass>();
						ts.addAll(ps);
						ts.addAll(s);
						attSets.add(ts);
					}
				}
			} else {
				//make sure to add the one that removes all of the items in the 
				//category, even if getting combos.
				attSets.addAll(powerSet);
			}

		} */
		//powerSets always have an empty set, don't want it.
		//attSets.remove(new HashSet<OWLClass>());

		LOG.info("Found "+newBMs.size()+" sets.");

		//convert the set to array
		
		EWAHCompressedBitmap[] attSets = newBMs.toArray(new EWAHCompressedBitmap[newBMs.size()]);
		return attSets;
	}

	public class NoCategoryFoundException extends Exception {
		
		private static final long serialVersionUID = -33111278710470853L;
		private String category = null;
		
		public NoCategoryFoundException(String s) {
			this.category = s;
		}
		
		public String getMessage() {
			if (category == null) {
				return "No category specified";
			} else {
				return "No category found with id "+category;
			}
		}
	}

	
}
