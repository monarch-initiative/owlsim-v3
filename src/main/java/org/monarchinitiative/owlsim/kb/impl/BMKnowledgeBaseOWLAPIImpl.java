package org.monarchinitiative.owlsim.kb.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.ewah.EWAHKnowledgeBaseStore;
import org.monarchinitiative.owlsim.model.kb.Attribute;
import org.monarchinitiative.owlsim.model.kb.Entity;
import org.monarchinitiative.owlsim.model.kb.impl.AttributeImpl;
import org.monarchinitiative.owlsim.model.kb.impl.EntityImpl;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.common.base.Preconditions;
import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Bridge between OWL and OntoEWAH
 * 
 * OntoEWAH references classes and individuals using integer indices for speed.
 * The class provides a mapping between these individuals and OWLNamedObjects
 * 
 * TODO - elininate unused methods
 * 
 * @author cjm
 *
 */
public class BMKnowledgeBaseOWLAPIImpl implements BMKnowledgeBase {

	private Logger LOG = Logger.getLogger(BMKnowledgeBaseOWLAPIImpl.class);


	private EWAHKnowledgeBaseStore ontoEWAHStore;
	private OWLOntology owlOntology;
	private OWLReasoner owlReasoner;

	private Map<Node<OWLClass>, Integer> classNodeToIntegerMap;
	private Node<OWLClass>[] classNodeArray;
	private Map<Node<OWLNamedIndividual>, Integer> individualNodeToIntegerMap;
	private Node<OWLNamedIndividual>[] individualNodeArray;

	private Set<Node<OWLClass>> classNodes;
	private Set<Node<OWLNamedIndividual>> individualNodes;

	private Map<OWLClass,Node<OWLClass>> classToNodeMap;
	private Map<OWLNamedIndividual,Node<OWLNamedIndividual>> individualToNodeMap;
	//private Set<OWLClass> classesInSignature;
	private Set<OWLNamedIndividual> individualsInSignature;
	private Map<String,Map<String,Set<Object>>> propertyValueMapMap;
	
	private int[] classFrequencyArray;

	CURIEMapperImpl curieMapper;
	LabelMapperImpl labelMapper;

	/**
	 * @param owlOntology
	 * @param owlReasoner
	 */
	public BMKnowledgeBaseOWLAPIImpl(OWLOntology owlOntology, OWLReasoner owlReasoner) {
		super();
		this.owlOntology = owlOntology;
		this.owlReasoner = owlReasoner;		
		createMap();
		ontoEWAHStore = new EWAHKnowledgeBaseStore(classNodes.size(), individualNodes.size());
		storeInferences();
		curieMapper = new CURIEMapperImpl();
		labelMapper = new LabelMapperImpl(curieMapper);
		labelMapper.initialize(owlOntology);
	}

