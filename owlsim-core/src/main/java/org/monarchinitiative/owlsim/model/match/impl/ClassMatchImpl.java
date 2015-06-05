package org.monarchinitiative.owlsim.model.match.impl;

import org.monarchinitiative.owlsim.model.match.ClassMatch;

/**
 * 
 * @author cjm
 *
 */
public class ClassMatchImpl implements ClassMatch {
	
	private String queryClassId;
	private String matchClassId;
	private String MICAClassId;
	private double MICAInformationContent;
	public String getQueryClassId() {
		return queryClassId;
	}
	public void setQueryClassId(String queryClassId) {
		this.queryClassId = queryClassId;
	}
	public String getMatchClassId() {
		return matchClassId;
	}
	public void setMatchClassId(String targetClassId) {
		this.matchClassId = targetClassId;
	}
	public String getMICAClassId() {
		return MICAClassId;
	}
	public void setMICAClassId(String mICAClassId) {
		MICAClassId = mICAClassId;
	}
	public double getMICAInformationContent() {
		return MICAInformationContent;
	}
	public void setMICAInformationContent(double mICAInformationContent) {
		MICAInformationContent = mICAInformationContent;
	}
	

	
}
