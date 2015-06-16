package org.monarchinitiative.owlsim.compute.runner;

import java.util.List;

/**
 * A specification of a set of {@link Job}s to be executed
 * 
 * @author cjm
 *
 */
public class RunConfiguration {
	
	private String description;
	private List<String> ontologyInputs;
	private String tool;
	
	private List<PairwiseJob> pairwiseJobs;
	private List<SearchJob> searchJobs;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<String> getOntologyInputs() {
		return ontologyInputs;
	}

	public void setOntologyInputs(List<String> ontologyInputs) {
		this.ontologyInputs = ontologyInputs;
	}

	public String getTool() {
		return tool;
	}

	public void setTool(String tool) {
		this.tool = tool;
	}

	public List<PairwiseJob> getPairwiseJobs() {
		return pairwiseJobs;
	}

	public void setPairwiseJobs(List<PairwiseJob> pairwiseJobs) {
		this.pairwiseJobs = pairwiseJobs;
	}

	public List<SearchJob> getSearchJobs() {
		return searchJobs;
	}

	public void setSearchJobs(List<SearchJob> searchJobs) {
		this.searchJobs = searchJobs;
	}
	
	

}