	public static BMKnowledgeBase create(OWLOntology owlOntology, OWLReasonerFactory rf) {
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology, rf.createReasoner(owlOntology));
	}


	/**
	 * @return utility object to map labels to ids
	 */
	public LabelMapperImpl getLabelMapper() {
		return labelMapper;
	}




	/**
	 * @return set of all classes
	 */
	public Set<OWLClass> getClassesInSignature() {
		return classToNodeMap.keySet(); // TODO - consider optimizing
	}

	/**
	 * @return set of all class identifiers
	 */
	public Set<String> getClassIdsInSignature() {
		Set<String> ids = new HashSet<String>();
		for (OWLClass i : getClassesInSignature()) {
			ids.add(curieMapper.getShortForm(i.getIRI()));
		}
		return ids;
	}

	/**
	 * @return set of all individual identifiers
	 */
	protected Set<OWLNamedIndividual> getIndividualsInSignature() {
		return individualsInSignature;
	}

	/**
	 * @return ids
	 */
	public Set<String> getIndividualIdsInSignature() {
		Set<String> ids = new HashSet<String>();
		for (OWLNamedIndividual i : getIndividualsInSignature()) {
			ids.add(curieMapper.getShortForm(i.getIRI()));
		}
		return ids;
	}



	/**
	 * @return OWLAPI representation of the ontology
	 */
	protected OWLOntology getOwlOntology() {
		return owlOntology;
	}


	// Each OWLClass and OWLIndividual is mapped to an Integer index
	private void createMap() {
		LOG.info("Creating mapping from ontology objects to integers");
		classNodes = new HashSet<Node<OWLClass>>();
		individualNodes = new HashSet<Node<OWLNamedIndividual>>();
		Set<OWLClass> classesInSignature;
		classesInSignature = owlOntology.getClassesInSignature(true);
		classesInSignature.add(getOWLThing());
		classesInSignature.remove(getOWLNothing());
		individualsInSignature = owlOntology.getIndividualsInSignature(true);
		classToNodeMap = new HashMap<OWLClass,Node<OWLClass>>();
		individualToNodeMap = new HashMap<OWLNamedIndividual,Node<OWLNamedIndividual>>();
		classNodeToIntegerMap = new HashMap<Node<OWLClass>, Integer>();
		individualNodeToIntegerMap = new HashMap<Node<OWLNamedIndividual>, Integer>();
		final HashMap<Node<OWLClass>, Integer> classNodeToFrequencyMap = new HashMap<Node<OWLClass>, Integer>();
		for (OWLClass c : classesInSignature) {
			Node<OWLClass> node = owlReasoner.getEquivalentClasses(c);
			if (node.contains(getOWLNothing())) {
				LOG.warn("Ignoring unsatisfiable class: "+c);
				continue;
			}
			classNodes.add(node);
			classToNodeMap.put(c, node);
			classNodeToFrequencyMap.put(node, owlReasoner.getInstances(c, false).getNodes().size());
		}

		for (OWLNamedIndividual i : individualsInSignature) {
			Node<OWLNamedIndividual> node = owlReasoner.getSameIndividuals(i);
			individualNodes.add(node);
			individualToNodeMap.put(i, node);
		}
		// TODO - for bitmap operation optimization,
		//        consider ordering nodes such that most common ones have low indices; OR
		//        rarest (lowest freq) ones have low indices (for fast LCS calculation)
		List<Node<OWLClass>> classNodesSorted = new ArrayList<Node<OWLClass>>(classNodes);
		Collections.sort(classNodesSorted, 
				new Comparator<Node<OWLClass>>() {
			public int compare(Node<OWLClass> n1, Node<OWLClass> n2) {
				int f1 = classNodeToFrequencyMap.get(n1);
				int f2 = classNodeToFrequencyMap.get(n2);
				if (f1 < f2) return -1;
				if (f1 > f2) return 1;
				return 0;
			}});
		int numClassNodes = classNodesSorted.size();
		classNodeArray = classNodesSorted.toArray(new Node[numClassNodes]);
		classFrequencyArray = new int[numClassNodes];
		for (int i=0; i<numClassNodes; i++) {
			classNodeToIntegerMap.put(classNodeArray[i], i);
			//LOG.info(classNodeArray[i] + " ix="+i + " FREQ="+classNodeToFrequencyMap.get(classNodeArray[i]));
			//LOG.info(classNodeArray[i] + " ix="+i + " IX_REV="+classNodeToIntegerMap.get(classNodeArray[i]));
			classFrequencyArray[i] = classNodeToFrequencyMap.get(classNodeArray[i]);
		}
		individualNodeArray = individualNodes.toArray(new Node[individualNodes.size()]);
		for (int i=0; i<individualNodes.size(); i++) {
			individualNodeToIntegerMap.put(individualNodeArray[i], i);
		}

	}


	private void storeInferences() {

		// Note: if there are any nodes containing >1 class or individual, then
		//  the store method is called redundantly. This is unlikely to affect performance,
		//  and the semantics are unchanged
		for (OWLClass c : getClassesInSignature()) {
			int clsIndex = 	getIndex(c);
			//LOG.info("String inferences for "+c+" --> " + clsIndex);
			Set<Integer> sups = getIntegersForClassSet(owlReasoner.getSuperClasses(c, false));
			sups.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(c)));
	
			ontoEWAHStore.setDirectSuperClasses(
					clsIndex, 
					getIntegersForClassSet(owlReasoner.getSuperClasses(c, true)));
			ontoEWAHStore.setSuperClasses(
					clsIndex, 
					sups);
		}
		for (OWLNamedIndividual i : individualsInSignature) {
			int individualIndex = 	getIndex(i);
			//LOG.info("String inferences for "+i+" --> " +individualIndex);
			ontoEWAHStore.setDirectTypes(
					individualIndex, 
					getIntegersForClassSet(owlReasoner.getTypes(i, true)));
			ontoEWAHStore.setTypes(
					individualIndex, 
					getIntegersForClassSet(owlReasoner.getTypes(i, false)));
		}

	}

	private Set<Integer> getIntegersForClassSet(NodeSet<OWLClass> nodeset) {
		Set<Integer> bits = new HashSet<Integer>();
		for (Node<OWLClass> n : nodeset.getNodes()) {
			bits.add(getIndexForClassNode(n));
		}
		return bits;
	}


	private Set<Integer> getIntegersForIndividualSet(NodeSet<OWLNamedIndividual> nodeset) {
		Set<Integer> bits = new HashSet<Integer>();
		for (Node<OWLNamedIndividual> n : nodeset.getNodes()) {
			bits.add(getIndexForIndividualNode(n));
		}
		return bits;
	}

	/**
	 * Each class is mapped to an integer
	 * 
	 * Note that equivalent classes will be mapped to the same integer
	 * 
	 * @param c
	 * @return integer representation of class
	 */
	protected int getIndex(OWLClass c) {
		return getIndexForClassNode(classToNodeMap.get(c));
	}

	/**
	 * @param id
	 * @return integer representation of class with id
	 */
	public int getClassIndex(String id) {
		Preconditions.checkNotNull(id);
		return getIndex(getOWLClass(id));
	}
	
	/**
	 * @param index
	 * @return OWLClass Node that corresponds to this index
	 */
	public Node<OWLClass> getClassNode(int index) {
		return classNodeArray[index];
	}
	
	/**
	 * Note: each index can correspond to multiple classes c1...cn if this set is an equivalence set.
	 * In this case the representative classId is returned
	 * 
	 * @param index
	 * @return classId
	 */
	public String getClassId(int index) {
		Node<OWLClass> n = getClassNode(index);
		OWLClass c = n.getRepresentativeElement();
		return curieMapper.getShortForm(c.getIRI());
	}
	

	/**
	 * @param id
	 * @return integer representation of class with id
	 */
	public int getIndividualIndex(String id) {
		Preconditions.checkNotNull(id);
		return getIndex(getOWLNamedIndividual(id));
	}

	/**
	 * Each set of equivalent classes (a class node) is mapped to a unique integer
	 * 
	 * @param n
	 * @return integer representation of class node
	 */
	protected int getIndexForClassNode(Node<OWLClass> n) {
		if (!classNodeToIntegerMap.containsKey(n))
			LOG.error("No such node: "+n);
		return classNodeToIntegerMap.get(n);
	}

	/**
	 * Each individual is mapped to an integer
	 * 
	 * Note that individuals that stand in a SameAs relationship to one another
	 * will be mapped to the same integer
	 * 
	 * @param i
	 * @return integer representation of individual
	 */
	protected int getIndex(OWLNamedIndividual i) {
		return getIndexForIndividualNode(individualToNodeMap.get(i));
	}

	/**
	 * Each set of same individuals (an individual node) is mapped to a unique integer
	 * 
	 * @param n
	 * @return integer representation of class node
	 */
	protected int getIndexForIndividualNode(Node<OWLNamedIndividual> n) {
		return individualNodeToIntegerMap.get(n);
	}




	/**
	 * @param c
	 * @return Bitmap representation of set of superclasses of c (direct and indirect)
	 */
	protected EWAHCompressedBitmap getSuperClassesBM(OWLClass c) {
		return ontoEWAHStore.getSuperClasses(getIndex(c));
	}

	/**
	 * @param c
	 * @return Bitmap representation of set of direct superclasses of c
	 */
	protected EWAHCompressedBitmap getDirectSuperClassesBM(OWLClass c) {
		return ontoEWAHStore.getDirectSuperClasses(getIndex(c));
	}

	/**
	 * @param c
	 * @param isDirect 
	 * @return Bitmap representation of set ofsuperclasses of c
	 */
	protected EWAHCompressedBitmap getSuperClassesBM(OWLClass c, boolean isDirect) {
		return ontoEWAHStore.getSuperClasses(getIndex(c), isDirect);
	}

	/**
	 * @param clsSet
	 * @return union of all superClasses (direct and indirect) of any input class
	 */
	protected EWAHCompressedBitmap getSuperClassesBMByOWLClassSet(Set<OWLClass> clsSet) {
		Set<Integer> classIndices = new HashSet<Integer>();
		for (OWLClass c : clsSet) {
			classIndices.add(getIndex(c));
		}
		return ontoEWAHStore.getSuperClasses(classIndices);
	}

	public EWAHCompressedBitmap getSuperClassesBM(String cid) {
		return ontoEWAHStore.getSuperClasses(getClassIndex(cid));
	}
	public EWAHCompressedBitmap getDirectSuperClassesBM(String cid) {
		return ontoEWAHStore.getDirectSuperClasses(getClassIndex(cid));
	}

	/**
	 * @param clsIds
	 * @return union of all superClasses (direct and indirect) of any input class
	 */
	public EWAHCompressedBitmap getSuperClassesBM(Set<String> clsIds) {
		Set<Integer> classIndices = new HashSet<Integer>();
		for (String id : clsIds) {
			LOG.info("IX("+id+") = "+getClassIndex(id));
			classIndices.add(getClassIndex(id));
		}
		return ontoEWAHStore.getSuperClasses(classIndices);
	}

	/**
	 * @param i
	 * @return Bitmap representation of set of (direct or indirect) types of i
	 */
	protected EWAHCompressedBitmap getTypesBM(OWLNamedIndividual i) {
		return ontoEWAHStore.getTypes(getIndex(i));
	}

	/**
	 * @param i
	 * @return Bitmap representation of set of direct types of i
	 */
	protected EWAHCompressedBitmap getDirectTypesBM(OWLNamedIndividual i) {
		return ontoEWAHStore.getDirectTypes(getIndex(i));
	}

	/**
	 * @param i
	 * @param isDirect 
	 * @return Bitmap representation of set of (direct or indirect) types of i
	 */
	protected EWAHCompressedBitmap getTypesBM(OWLNamedIndividual i, boolean isDirect) {
		return ontoEWAHStore.getTypes(getIndex(i), isDirect);
	}

	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) instantiated classes
	 */
	public EWAHCompressedBitmap getTypesBM(String id) {
		Preconditions.checkNotNull(id);
		return ontoEWAHStore.getTypes(getIndividualIndex(id));
	}



	private OWLClass getOWLThing() {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLThing();
	}
	private OWLClass getOWLNothing() {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNothing();
	}


	/**
	 * @param obj
	 * @return CURIE-style identifier
	 */
	protected String getIdentifier(OWLNamedObject obj) {
		return obj.getIRI().toString();
	}

	/**
	 * @param id
	 * @return OWLAPI Class object
	 */
	protected OWLClass getOWLClass(String id) {
		Preconditions.checkNotNull(id);
		return getOWLClass(IRI.create(id));
	}

	/**
	 * @param iri
	 * @return OWLAPI Class object
	 */
	protected OWLClass getOWLClass(IRI iri) {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
	}

	/**
	 * @param iri
	 * @return OWLAPI Class object
	 */
	protected OWLNamedIndividual getOWLNamedIndividual(IRI iri) {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
	}

	/**
	 * @param id
	 * @return OWLAPI Class object
	 */
	public OWLNamedIndividual getOWLNamedIndividual(String id) {
		Preconditions.checkNotNull(id);
		return getOWLNamedIndividual(IRI.create(id));
	}

	public Attribute getAttribute(String id) {
		Preconditions.checkNotNull(id);
		String label = labelMapper.getArbitraryLabel(id);
		return new AttributeImpl(id, label);
	}

	public Entity getEntity(String id) {
		Preconditions.checkNotNull(id);
		String label = labelMapper.getArbitraryLabel(id);
		return new EntityImpl(id, label);
	}



	public int[] getClassFrequencyArray() {
		return classFrequencyArray;
	}

	@Override
	public Map<String, Set<Object>> getPropertyValueMap(String individualId) {
		return propertyValueMapMap.get(individualId);
	}

	@Override
	public Set<Object> getPropertyValues(String individualId, String property) {
		Map<String, Set<Object>> m = getPropertyValueMap(individualId);
		if (m.containsKey(property))
			return new HashSet<Object>(m.get(property));
		else
			return Collections.emptySet();
	}

	

}