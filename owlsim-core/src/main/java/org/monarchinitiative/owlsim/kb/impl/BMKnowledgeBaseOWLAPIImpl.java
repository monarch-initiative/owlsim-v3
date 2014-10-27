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
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.CURIEMapper;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.ewah.EWAHKnowledgeBaseStore;
import org.monarchinitiative.owlsim.model.kb.Attribute;
import org.monarchinitiative.owlsim.model.kb.Entity;
import org.monarchinitiative.owlsim.model.kb.KBMetadata;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLIndividualAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLObjectComplementOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLPropertyAssertionObject;
import org.semanticweb.owlapi.model.OWLPropertyExpression;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.google.common.base.Preconditions;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Implementation of {@link BMKnowledgeBase} that uses the OWLAPI.
 * 
 * An OWL reasoner is used. This guarantees the graphs is a DAG.
 * (equivalence sets are mapped to the same node. subclass is mapped to DAG edges).
 * 
 * See also: {@link OWLLoader}
 * 
 * TODO - elininate unused methods
 * 
 * @author cjm
 *
 */
public class BMKnowledgeBaseOWLAPIImpl implements BMKnowledgeBase {

	private Logger LOG = Logger.getLogger(BMKnowledgeBaseOWLAPIImpl.class);

	private KBMetadata kbMetdata;

	private EWAHKnowledgeBaseStore ontoEWAHStore;
	private OWLOntology owlOntology;
	private OWLOntology owlDataOntology;
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

	private int[] individualCountPerClassArray;

	CURIEMapper curieMapper;
	LabelMapper labelMapper;

	/**
	 * @param owlOntology
	 * @param owlDataOntology
	 * @param rf
	 */
	public BMKnowledgeBaseOWLAPIImpl(OWLOntology owlOntology,
			OWLOntology owlDataOntology, OWLReasonerFactory rf) {
		super();
		curieMapper = new CURIEMapperImpl();
		labelMapper = new LabelMapperImpl(curieMapper);

		this.owlOntology = owlOntology;
		this.owlDataOntology = owlDataOntology;
		if (owlDataOntology != null) {
			translateFromDataOntology();
		}
		this.owlReasoner = rf.createReasoner(owlOntology);		
		createMap();
		ontoEWAHStore = new EWAHKnowledgeBaseStore(classNodes.size(), individualNodes.size());
		storeInferences();
		populateLabelsFromOntology(labelMapper, owlOntology);
		if (owlDataOntology != null) {
			LOG.info("Fetching labels from "+owlDataOntology);
			// the data ontology may contain labels of data items
			populateLabelsFromOntology(labelMapper, owlDataOntology);
		}
	}

	

