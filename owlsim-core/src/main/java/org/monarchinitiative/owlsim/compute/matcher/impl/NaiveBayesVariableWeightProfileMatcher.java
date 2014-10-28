package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Given a query profile (a set of classes c1, .., cn) return a match profile, 
 * where each candidate individual is assigned a probability of being the match,
 * based on multiplying the probabilities of the set of all classes being on/off, given
 * the item is true.
 * 
 * TODO: this is INCOMPLETE
 * 
 * @author cjm
 *
 */
public class NaiveBayesVariableWeightProfileMatcher extends AbstractProfileMatcher implements ProfileMatcher {

	private Logger LOG = Logger.getLogger(NaiveBayesVariableWeightProfileMatcher.class);

	private double[][] likelihoods; // [label_j][feature_i] p( feature_i | label_j )
	private double[] priors; // p(label_j)

	/**
	 * @param kb
	 */
	@Inject
	public NaiveBayesVariableWeightProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
	}

	private int[] bmToVector(EWAHCompressedBitmap bm, int size) {
		int[] v = new int[size];
		for (int pos : bm.getPositions()) {
			v[pos] = 1;
		}
		return v;
	}

	@Override
	public String getShortName() {
		return "bayes-variable";
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		EWAHCompressedBitmap queryProfileBM = getProfileBM(q);
		int[] qvector = bmToVector(queryProfileBM, knowledgeBase.getNumClassNodes());


		//LOG.info("QUERY PROFILE for "+q+" "+queryProfileBM.getPositions());

		MatchSet mp =  MatchSetImpl.create(q);

		List<String> indIds = getFilteredIndividualIds(q.getFilter());

		double sumOfProbs = 0;
		double[] pvector = new double[indIds.size()];
		String[] indIdsVector = new String[indIds.size()]; 

		int localItemIndex = 0;
		for (String itemId : indIds) {
			LOG.info(itemId);
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			// TODO - should not need this; tvector already used to calculate likelihoods
			int[] tvector = bmToVector(targetProfileBM, knowledgeBase.getNumClassNodes());
			int j = knowledgeBase.getIndividualIndex(itemId);
			double[] likelihoodsForItem = likelihoods[j];
			double logpsum = 0;
			for (int i=0; i<likelihoodsForItem.length; i++) {
				double prob;
				if (qvector[i] == 1)
					prob = likelihoodsForItem[i];
				else
					prob = 1-likelihoodsForItem[i];
				/*
				if (tvector[i] ==1) 
					if (qvector[i] == 1)
						prob = likelihoodsForItem[i];
					else
						prob = 1-likelihoodsForItem[i];
				else
					if (qvector[i] == 1)
						prob = nlikelihoodsForItem[i];
					else
						prob = 1-nlikelihoodsForItem[i];
				 */
				// TODO - obvious optimization - storelikelihoods as logs
				logpsum += Math.log(prob);	

			}
			double p = Math.exp(logpsum);
			pvector[localItemIndex] = p;
			indIdsVector[localItemIndex] = itemId;
			localItemIndex++;
			sumOfProbs += p;
		}
		for (int j=0; j<pvector.length; j++) {
			pvector[j] /= sumOfProbs;
			String id = indIdsVector[j];
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(id);
			mp.add(createMatch(id, label, pvector[j]));

		}

		mp.sortMatches();
		return mp;
	}

	/**
	 * Training may be a misnomer here, since we have at most one sample
	 * per label (e.g. disease)
	 * 
	 */
	public void train() {

		// 'train' on all individuals
		// TODO: training set
		Set<String> indIds = knowledgeBase.getIndividualIdsInSignature();

		double[] pvector = new double[indIds.size()];

		int localItemIndex = 0;
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			int[] tvector = bmToVector(targetProfileBM, knowledgeBase.getNumClassNodes());
			int j = knowledgeBase.getIndividualIndex(itemId);
			for (int i=0; i < knowledgeBase.getNumClassNodes(); i++) {
				String classId = knowledgeBase.getClassId(i);
				// p ( feature | label, all parentOf(feature) = true)
				//EWAHCompressedBitmap parentsBM = knowledgeBase.getSuperClassesBM(classId);
				//EWAHCompressedBitmap indsFeatureBM = knowledgeBase.getIndividualsByType(classId);
				//EWAHCompressedBitmap indsParemtsFeatureBM = knowledgeBase.getIndividualsByTypes(classIds);
				double pFeatGivenLabel =  0.0;
				double prob;
				double nprob;
				if (tvector[i] ==1) { 
					// disease has feature.
					// if observations are infallible then p(feature|disease) = 1,
					// but in practice there are false negatives (not in query, in disease)
					prob = 0.99; // 1-FNR
				}
				else {
					// disease does not have feature
					// if observations are infallible p(feature|disease) = 0
					// but in practice there are false positives (in query, not in disease)
					prob = 0.05; // FPR
				}
				likelihoods[j][i] = prob;
			}		
		}
	}



}
