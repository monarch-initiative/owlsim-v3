package org.monarchinitiative.owlsim.kb.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.NonUniqueLabelException;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.base.Preconditions;

/**
 * Utilities for mapping labels to identifiers
 * 
 * @author cjm
 *
 */
public class LabelMapperImpl implements LabelMapper {

	private Logger LOG = Logger.getLogger(LabelMapperImpl.class);

	Map<String,Set<String>> labelToIdMap;
	Map<String,Set<String>> idToLabelMap;
	CURIEMapperImpl curieMapper;
	
	/**
	 * @param curieMapper
	 */
	public LabelMapperImpl(CURIEMapperImpl curieMapper) {
		super();
		this.curieMapper = curieMapper;
		initializeMaps();
	}
	
	private void initializeMaps() {
		labelToIdMap = new HashMap<String,Set<String>>();
		idToLabelMap = new HashMap<String,Set<String>>();
	}

	/**
	 * @param label
	 * @return ids
	 */
	public Set<String> lookupByLabel(String label) {
		Set<String> ids = labelToIdMap.get(label);
		if (ids == null)
			ids = new HashSet<String>();
		return new HashSet<String>(ids);
	}
	
	/**
	 * @param label
	 * @return id that has this label
	 * @throws NonUniqueLabelException
	 */
	public String  lookupByUniqueLabel(String label) throws NonUniqueLabelException {
		Set<String> ids = lookupByLabel(label);
		if (ids.size() > 1) {
			throw new NonUniqueLabelException(label, ids);
		}
		if (ids.size() == 0)
			return null;
		return ids.iterator().next();
	}
	
	/**
	 * @param labels
	 * @return id that has this labels
	 * @throws NonUniqueLabelException
	 */
	public Set<String> lookupByUniqueLabels(Set<String> labels) throws NonUniqueLabelException {
		Set<String> ids = new HashSet<String>();
		for (String label : labels) {
			String cid = lookupByUniqueLabel(label);
			Preconditions.checkNotNull(cid);
			ids.add(cid);
		}
		return ids;
	}
	
	/**
	 * @param id
	 * @return labels
	 */
	public Set<String> getLabel(String id) {
		Set<String> labels = idToLabelMap.get(id);
		if (labels == null)
			labels = new HashSet<String>();
		return new HashSet<String>(labels);
	}
	
	/**
	 * @param id
	 * @return unique label for this id
	 * @throws NonUniqueLabelException
	 */
	public String  getUniqueLabel(String id) throws NonUniqueLabelException {
		Set<String> labels = getLabel(id);
		if (labels.size() > 1) {
			throw new NonUniqueLabelException(id, labels);
		}
		if (labels.size() == 0)
			return null;
		return labels.iterator().next();
	}

	/**
	 * @param id
	 * @return label for this id. if label is not unique, an arbitrary one is selected
	 */
	public String  getArbitraryLabel(String id) {
		Set<String> labels = getLabel(id);
		if (labels.size() == 0)
			return null;
		return labels.iterator().next();
	}
	
	/**
	 * Initialize label<->id mappings using an OWLOntology
	 * 
	 * Note this method may be moved in future
	 * 
	 * @param ontology
	 */
	public void populateFromOntology(OWLOntology ontology) {
		LOG.info("Populating labels from "+ontology);
		int n=0;
		for (OWLAnnotationAssertionAxiom aaa : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
			if (aaa.getProperty().isLabel()) {
				if (aaa.getSubject() instanceof IRI &&
					aaa.getValue() instanceof OWLLiteral) {
					add((IRI) aaa.getSubject(), (OWLLiteral) aaa.getValue());
					n++;
				}
			}
		}
		if (n==0) {
			LOG.info("Setting labels from fragments");
			Set<OWLNamedObject> objs = new HashSet<OWLNamedObject>();
			objs.addAll(ontology.getClassesInSignature());
			objs.addAll(ontology.getIndividualsInSignature());
			for (OWLNamedObject obj : objs) {
				add(obj.getIRI(), obj.getIRI().getFragment());
			}
		}
		LOG.info("Label axioms mapped: "+n);
	}

	private void add(IRI subject, String value) {
		add(curieMapper.getShortForm(subject), value);
	}

	private void add(IRI subject, OWLLiteral value) {
		add(curieMapper.getShortForm(subject), value.getLiteral());
	}

	private void add(String id, String label) {
		if (!labelToIdMap.containsKey(label))
			labelToIdMap.put(label, new HashSet<String>());
		labelToIdMap.get(label).add(id);
		if (!idToLabelMap.containsKey(id))
			idToLabelMap.put(id, new HashSet<String>());
		idToLabelMap.get(id).add(label);
	}

}
