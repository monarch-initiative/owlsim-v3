package org.monarchinitiative.owlsim.compute.stats;

public class CoAnnotationElement {

	private String classId;
	private int count;
	
	public CoAnnotationElement(String classId, int count) {
		this.classId = classId;
		this.count = count;
	}

	public String getClassId() {
		return classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}
}
