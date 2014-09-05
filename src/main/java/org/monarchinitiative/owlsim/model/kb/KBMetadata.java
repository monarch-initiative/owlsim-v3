package org.monarchinitiative.owlsim.model.kb;

import java.util.Set;

public class KBMetadata {
	private String ontologyIRI;
	private String versionIRI;
	private Set<KBMetadata> importedOntologies;
	public KBMetadata(String ontologyIRI, String versionIRI,
			Set<KBMetadata> importedOntologies) {
		super();
		this.ontologyIRI = ontologyIRI;
		this.versionIRI = versionIRI;
		this.importedOntologies = importedOntologies;
	}
	public String getOntologyIRI() {
		return ontologyIRI;
	}
	public void setOntologyIRI(String ontologyIRI) {
		this.ontologyIRI = ontologyIRI;
	}
	public String getVersionIRI() {
		return versionIRI;
	}
	public void setVersionIRI(String versionIRI) {
		this.versionIRI = versionIRI;
	}
	public Set<KBMetadata> getImportedOntologies() {
		return importedOntologies;
	}
	public void setImportedOntologies(Set<KBMetadata> importedOntologies) {
		this.importedOntologies = importedOntologies;
	}
	
	
	
}
