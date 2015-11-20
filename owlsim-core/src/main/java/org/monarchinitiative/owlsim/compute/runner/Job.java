package org.monarchinitiative.owlsim.compute.runner;

import java.util.Set;
import java.util.UUID;

public abstract class Job {

	public String id = null;
	public abstract String getQueryIndividual();
	public abstract Set<String> getQueryClassIds();
	public abstract Set<String> getNegatedQueryClassIds();
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public void setId() {
		this.id = UUID.randomUUID().toString();
	}
	
	

}
