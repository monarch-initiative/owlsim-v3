package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.GraphUtils;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.model.match.BasicQuery;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Graphical Model Profile Matcher
 * 
 * See TBA paper
 * 
 * @author cjm
 *
 */
public class GMProfileMatcher extends AbstractSemanticSimilarityProfileMatcher implements ProfileMatcher {
	
	private Logger LOG = Logger.getLogger(GMProfileMatcher.class);
	private String[] queryClassArray;
	private double[] pmatrix00;
	private double[] pmatrix01;
	private double[] pmatrix10;
	private double[] pmatrix11;
	private double[] prVector; // πT 
	private double[] prUVector; // UT′
	private double q = 0.05; // resample probability
	
	/**
	 * @param kb
	 */
	public GMProfileMatcher(BMKnowledgeBase kb) {
		super(kb);
		initializeProbabilityMatrix();
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new GMProfileMatcher(kb);
	}
	
	private void initializeProbabilityMatrix() {
		int n = knowledgeBase.getNumClassNodes();
		EWAHCompressedBitmap[] parentChildMatrixBM = knowledgeBase.getStoredDirectSubClassIndex();
		
		int[] freqIndex = knowledgeBase.getClassFrequencyArray();
		int r = knowledgeBase.getRootIndex();
		int rootFreq = freqIndex[r];

		pmatrix00 = new double[n];
		pmatrix01 = new double[n];
		pmatrix10 = new double[n];
		pmatrix11 = new double[n];
		prVector = new double[n];
		prUVector = new double[n];
		
		
		GraphUtils u = new GraphUtils();
		int[] indices = u.getTopologicalSort(knowledgeBase);
		for (int i : indices) {
			String cid = knowledgeBase.getClassId(i); // for debugging only
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(cid);
			
			// TODO: hack - we want to avoid zero probability on leaves
			//  assume that our corpus includes an additional unobserved individual
			//  for every class
			double pt = (freqIndex[i]+1) / (double) (rootFreq+1);
			prVector[i] = pt;
			// TODO - use logs
			pmatrix00[i] = (1-pt) * ((1-q) + q * (1-pt));
			pmatrix01[i] = (1-pt) *  q * pt;
			pmatrix10[i] = pt * q * (1-pt);
			pmatrix11[i] = pt * ( (1-q) + q * pt);
			
			LOG.info("Class; "+cid+" "+label);
			LOG.info("M00 = "+pmatrix00[i]);
			LOG.info("M01 = "+pmatrix01[i]);
			LOG.info("M10 = "+pmatrix10[i]);
			LOG.info("M11 = "+pmatrix11[i]);
			
			// calculate UT′ using DP
			EWAHCompressedBitmap childrenBM = parentChildMatrixBM[i];
			if (childrenBM.cardinality() == 0) {
				prUVector[i] = 0;
			}
			else {
				double prUT = 1.0;
				for (int k : childrenBM.getPositions()) {
					double prT = prVector[k];
					double XT = (1-prT) + prT * prUVector[k];
					prUT *= XT;
				}
				prUVector[i] = prUT;
			}
		}
		
	}
	
