package org.monarchinitiative.owlsim.io;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.monarchinitiative.owlsim.compute.cpt.SimplePairwiseConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.compute.cpt.impl.DefaultSimplePairwiseConditionalProbabilityIndex;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.LabelMapper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


/**
 * Writes 2D matrix as simple file
 * 
 * 
 * 
 * @author cjm
 *
 */
public class WeightedSimpleGraphWriter extends AbstractWriter {
	


	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public WeightedSimpleGraphWriter(BMKnowledgeBase kb, String file) throws FileNotFoundException {
		super(file);
		knowledgeBase = kb;
	}
	
	
	/**
	 * 
	 * @param obj
	 */
	public void write(Object obj) {
		writeClassIndex();
		stream.println("###");
		// TODO: this is not an elegant way of handling
		if (obj instanceof DefaultSimplePairwiseConditionalProbabilityIndex) {
			DefaultSimplePairwiseConditionalProbabilityIndex pci = (DefaultSimplePairwiseConditionalProbabilityIndex)obj;
			short[][] arr = pci.getCpIndex();
			int N = knowledgeBase.getNumClassNodes();
			for (int i=0; i<N; i++) {
				for (int j=0; j<N; j++) {
					short v = arr[i][j];
					if (v > 0) {
						stream.println(i+"\t"+j+"\t"+v);
					}
				}
			}
			
		}
		stream.flush();
	}
	
	public void writeClassIndex() {
		LabelMapper lm = knowledgeBase.getLabelMapper();
		for (int i=0; i<knowledgeBase.getNumClassNodes(); i++) {
			String cid = knowledgeBase.getClassId(i);
			stream.println(i+"\t"+cid+"\t"+lm.getArbitraryLabel(cid));
		}
	}


	public String toString() {
		return "JSONWriter file: "+stream;
	}

}
