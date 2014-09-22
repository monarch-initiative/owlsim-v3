package org.monarchinitiative.owlsim.compute.matcher.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.GraphUtils;
import org.monarchinitiative.owlsim.kb.ewah.EWAHUtils;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.Match;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.impl.MatchImpl;
import org.monarchinitiative.owlsim.model.match.impl.MatchSetImpl;

import com.google.common.base.Preconditions;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * TBN Graphical Model Profile Matcher
 * 
 * See paper (when it's ready)
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
	private GMProfileMatcherConfig config;
	
	public static class GMProfileMatcherConfig {
		public double q = 0.2; // re-sample probability	
	}
	

	public GMProfileMatcher(BMKnowledgeBase kb, GMProfileMatcherConfig config) {
		super(kb);
		this.config = config;
		initializeProbabilityMatrix();
	}
	
	/**
	 * @param kb
	 * @return new instance
	 */
	public static ProfileMatcher create(BMKnowledgeBase kb) {
		return new GMProfileMatcher(kb, new GMProfileMatcherConfig());
	}
	public static ProfileMatcher create(BMKnowledgeBase kb, GMProfileMatcherConfig config) {
		return new GMProfileMatcher(kb, config);
	}
	
	
	@Deprecated
	public double getResampleProbability() {
		return config.q;
	}

	private void initializeProbabilityMatrix() {
		int n = knowledgeBase.getNumClassNodes();
		EWAHCompressedBitmap[] parentChildMatrixBM = knowledgeBase.getStoredDirectSubClassIndex();
		
		int[] freqIndex = knowledgeBase.getIndividualCountPerClassArray();
		int r = knowledgeBase.getRootIndex();
		int rootFreq = freqIndex[r];

		pmatrix00 = new double[n];
		pmatrix01 = new double[n];
		pmatrix10 = new double[n];
		pmatrix11 = new double[n];
		prVector = new double[n];
		prUVector = new double[n];
		double q = config.q;
		
		GraphUtils u = new GraphUtils();
		int[] indices = u.getTopologicalSort(knowledgeBase);
		for (int i : indices) {
			String cid = knowledgeBase.getClassId(i); // for debugging only
			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(cid);
			
			// TODO: hack - we want to avoid zero probability on leaves
			//  assume that every node has an additional unseen annotation
			int numBelow = knowledgeBase.getSubClasses(i).cardinality();
			int numBelowRoot = knowledgeBase.getSubClasses(knowledgeBase.getRootIndex()).cardinality();
			LOG.debug("Adding pseudocount of "+numBelow+" for "+cid);
			double pt = (freqIndex[i]+numBelow) / (double) (rootFreq + numBelowRoot);
			Preconditions.checkState(pt <= 1.0);
			Preconditions.checkState(pt >= 0.0);
			prVector[i] = pt;
			// TODO - use logs
			pmatrix00[i] = (1-pt) * ((1-q) + q * (1-pt));
			pmatrix01[i] = (1-pt) *  q * pt;
			pmatrix10[i] = pt * q * (1-pt);
			pmatrix11[i] = pt * ( (1-q) + q * pt);
			
			LOG.debug("Class; "+cid+" "+label);
			LOG.debug("p="+pt+" q="+q);
			LOG.debug("M00 = "+pmatrix00[i]);
			LOG.debug("M01 = "+pmatrix01[i]);
			LOG.debug("M10 = "+pmatrix10[i]);
			LOG.debug("M11 = "+pmatrix11[i]);
			
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
			EWAHCompressedBitmap qbm,
			EWAHCompressedBitmap tbmDirect, 
			EWAHCompressedBitmap tbm) {
		List<Integer> qon = qbm.getPositions(); // switches that are on in G (query)
		List<Integer> ton = tbm.getPositions(); // switches that are on in G' (target)
		
		int n = knowledgeBase.getNumClassNodes();
		
		int[] freqIndex = knowledgeBase.getIndividualCountPerClassArray();
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

			String cid = knowledgeBase.getClassId(i); // for debugging only

			//LOG.debug("LUUK"+i+" "+knowledgeBase.getClassId(i));
			if (parentChildMatrixBM[i].cardinality() == 0) {
				// true leaf
				LOG.debug("True leaf:"+cid);
				lvector[i] = true; 
			}
			else if (qbmDirect.getPositions().contains(i)) {
				// fake leaf
				LOG.debug("Fake leaf (Q):"+cid);
				lvector[i] = true; 
			}
			else if (tbmDirect.getPositions().contains(i)) {
				// fake leaf
				LOG.debug("Fake leaf (T):"+cid);
				lvector[i] = true; 
			}
			else if (knowledgeBase.getSuperClassesBM(i).andCardinality(qbmDirect) > 0) {
				sublvector[i] = true; 
			}
			else if (knowledgeBase.getSuperClassesBM(i).andCardinality(tbmDirect) > 0) {
				sublvector[i] = true; 
			}
		}
		
		for (int ti : indices) {
			
			String cid = knowledgeBase.getClassId(ti); // for debugging only
			LOG.debug("Class:"+cid);
				
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
				LOG.debug("Leaf D00="+D00[ti]);
				LOG.debug("Leaf D01="+D01[ti]);
				LOG.debug("Leaf D10="+D10[ti]);
				LOG.debug("Leaf D11="+D11[ti]);
				
				// TODO : negation and frequencies
				// we take negation to explicitly block transitions
				// if T is explicitly negated in G', then M10 =~ 0 (i.e unlikely to start switched on)                                       
				// if T is explicitly negated in G,  then M01 =~ 0 (i.e unlikely to end switched on)
			}
			else {
				// non-leaf
				
				// first we initialize each element of D with the corresponding
				// value in M. We will then multiply this for all child nodes
				
				// TODO - use logs
				D00[ti] = pmatrix00[ti];
				D01[ti] = pmatrix01[ti];
				D10[ti] = pmatrix10[ti];
				D11[ti] = pmatrix11[ti];
				LOG.debug("  M00="+D00[ti]);
				LOG.debug("  M01="+D01[ti]);
				LOG.debug("  M10="+D10[ti]);
				LOG.debug("  M11="+D11[ti]);
				
				//LOG.debug("i="+knowledgeBase.getClassId(ti));
				for (int k : parentChildMatrixBM[ti].getPositions()) {
					
					// we dynamically filterout anything below an effective leaf,
					// where effective leaf is either a true leaf or a member of the query
					// TODO - revisit this when a better solution for non-leaf observables arrives
					if (sublvector[k]) {
						LOG.debug("Skipping sub-leaf");
						continue;
					}
					// this is guaranteed never to happen due to topological sort.
					// TODO: remove post-testing.
					Preconditions.checkState(isIndexed[k]);

					D00[ti] *= D00[k];
					D01[ti] *= ( D00[k] + D01[k] );
					D10[ti] *= ( D00[k] + D10[k] );
					D11[ti] *= ( D00[k] + D01[k] + D10[k] + D11[k]);
				}
				LOG.debug("NODE D00="+D00[ti]);
				LOG.debug("NODE D01="+D01[ti]);
				LOG.debug("NODE D10="+D10[ti]);
				LOG.debug("NODE D11="+D11[ti]);
				
				
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
		LOG.info("logOddsRation=" + likelihood + " / (" + prQ + " + " + prT + ") = " + logOddsRatio); 
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
	public MatchSet findMatchProfileImpl(ProfileQuery q) {
		
		
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
			EWAHCompressedBitmap targetProfileDirect = knowledgeBase.getDirectTypesBM(itemId);
			LOG.info("TARGET PROFILE for "+itemId+" "+targetProfileBM);
			double p = calculateLogOddsRatio(queryProfileDirect, queryProfile,
					targetProfileDirect, targetProfileBM);

			String label = knowledgeBase.getLabelMapper().getArbitraryLabel(itemId);
			Match m = MatchImpl.create(itemId, label, p);
			LOG.info("MATCH:"+m);
			mp.add(m);
		}
		//LOG.info("Sorting matches.... top="+mp.getMatches().get(0));
		mp.sortMatches();
		LOG.info("Sorted matches.... top="+mp.getMatchesWithRank(1));
		return mp;
	}





}