	private double calculateLogOddsRatio(EWAHCompressedBitmap qbmDirect,
			EWAHCompressedBitmap qbm, EWAHCompressedBitmap tbm) {
		List<Integer> qon = qbm.getPositions(); // switches that are on in G (query)
		List<Integer> ton = tbm.getPositions(); // switches that are on in G' (target)
		
		int n = knowledgeBase.getNumClassNodes();
		
		int[] freqIndex = knowledgeBase.getClassFrequencyArray();
		int r = knowledgeBase.getRootIndex();
		int rootFreq = freqIndex[r];
		
		
		// TODO - populate this
		EWAHCompressedBitmap[] parentChildMatrixBM = knowledgeBase.getStoredDirectSubClassIndex();
		
		double[] D00 = new double[n];
		double[] D01 = new double[n];
		double[] D10 = new double[n];
		double[] D11 = new double[n];
		
		// for verification: can be removed when tests are implemented
		boolean[] isIndexed = new boolean[n];
	
		GraphUtils u = new GraphUtils();
		int[] indices = u.getTopologicalSort(knowledgeBase);
		boolean[] lvector = new boolean[indices.length];
		boolean[] sublvector = new boolean[indices.length];
		
		// for now we treat queries as leaves and ignore sub-leaves
		// TODO - optimize
		// TODO - figure out a better solution for non-leaf queries
		for (int i=0; i<indices.length; i++) {
			lvector[i] = false;
			sublvector[i] = false;
			
			//LOG.info("LUUK"+i+" "+knowledgeBase.getClassId(i));
			if (parentChildMatrixBM[i].cardinality() == 0) {
				// true leaf
				lvector[i] = true; 
			}
			else if (qbmDirect.getPositions().contains(i)) {
				// fake leaf
				lvector[i] = true; 
			}
			else if (knowledgeBase.getSuperClassesBM(i).andCardinality(qbmDirect) > 0) {
				sublvector[i] = true; 
			}
		}
		
		for (int ti : indices) {
			
			String cid = knowledgeBase.getClassId(ti); // for debugging only
					
			if (lvector[ti]) {
				// Leaf
				// hack: we exclude all nodes beneath a query node,
				// and treat all query nodes as leaves

				boolean a = qon.contains(ti);
				boolean b = ton.contains(ti);
				
				D00[ti] = !a && !b ? pmatrix00[ti] : 0;
				D01[ti] = !a && b  ? pmatrix01[ti] : 0;
				D10[ti] = a  && !b ? pmatrix10[ti] : 0;
				D11[ti] = a  && b  ? pmatrix11[ti] : 0;
				LOG.info("Class:"+cid);
				LOG.info("Leaf D00="+D00[ti]);
				LOG.info("Leaf D01="+D01[ti]);
				LOG.info("Leaf D10="+D10[ti]);
				LOG.info("Leaf D11="+D11[ti]);
			}
			else {
				// first we initialize each element of D with the corresponding
				// value in M. We will then multiply this for all child nodes
				
				// TODO - use logs
				D00[ti] = pmatrix00[ti];
				D01[ti] = pmatrix01[ti];
				D10[ti] = pmatrix10[ti];
				D11[ti] = pmatrix11[ti];
				
				//LOG.info("i="+knowledgeBase.getClassId(ti));
				for (int k : parentChildMatrixBM[ti].getPositions()) {
					
					// this is guaranteed never to happen due to topological sort.
					// TODO: remove post-testing.
					if (!isIndexed[k]) {
						LOG.error("LOGIC ERROR: "+knowledgeBase.getClassId(k));
					}

					D00[ti] *= D00[k];
					D01[ti] *= ( D00[k] + D01[k] );
					D10[ti] *= ( D00[k] + D10[k] );
					D11[ti] *= ( D00[k] + D10[k] + D10[k] + D11[k]);
				}
				
			}
			isIndexed[ti] = true;
		}
		double likelihood =
				D00[r] + D01[r] + D10[r] + D11[r];
		LOG.info("Likelihood="+ D00[r] + " + " + D01[r] + " + " + D10[r] + " + " + D11[r]+
				" = "+likelihood);
		double prQ = calculateProbabilityNullModel(qbm);
		double prT = calculateProbabilityNullModel(tbm);
		double logOddsRatio = likelihood / (prQ + prT);
		return logOddsRatio;
	}	
	
	private EWAHCompressedBitmap getUnobserved(EWAHCompressedBitmap obsBM) {
		int obsBMsize = obsBM.cardinality();
		int n = knowledgeBase.getNumClassNodes();
		Set<Integer> bits = new HashSet<Integer>();
		for (int i=0; i<n; i++) {
			if (obsBM.getPositions().contains(i))
				continue;
			EWAHCompressedBitmap pbm = knowledgeBase.getDirectSuperClassesBM(i);
			if (obsBM.andCardinality(pbm) == obsBMsize) {
				bits.add(i);
			}
		}
		return EWAHUtils.converIndexSetToBitmap(bits); // TODO
	}
	
	private double calculateProbabilityNullModel(EWAHCompressedBitmap obsBM) {
		double p = 1.0;
		int n = knowledgeBase.getNumClassNodes();
		for (int i=0; i<n; i++) {
			// TODO add generic method for multiplying subset of prVector (and use logs)
			if (obsBM.getPositions().contains(i))
				p *= prVector[i];
		}
		EWAHCompressedBitmap U = getUnobserved(obsBM);
		for (int i=0; i<n; i++) {
			if (U.getPositions().contains(i))
				p *= prUVector[i];
		}
	
		return p;
		
	}

	/**
	 * @param q
	 * @return match profile containing probabilities of each individual
	 */
	public MatchSet findMatchProfileImpl(BasicQuery q) {
		
		
		Set<String> qClassIds = q.getQueryClassIds();
		int qsize = qClassIds.size();
		queryClassArray = qClassIds.toArray(new String[qsize]);
		EWAHCompressedBitmap queryProfileBMArr[] = getProfileSetBM(queryClassArray);
		EWAHCompressedBitmap queryProfileDirect = getDirectProfileBM(q);
		EWAHCompressedBitmap queryProfile = getProfileBM(q);
		
		
		MatchSet mp =  MatchSetImpl.create(q);
		
		List<String> indIds = getFilteredIndividualIds(q.getFilter());
		for (String itemId : indIds) {
			EWAHCompressedBitmap targetProfileBM = knowledgeBase.getTypesBM(itemId);
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			double p = calculateLogOddsRatio(queryProfileDirect, queryProfile, targetProfileBM);

			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			Match m = MatchImpl.create(itemId, label, p);
			mp.add(m);
		}
		mp.sortMatches();
		return mp;
	}





}