	public static BMKnowledgeBase create(OWLOntology owlOntology, OWLReasonerFactory rf) {
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology, null, rf);
	}

	public static BMKnowledgeBase create(OWLOntology owlOntology, 
			OWLOntology owlDataOntology,
			OWLReasonerFactory rf) {
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology,owlDataOntology, rf);
	}
	
	

	public KBMetadata getKbMetdata() {
		return kbMetdata;
	}



	public void setKbMetdata(KBMetadata kbMetdata) {
		this.kbMetdata = kbMetdata;
	}



	private String getShortForm(IRI iri) {
		return curieMapper.getShortForm(iri);
	}
	private void populateLabelsFromOntology(LabelMapper labelMapper, OWLOntology ontology) {
		LOG.info("Populating labels from "+ontology);
		int n=0;
		for (OWLAnnotationAssertionAxiom aaa : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
			if (aaa.getProperty().isLabel()) {
				if (aaa.getSubject() instanceof IRI &&
					aaa.getValue() instanceof OWLLiteral) {
					labelMapper.add(getShortForm((IRI) aaa.getSubject()), 
							((OWLLiteral) aaa.getValue()).getLiteral());
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
				labelMapper.add(getShortForm(obj.getIRI()), 
						obj.getIRI().getFragment());
				n++;
			}
		}
		LOG.info("Label axioms mapped: "+n);
	}

	/**
	 * @return utility object to map labels to ids
	 */
	public LabelMapper getLabelMapper() {
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

	public int getNumClassNodes() {
		return classNodeArray.length;
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

	// Assumption: data ontology includes ObjectPropertyAssertions
	// TODO: make flexible
	// TODO: extract associations
	private void translateFromDataOntology() {
		// TODO: allow other axiom types
		for (OWLObjectPropertyAssertionAxiom opa : owlDataOntology.getAxioms(AxiomType.OBJECT_PROPERTY_ASSERTION)) {
			OWLIndividual obj = opa.getObject();
			if (obj instanceof OWLNamedIndividual) {
				OWLClass type = 
						getOWLDataFactory().getOWLClass(((OWLNamedIndividual) obj).getIRI());
				OWLClassAssertionAxiom ca = getOWLDataFactory().getOWLClassAssertionAxiom(type, opa.getSubject());
				owlOntology.getOWLOntologyManager().addAxiom(owlOntology, ca);
			}
		}
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
		propertyValueMapMap = new HashMap<String,Map<String,Set<Object>>>();
		final HashMap<Node<OWLClass>, Integer> classNodeToFrequencyMap = new HashMap<Node<OWLClass>, Integer>();
		for (OWLClass c : classesInSignature) {
			if (owlReasoner.getInstances(c, false).isEmpty()) {
				//TODO: deal with subclasses
				//LOG.info("Skipping non-instantiated class: "+c);
				//continue;
			}
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
			setPropertyValues(owlOntology,i);
			if (owlDataOntology != null)
				setPropertyValues(owlDataOntology,i);
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
		individualCountPerClassArray = new int[numClassNodes];
		for (int i=0; i<numClassNodes; i++) {
			classNodeToIntegerMap.put(classNodeArray[i], i);
			//LOG.info(classNodeArray[i] + " ix="+i + " FREQ="+classNodeToFrequencyMap.get(classNodeArray[i]));
			//LOG.info(classNodeArray[i] + " ix="+i + " IX_REV="+classNodeToIntegerMap.get(classNodeArray[i]));
			individualCountPerClassArray[i] = classNodeToFrequencyMap.get(classNodeArray[i]);
		}
		individualNodeArray = individualNodes.toArray(new Node[individualNodes.size()]);
		for (int i=0; i<individualNodes.size(); i++) {
			individualNodeToIntegerMap.put(individualNodeArray[i], i);
		}

	}


	private void setPropertyValues(OWLOntology ont, OWLNamedIndividual i) {
		Preconditions.checkNotNull(i);
		Map<String, Set<Object>> pvm = new HashMap<String, Set<Object>>();
		String id = curieMapper.getShortForm(i.getIRI());
		propertyValueMapMap.put(id, pvm);
		for (OWLIndividualAxiom ax : ont.getAxioms(i)) {
			if (ax instanceof OWLPropertyAssertionAxiom) {
				OWLPropertyAssertionAxiom paa = (OWLPropertyAssertionAxiom)ax;
				OWLPropertyExpression p = paa.getProperty();
				if (p instanceof OWLObjectProperty) {
					String pid = curieMapper.getShortForm(((OWLObjectProperty) p).getIRI());
					OWLPropertyAssertionObject obj = paa.getObject();
					if (obj instanceof OWLLiteral) {
						addPropertyValue(pvm,pid, ((OWLLiteral)obj).getLiteral());
					}
					else if (obj instanceof OWLNamedIndividual) {
						addPropertyValue(pvm,pid,
								curieMapper.getShortForm(((OWLNamedIndividual) obj).getIRI()));
						
					}
					
				}
			}
		}
		
	}


	private void addPropertyValue(Map<String, Set<Object>> pvm, String pid, String v) {
		//LOG.debug("PV="+pid+"="+v);
		if (!pvm.containsKey(pid))
			pvm.put(pid, new HashSet<Object>());
		pvm.get(pid).add(v);
	}


	private void storeInferences() {

		// Note: if there are any nodes containing >1 class or individual, then
		//  the store method is called redundantly. This is unlikely to affect performance,
		//  and the semantics are unchanged
		for (OWLClass c : getClassesInSignature()) {
			int clsIndex = 	getIndex(c);
			//LOG.info("Storing inferences for "+c+" --> " + clsIndex);
			Set<Integer> sups = getIntegersForClassSet(owlReasoner.getSuperClasses(c, false));
			sups.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(c)));

			Set<Integer> subs = getIntegersForClassSet(owlReasoner.getSubClasses(c, false));
			subs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(c)));

			ontoEWAHStore.setDirectSuperClasses(
					clsIndex, 
					getIntegersForClassSet(owlReasoner.getSuperClasses(c, true)));
			ontoEWAHStore.setSuperClasses(
					clsIndex, 
					sups);
			ontoEWAHStore.setDirectSubClasses(
					clsIndex, 
					getIntegersForClassSet(owlReasoner.getSubClasses(c, true)));
			ontoEWAHStore.setSubClasses(
					clsIndex, 
					subs);
			
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
		
			// Treat CLassAssertion( ComplementOf(c) i) as a negative assertion
			Set<Integer> ncs = new HashSet<Integer>();
			for (OWLClassAssertionAxiom cx : owlOntology.getClassAssertionAxioms(i)) {
				// TODO: investigate efficiency - number of items set may be high
				if (cx.getClassExpression() instanceof OWLObjectComplementOf) {
					OWLObjectComplementOf nx = (OWLObjectComplementOf)(cx.getClassExpression());
					OWLClassExpression nc = nx.getOperand();
					ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(nc, false)));
					ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(nc, false)));
				}
			}
			ontoEWAHStore.setNegatedTypes(individualIndex, ncs);
		}

	}

	// TODO
	private void storeIndividualProperties() {
		for (OWLNamedIndividual i : individualsInSignature) {
			for ( OWLIndividualAxiom ax : owlOntology.getAxioms(i) ) {
				if (ax instanceof OWLObjectPropertyAssertionAxiom) {
					OWLObjectPropertyExpression p = ((OWLObjectPropertyAssertionAxiom)ax).getProperty();
				}
			}
		}		
	}

	// TODO - complete this
	// TODO - separate this out as it is not an OWLAPI model. Maybe sparql is overkill here?
	// use sparql to query the memory model
	private void storeIndividualToClassFrequencies() {
		String sparql = "";
		Query query = QueryFactory.create(sparql);
		Model model = null;
		QueryExecution qexec = QueryExecutionFactory.create(query, model);
		ResultSet results = qexec.execSelect() ;
		for ( ; results.hasNext() ; ) {
			QuerySolution soln = results.nextSolution() ;
			RDFNode x = soln.get("varName") ;       // Get a result variable by name.
			Resource r = soln.getResource("VarR") ; // Get a result variable - must be a resource
			Literal l = soln.getLiteral("VarL") ;   // Get a result variable - must be a literal
		}
	}



	private Set<Integer> getIntegersForClassSet(NodeSet<OWLClass> nodeset) {
		Set<Integer> bits = new HashSet<Integer>();
		for (Node<OWLClass> n : nodeset.getNodes()) {
			if (n.contains(getOWLNothing()))
				continue;
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
	 * @param index
	 * @return OWLClass Node that corresponds to this index
	 */
	public Node<OWLNamedIndividual> getIndividualNode(int index) {
		return individualNodeArray[index];
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

	public Set<String> getClassIds(int index) {
		Node<OWLClass> n = getClassNode(index);
		Set<String> cids = new HashSet<String>();
		for (OWLClass c : n.getEntities()) {
			cids.add( curieMapper.getShortForm(c.getIRI()) );
		}
		return cids;
	}
	
	public Set<String> getClassIds(EWAHCompressedBitmap bm) {
		Set<String> cids = new HashSet<String>();
		for (int x : bm) {
			Node<OWLClass> n = getClassNode(x);
			for (OWLClass c : n.getEntities()) {
				cids.add(curieMapper.getShortForm(c.getIRI()) );
			}
		}
		return cids;
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

	public EWAHCompressedBitmap getSuperClassesBM(int classIndex) {
		return ontoEWAHStore.getSuperClasses(classIndex);
	}

	public EWAHCompressedBitmap getDirectSuperClassesBM(int classIndex) {
		return ontoEWAHStore.getDirectSuperClasses(classIndex);
	}

	public EWAHCompressedBitmap getSubClasses(int classIndex) {
		return ontoEWAHStore.getSubClasses(classIndex);
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
	 * @param classFilter
	 * @return Bitmap representation of the subset of direct types of i, which are descendants of classFilter
	 */
	protected EWAHCompressedBitmap getFilteredDirectTypesBM(OWLNamedIndividual i, OWLClass c) {
		return ontoEWAHStore.getDirectTypes(getIndex(i), this.getIndex(c));
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

	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) classes known to be not instantiated
	 */
	public EWAHCompressedBitmap getNegatedTypesBM(String id) {
		Preconditions.checkNotNull(id);
		return ontoEWAHStore.getNegatedTypes(getIndividualIndex(id));
	}

	
	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) instantiated classes
	 */
	public EWAHCompressedBitmap getDirectTypesBM(String id) {
		Preconditions.checkNotNull(id);
		return ontoEWAHStore.getDirectTypes(getIndividualIndex(id));
	}
	
	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) instantiated classes that are subclasses of classId
	 */
	public EWAHCompressedBitmap getFilteredDirectTypesBM(String id, String classId) {
		Preconditions.checkNotNull(id);
		Preconditions.checkNotNull(classId);
		return ontoEWAHStore.getDirectTypes(getIndividualIndex(id), getClassIndex(classId));
	}
	


	private OWLClass getOWLThing() {
		return getOWLDataFactory().getOWLThing();
	}
	private OWLClass getOWLNothing() {
		return getOWLDataFactory().getOWLNothing();
	}

	private OWLDataFactory getOWLDataFactory() {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory();
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
		return new Attribute(id, label);
	}

	public Entity getEntity(String id) {
		Preconditions.checkNotNull(id);
		String label = labelMapper.getArbitraryLabel(id);
		return new Entity(id, label);
	}

	public int[] getIndividualCountPerClassArray() {
		return individualCountPerClassArray;
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

	public EWAHCompressedBitmap[] getStoredDirectSubClassIndex() {
		return ontoEWAHStore.getStoredDirectSubClasses();
	}

	@Override
	public int getRootIndex() {
		return getIndex(getOWLThing());
	}



	@Override
	public String getIndividualId(int index) {
		Node<OWLNamedIndividual> n = getIndividualNode(index);
		OWLNamedIndividual ind = n.getRepresentativeElement();
		return curieMapper.getShortForm(ind.getIRI());
	}



	@Override
	public EWAHCompressedBitmap getFilteredTypesBM(Set<String> ids,
			String classId) {
		
		Set<Integer> classBits = new HashSet<Integer>();
		for (String id : ids) {
			classBits.add(this.getClassIndex(id));
		}
		
		return ontoEWAHStore.getTypes(classBits, getClassIndex(classId));

	}


	public EWAHCompressedBitmap getFilteredDirectTypesBM(Set<String> classIds,
			String classId) {
		
		Set<Integer> classBits = new HashSet<Integer>();
		for (String id : classIds) {
			classBits.add(this.getClassIndex(id));
		}
		
		return ontoEWAHStore.getDirectTypes(classBits, getClassIndex(classId));

	}




}
