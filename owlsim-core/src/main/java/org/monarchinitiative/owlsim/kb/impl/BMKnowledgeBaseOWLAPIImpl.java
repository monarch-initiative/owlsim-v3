package org.monarchinitiative.owlsim.kb.impl;

import com.google.common.base.Preconditions;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.io.OWLLoader;
import org.monarchinitiative.owlsim.io.Ontology;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.CURIEMapper;
import org.monarchinitiative.owlsim.kb.LabelMapper;
import org.monarchinitiative.owlsim.kb.ewah.EWAHKnowledgeBaseStore;
import org.monarchinitiative.owlsim.model.kb.Attribute;
import org.monarchinitiative.owlsim.model.kb.Entity;
import org.monarchinitiative.owlsim.model.kb.KBMetadata;
import org.prefixcommons.CurieUtil;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of {@link BMKnowledgeBase} that uses the OWLAPI.
 * 
 * An OWL reasoner is used. This guarantees the graphs is a DAG. (equivalence sets are mapped to the
 * same node. subclass is mapped to DAG edges).
 * 
 * See also: {@link OWLLoader}
 * 
 * TODO - eliminate unused methods
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

	private Map<OWLClass, Node<OWLClass>> classToNodeMap;
	private Map<OWLNamedIndividual, Node<OWLNamedIndividual>> individualToNodeMap;
	// private Set<OWLClass> classesInSignature;
	private Set<OWLNamedIndividual> individualsInSignature;
	private Map<String, Map<String, Set<Object>>> propertyValueMapMap;
	private Map<OWLClass, Set<OWLClassExpression>> opposingClassMap = new HashMap<>();

	private Map<Integer, Map<Integer, Integer>> individualToWeightedDirectTypeMap = new HashMap<>();

	private int[] individualCountPerClassArray;

	private CURIEMapper curieMapper;
	private LabelMapper labelMapper;
	private CurieUtil curieUtil;

	/**
	 * @deprecated - use the create() constructor method with the single owlOntology - this should contain all the required axioms.
	 * @param owlOntology
	 * @param owlDataOntology
	 *            TODO - fix this
	 * @param reasonerFactory
	 */
	public BMKnowledgeBaseOWLAPIImpl(OWLOntology owlOntology, OWLOntology owlDataOntology, OWLReasonerFactory reasonerFactory, CurieUtil curieUtil) {
		Objects.requireNonNull(owlOntology, "OWLOntology required - cannot be null.");
		Objects.requireNonNull(reasonerFactory, "OWLReasonerFactory required, cannot be null.");
		Objects.requireNonNull(curieUtil, "CurieUtil required - cannot be null");

		Instant start = Instant.now();
		curieMapper = new CURIEMapperImpl();
		labelMapper = new LabelMapperImpl(curieMapper);

		this.owlOntology = owlOntology;
		this.owlDataOntology = owlDataOntology;
		if (owlDataOntology != null) {
			translateFromDataOntology();
		}
		LOG.info("Creating OWL reasoner");
		this.owlReasoner = reasonerFactory.createReasoner(owlOntology);
		this.curieUtil = curieUtil;
		createMap();
		ontoEWAHStore = new EWAHKnowledgeBaseStore(classNodes.size(), individualNodes.size());
		storeInferences();
		populateLabelsFromOntology(labelMapper, owlOntology);
		if (owlDataOntology != null) {
			LOG.info("Fetching labels from " + owlDataOntology);
			// the data ontology may contain labels of data items
			populateLabelsFromOntology(labelMapper, owlDataOntology);
		}
		Instant end = Instant.now();
		LOG.info("Knowledgebase loaded in " + Duration.between(start, end).toMillis() + " ms");
	}

	public static BMKnowledgeBase create(Ontology ontology, OWLReasonerFactory owlReasonerFactory) {
		return new BMKnowledgeBaseOWLAPIImpl(ontology.getOwlOntology(), null, owlReasonerFactory, ontology.getCurieUtil());
	}

	public static BMKnowledgeBase create(OWLOntology owlOntology, OWLReasonerFactory rf, CurieUtil curieUtil) {
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology, null, rf, curieUtil);
	}

	/**
	 * @deprecated
	 * @param owlOntology
	 * @param owlDataOntology
	 * @param rf
	 * @return
	 */
	public static BMKnowledgeBase create(OWLOntology owlOntology, OWLOntology owlDataOntology, OWLReasonerFactory rf, CurieUtil curieUtil) {
		return new BMKnowledgeBaseOWLAPIImpl(owlOntology, owlDataOntology, rf, curieUtil);
	}

	public KBMetadata getKbMetdata() {
		return kbMetdata;
	}

	public void setKbMetdata(KBMetadata kbMetdata) {
		this.kbMetdata = kbMetdata;
	}

	private String getShortForm(IRI iri) {
		String iriString = iri.toString();
		return curieUtil.getCurie(iriString).orElse(iriString);
	}

	private void populateLabelsFromOntology(LabelMapper labelMapper, OWLOntology ontology) {
		LOG.info("Populating labels from " + ontology);
		int n = 0;
		for (OWLAnnotationAssertionAxiom aaa : ontology.getAxioms(AxiomType.ANNOTATION_ASSERTION)) {
			if (aaa.getProperty().isLabel()) {
				if (aaa.getSubject() instanceof IRI && aaa.getValue() instanceof OWLLiteral) {
					labelMapper.add(getShortForm((IRI) aaa.getSubject()), ((OWLLiteral) aaa.getValue()).getLiteral());
					n++;
				}
			}
		}
		if (n == 0) {
			LOG.info("Setting labels from fragments");
			Set<OWLNamedObject> objs = new HashSet<>();
			objs.addAll(ontology.getClassesInSignature());
			objs.addAll(ontology.getIndividualsInSignature());
			for (OWLNamedObject obj : objs) {
				labelMapper.add(getShortForm(obj.getIRI()), obj.getIRI().getFragment());
				n++;
			}
		}
		LOG.info("Label axioms mapped: " + n);
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
		Set<String> ids = new HashSet<>();
		for (OWLClass i : getClassesInSignature()) {
			ids.add(getShortForm(i.getIRI()));
		}
		return ids;
	}

	public Set<String> getClassIdsByOntology(String ont) {
		return getClassIdsInSignature().stream().filter(x -> isIn(x, ont)).collect(Collectors.toSet());
	}

	/**
	 * @param id
	 * @param ont
	 * @return true if id is in ontology
	 */
	private boolean isIn(String id, String ont) {
		// TODO - use curie util
		return id.startsWith(ont + ":") || id.contains("/" + ont + "_");
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
		Set<String> ids = new HashSet<>();
		for (OWLNamedIndividual i : getIndividualsInSignature()) {
			ids.add(getShortForm(i.getIRI()));
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
				OWLClass type = getOWLDataFactory().getOWLClass(((OWLNamedIndividual) obj).getIRI());
				OWLClassAssertionAxiom ca = getOWLDataFactory().getOWLClassAssertionAxiom(type, opa.getSubject());
				owlOntology.getOWLOntologyManager().addAxiom(owlOntology, ca);
			}
		}
	}

	// Each OWLClass and OWLIndividual is mapped to an Integer index
	private void createMap() {
		LOG.info("Creating mapping from ontology objects to integers");
		classNodes = new HashSet<>();
		individualNodes = new HashSet<>();
		Set<OWLClass> classesInSignature;
		classesInSignature = owlOntology.getClassesInSignature(true);
		LOG.info("|classes|=" + classesInSignature.size());
		classesInSignature.add(getOWLThing());
		classesInSignature.remove(getOWLNothing());
		individualsInSignature = owlOntology.getIndividualsInSignature(true);
		LOG.info("|individuals|=" + individualsInSignature.size());
		classToNodeMap = new HashMap<>();
		individualToNodeMap = new HashMap<>();
		classNodeToIntegerMap = new HashMap<>();
		individualNodeToIntegerMap = new HashMap<>();
		propertyValueMapMap = new HashMap<>();
		final HashMap<Node<OWLClass>, Integer> classNodeToFrequencyMap = new HashMap<>();
		final HashMap<Node<OWLClass>, Double> classNodeToFreqDepthMap = new HashMap<>();
		for (OWLClass c : classesInSignature) {
			if (owlReasoner.getInstances(c, false).isEmpty()) {
				// TODO: deal with subclasses
				// LOG.info("Skipping non-instantiated class: "+c);
				// continue;
			}
			Node<OWLClass> node = owlReasoner.getEquivalentClasses(c);
			if (node.contains(getOWLNothing())) {
				LOG.warn("Ignoring unsatisfiable class: " + c);
				continue;
			}
			classNodes.add(node);
			classToNodeMap.put(c, node);
			int numAncNodes = owlReasoner.getSuperClasses(c, false).getNodes().size();
			int freq = owlReasoner.getInstances(c, false).getNodes().size();
			classNodeToFrequencyMap.put(node, freq);

			// freq depth is inversely correlated informativeness;
			// frequency is primary measure (high freq = low informativeness);
			// if frequency is tied, then tie is broken by number of ancestors
			// (high ancestors = high informativeness)
			// note that if frequency is not tied, then depth/ancestors should make
            // no overall difference - we ensure this by taking the proportion of
            // ancestor nodes divided by number of classes (there are always equal
			// or more classes than nodes)
			double freqDepth = freq + 1 - (numAncNodes / (double) classesInSignature.size());
			// LOG.info("freqDepth = "+freq+" "+freqDepth);
			classNodeToFreqDepthMap.put(node, freqDepth);
		}

		for (OWLNamedIndividual i : individualsInSignature) {
			Node<OWLNamedIndividual> node = owlReasoner.getSameIndividuals(i);
			individualNodes.add(node);
			individualToNodeMap.put(i, node);
			setPropertyValues(owlOntology, i);
			if (owlDataOntology != null)
				setPropertyValues(owlDataOntology, i);
		}

		// Order class nodes such that LOW frequencies (HIGH Information Content)
		// nodes are have LOWER indices
		// TODO: use depth as a tie breaker
		List<Node<OWLClass>> classNodesSorted = new ArrayList<>(classNodes);
		classNodesSorted.sort((n1, n2) -> {
			double f1 = classNodeToFreqDepthMap.get(n1);
			double f2 = classNodeToFreqDepthMap.get(n2);
			if (f1 < f2)
				return -1;
			if (f1 > f2)
				return 1;
			return 0;
		});
		int numClassNodes = classNodesSorted.size();
		classNodeArray = classNodesSorted.toArray(new Node[numClassNodes]);
		individualCountPerClassArray = new int[numClassNodes];
		for (int i = 0; i < numClassNodes; i++) {
			classNodeToIntegerMap.put(classNodeArray[i], i);
			// LOG.info(classNodeArray[i] + " ix="+i + "
			// FREQ="+classNodeToFrequencyMap.get(classNodeArray[i]));
			// LOG.info(classNodeArray[i] + " ix="+i + "
			// IX_REV="+classNodeToIntegerMap.get(classNodeArray[i]));
			individualCountPerClassArray[i] = classNodeToFrequencyMap.get(classNodeArray[i]);
		}
		individualNodeArray = individualNodes.toArray(new Node[individualNodes.size()]);
		for (int i = 0; i < individualNodes.size(); i++) {
			individualNodeToIntegerMap.put(individualNodeArray[i], i);
		}

	}

	private void setPropertyValues(OWLOntology ont, OWLNamedIndividual i) {
		Preconditions.checkNotNull(i);
		Map<String, Set<Object>> pvm = new HashMap<>();
		String id = getShortForm(i.getIRI());
		propertyValueMapMap.put(id, pvm);
		for (OWLIndividualAxiom ax : ont.getAxioms(i)) {
			if (ax instanceof OWLPropertyAssertionAxiom) {
				OWLPropertyAssertionAxiom paa = (OWLPropertyAssertionAxiom) ax;
				OWLPropertyExpression p = paa.getProperty();
				if (p instanceof OWLObjectProperty || p instanceof OWLDataProperty) {
					String pid;
					if (p instanceof OWLObjectProperty)
						pid = getShortForm(((OWLObjectProperty) p).getIRI());
					else
						pid = getShortForm(((OWLDataProperty) p).getIRI());
					OWLPropertyAssertionObject obj = paa.getObject();
					if (obj instanceof OWLLiteral) {
						addPropertyValue(pvm, pid, ((OWLLiteral) obj).getLiteral());
					} else if (obj instanceof OWLNamedIndividual) {
						addPropertyValue(pvm, pid, getShortForm(((OWLNamedIndividual) obj).getIRI()));

					}

				} else if (false) {
					String pid = getShortForm(((OWLDataProperty) p).getIRI());
					OWLLiteral obj = ((OWLDataPropertyAssertionAxiom) paa).getObject();
					if (obj instanceof OWLLiteral) {
						addPropertyValue(pvm, pid, ((OWLLiteral) obj).getLiteral());
					} else if (obj instanceof OWLNamedIndividual) {
						addPropertyValue(pvm, pid, getShortForm(((OWLNamedIndividual) obj).getIRI()));

					}

				}
			}
		}

	}

	private void addPropertyValue(Map<String, Set<Object>> pvm, String pid, String v) {
		// LOG.debug("PV="+pid+"="+v);
		if (!pvm.containsKey(pid))
			pvm.put(pid, new HashSet<>());
		pvm.get(pid).add(v);
	}

	private void addOpposingClassPair(OWLClass c, OWLClassExpression dc) {
		addOpposingClassPairAsym(c, dc);
		if (!dc.isAnonymous())
			addOpposingClassPairAsym(dc.asOWLClass(), c);
	}

	private void addOpposingClassPairAsym(OWLClass c, OWLClassExpression d) {
		if (!opposingClassMap.containsKey(c))
			opposingClassMap.put(c, new HashSet<>());
		opposingClassMap.get(c).add(d);
	}

	private void storeInferences() {
		LOG.info("Storing inferences...");
		// Note: if there are any nodes containing >1 class or individual, then
		// the store method is called redundantly. This is unlikely to affect performance,
		// and the semantics are unchanged
		for (OWLClass c : getClassesInSignature()) {
			int clsIndex = getIndex(c);
			// LOG.info("Storing inferences for "+c+" --> " + clsIndex);
			Set<Integer> sups = getIntegersForClassSet(owlReasoner.getSuperClasses(c, false));
			sups.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(c)));

			Set<Integer> subs = getIntegersForClassSet(owlReasoner.getSubClasses(c, false));
			subs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(c)));

			ontoEWAHStore.setDirectSuperClasses(clsIndex, getIntegersForClassSet(owlReasoner.getSuperClasses(c, true)));
			ontoEWAHStore.setSuperClasses(clsIndex, sups);
			ontoEWAHStore.setDirectSubClasses(clsIndex, getIntegersForClassSet(owlReasoner.getSubClasses(c, true)));
			ontoEWAHStore.setSubClasses(clsIndex, subs);

			// Find all disjoint pairs plus opposing pairs
			for (OWLAnnotationAssertionAxiom aaa : owlOntology.getAnnotationAssertionAxioms(c.getIRI())) {
				// RO_0002604 is-opposite-of. TODO - use a vocabulary object
				if (aaa.getProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/RO_0002604")) {
					OWLAnnotationValue v = aaa.getValue();
					if (v instanceof IRI) {
						IRI dciri = (IRI) v;
						OWLClass dc = owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(dciri);
						addOpposingClassPair(c, dc);

					}
				}
			}

			for (OWLDisjointClassesAxiom dca : owlOntology.getDisjointClassesAxioms(c)) {
				for (OWLClassExpression dc : dca.getClassExpressionsMinus(c)) {
					addOpposingClassPair(c, dc);
				}
			}

			// direct individuals are those asserted to be of type c or anything equivalent to c
			Set<Integer> individualInts = new HashSet<>();
			for (OWLClass ec : owlReasoner.getEquivalentClasses(c).getEntities()) {
				for (OWLClassAssertionAxiom ax : owlOntology.getClassAssertionAxioms(ec)) {
					if (ax.getIndividual().isNamed()) {
						individualInts.add(getIndex(ax.getIndividual().asOWLNamedIndividual()));
					}
				}
			}
			ontoEWAHStore.setDirectIndividuals(clsIndex, individualInts);

		}

        // populate frequency-awareness map
        individualToWeightedDirectTypeMap = new HashMap<>();
		for (OWLNamedIndividual i : individualsInSignature) {
			int individualIndex = getIndex(i);
			// LOG.info("String inferences for "+i+" --> " +individualIndex);
			ontoEWAHStore.setDirectTypes(individualIndex, getIntegersForClassSet(owlReasoner.getTypes(i, true)));
			ontoEWAHStore.setTypes(individualIndex, getIntegersForClassSet(owlReasoner.getTypes(i, false)));

      // TODO - ensure robust for equivalent individuals
      Map<Integer, Integer> wmap = new HashMap<>();
      individualToWeightedDirectTypeMap.put(individualIndex, wmap);
      for (OWLClassAssertionAxiom caax : owlOntology.getClassAssertionAxioms(i)) {
          int cix;

          // only associations to named classes
          if (caax.getClassExpression().isAnonymous()) {
              continue;
          }
          cix = getIndex(caax.getClassExpression().asOWLClass());

          // we use reification to store probability
          for (OWLAnnotation ann : caax.getAnnotations()) {
              OWLAnnotationProperty prop = ann.getProperty();
              OWLAnnotationValue v = ann.getValue();
              if (v instanceof OWLLiteral) {
                  OWLLiteral lv = v.asLiteral().get();
                  Double pr = null;
                  if (lv.isDouble()) {
                      pr = lv.parseDouble();
                  }
                  if (lv.isFloat()) {
                      pr = (double) lv.parseFloat();
                  }
                  if (pr != null) {
                      // TODO : decide on a vocabulary
                      if (prop.getIRI().toString().contains("probability")) {
                          wmap.put(cix, (int) (pr * 100));
                      }
                  }
                  if (lv.isInteger()) {
                      if (prop.getIRI().toString().contains("frequency")) {
                          wmap.put(cix, lv.parseInteger());
                      }

                  }
              }
          }
      }

      // Treat ClassAssertion( ComplementOf(c) i) as a negative assertion
      Set<Integer> ncs = new HashSet<Integer>();
      Set<Integer> ncsDirect = new HashSet<Integer>();
			for (OWLClassAssertionAxiom cx : owlOntology.getClassAssertionAxioms(i)) {
				// TODO: investigate efficiency - number of items set may be high
				if (cx.getClassExpression() instanceof OWLObjectComplementOf) {
					OWLObjectComplementOf nx = (OWLObjectComplementOf) (cx.getClassExpression());
					OWLClassExpression nc = nx.getOperand();
					ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(nc, false)));
					ncs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(nc)));
					ncsDirect.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(nc)));
				}
			}

			// Populate negative assertions from DisjointClasses axioms
			for (OWLClass c : owlReasoner.getTypes(i, false).getFlattened()) {
				LOG.debug("TESTING FOR DCs: " + c);
				if (opposingClassMap.containsKey(c)) {
					for (OWLClassExpression dc : opposingClassMap.get(c)) {
						LOG.info(i + " Type: " + c + " DisjointWith: " + dc);
						ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(dc, false)));
						ncs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc)));
						ncsDirect.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc)));
					}
				}
				/*
				 * for (OWLDisjointClassesAxiom dca : owlOntology.getDisjointClassesAxioms(c)) {
				 * for (OWLClassExpression dc : dca.getClassExpressionsMinus(c)) {
				 * LOG.info(i+" Type: "+c+" DisjointWith: "+dc);
				 * ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(dc, false)));
				 * ncs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc)));
				 * ncsDirect.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc))); } }
				 * for (OWLAnnotationAssertionAxiom aaa : owlOntology.getAnnotationAssertionAxioms(c.getIRI())){
				 * // RO_0002604 is-opposite-of. TODO - use a vocabulary object if
				 * (aaa.getProperty().getIRI().toString().equals("http://purl.obolibrary.org/obo/RO_0002604" )) {
				 * OWLAnnotationValue v = aaa.getValue(); if (v instanceof IRI){
				 * IRI dciri = (IRI)v; OWLClass dc = owlOntology.getOWLOntologyManager().getOWLDataFactory(). getOWLClass(dciri);
				 * ncs.addAll(getIntegersForClassSet(owlReasoner.getSubClasses(dc, false)));
				 * ncs.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc)));
				 * ncsDirect.add(getIndexForClassNode(owlReasoner.getEquivalentClasses(dc)));
				 *
				 * } } }
				 */
			}

			ontoEWAHStore.setNegatedTypes(individualIndex, ncs); // TODO - determine if storing all
																	// inferred negated types is too
																	// inefficient
			ontoEWAHStore.setDirectNegatedTypes(individualIndex, ncsDirect);
		}

	}

	// TODO
	private void storeIndividualProperties() {
		for (OWLNamedIndividual i : individualsInSignature) {
			for (OWLIndividualAxiom ax : owlOntology.getAxioms(i)) {
				if (ax instanceof OWLObjectPropertyAssertionAxiom) {
					OWLObjectPropertyExpression p = ((OWLObjectPropertyAssertionAxiom) ax).getProperty();
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
		ResultSet results = qexec.execSelect();
		for (; results.hasNext();) {
			QuerySolution soln = results.nextSolution();
			RDFNode x = soln.get("varName"); // Get a result variable by name.
			Resource r = soln.getResource("VarR"); // Get a result variable - must be a resource
			Literal l = soln.getLiteral("VarL"); // Get a result variable - must be a literal
		}
	}

	private Set<Integer> getIntegersForClassSet(NodeSet<OWLClass> nodeset) {
		Set<Integer> bits = new HashSet<>();
		for (Node<OWLClass> n : nodeset.getNodes()) {
			if (n.contains(getOWLNothing()))
				continue;
			bits.add(getIndexForClassNode(n));
		}
		return bits;
	}

	private Set<Integer> getIntegersForIndividualSet(NodeSet<OWLNamedIndividual> nodeset) {
		Set<Integer> bits = new HashSet<>();
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
	private int getIndex(OWLClass c) {
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
	 * @param cix
	 * @return bitmap
	 */
	public EWAHCompressedBitmap getDirectIndividualsBM(int cix) {
		return ontoEWAHStore.getDirectIndividuals(cix);
	}

	@Override
	public EWAHCompressedBitmap getIndividualsBM(String classId) {
		return getIndividualsBM(getClassIndex(classId));
	}

	@Override
	public EWAHCompressedBitmap getIndividualsBM(int classIndex) {
		if (classIndex == getRootIndex()) {
			EWAHCompressedBitmap indsBM = new EWAHCompressedBitmap();
			indsBM.setSizeInBits(getIndividualIdsInSignature().size(), true);
			return indsBM;
		}
		EWAHCompressedBitmap subsBM = getSubClasses(classIndex);
		EWAHCompressedBitmap indsBM = null;
		// Note this implementation iterates through all subclasses
		// combining individuals; it is too expensive to store all inferred inds by class
		for (int subcix : subsBM.getPositions()) {
			EWAHCompressedBitmap bm = getDirectIndividualsBM(subcix);
			if (indsBM == null) {
				indsBM = bm;
			} else {
				indsBM = indsBM.or(bm);
			}
		}
		return indsBM;
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
		return getShortForm(c.getIRI());
	}

	public Set<String> getClassIds(int index) {
		Node<OWLClass> n = getClassNode(index);
		Set<String> cids = new HashSet<>();
		for (OWLClass c : n.getEntities()) {
			cids.add(getShortForm(c.getIRI()));
		}
		return cids;
	}

	public Set<String> getClassIds(EWAHCompressedBitmap bm) {
		Set<String> cids = new HashSet<>();
		for (int x : bm) {
			Node<OWLClass> n = getClassNode(x);
			for (OWLClass c : n.getEntities()) {
				cids.add(getShortForm(c.getIRI()));
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
	private Integer getIndexForClassNode(Node<OWLClass> n) {
		Preconditions.checkNotNull(n);
		if (!classNodeToIntegerMap.containsKey(n))
			LOG.error("No such node: " + n);
		return classNodeToIntegerMap.get(n);
	}

	/**
	 * Each individual is mapped to an integer
	 * 
	 * Note that individuals that stand in a SameAs relationship to one another will be mapped to the
	 * same integer
	 * 
	 * @param i
	 * @return integer representation of individual
	 */
	private int getIndex(OWLNamedIndividual i) {
		return getIndexForIndividualNode(individualToNodeMap.get(i));
	}

	/**
	 * Each set of same individuals (an individual node) is mapped to a unique integer
	 * 
	 * @param n
	 * @return integer representation of class node
	 */
	private int getIndexForIndividualNode(Node<OWLNamedIndividual> n) {
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
		Set<Integer> classIndices = new HashSet<>();
		for (OWLClass c : clsSet) {
			classIndices.add(getIndex(c));
		}
		return ontoEWAHStore.getSuperClasses(classIndices);
	}

  /* (non-Javadoc)
 * @see org.monarchinitiative.owlsim.kb.BMKnowledgeBase#getSuperClassesBM(com.googlecode.javaewah.EWAHCompressedBitmap)
 */
  public EWAHCompressedBitmap getSuperClassesBM(EWAHCompressedBitmap classesBM) {
      return ontoEWAHStore.getSuperClasses(new HashSet<>(classesBM.getPositions()));
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

	public EWAHCompressedBitmap getClassesBM(Set<String> classIds) {
		EWAHCompressedBitmap bm = new EWAHCompressedBitmap();
		for (String id : classIds) {
			bm.set(getClassIndex(id));
		}
		return bm;
	}

	public EWAHCompressedBitmap getDirectSuperClassesBM(int classIndex) {
		return ontoEWAHStore.getDirectSuperClasses(classIndex);
	}

	public EWAHCompressedBitmap getSubClasses(int classIndex) {
		return ontoEWAHStore.getSubClasses(classIndex);
	}

	public EWAHCompressedBitmap getDirectSubClassesBM(String cid) {
		return ontoEWAHStore.getDirectSubClasses(getClassIndex(cid));
	}

	public EWAHCompressedBitmap getDirectSubClassesBM(int classIndex) {
		return ontoEWAHStore.getDirectSubClasses(classIndex);
	}

	/**
	 * @param clsIds
	 * @return union of all subClasses (direct and indirect) of any input class
	 */
	public EWAHCompressedBitmap getSubClassesBM(Set<String> clsIds) {
		Set<Integer> classIndices = new HashSet<>();
		for (String id : clsIds) {
			classIndices.add(getClassIndex(id));
		}
		return ontoEWAHStore.getSubClasses(classIndices);
	}

	/**
	 * @param clsIds
	 * @return union of all direct subClasses of all input classes
	 */
	public EWAHCompressedBitmap getDirectSubClassesBM(Set<String> clsIds) {
		Set<Integer> classIndices = new HashSet<>();
		for (String id : clsIds) {
			classIndices.add(getClassIndex(id));
		}
		return ontoEWAHStore.getDirectSubClasses(classIndices);
	}

	/**
	 * @param clsIds
	 * @return union of all superClasses (direct and indirect) of any input class
	 */
	public EWAHCompressedBitmap getSuperClassesBM(Set<String> clsIds) {
		Set<Integer> classIndices = new HashSet<>();
		for (String id : clsIds) {
			classIndices.add(getClassIndex(id));
		}
		return ontoEWAHStore.getSuperClasses(classIndices);
	}

    /**
	 * @param clsIds
	 * @return union of all direct superClasses of all input classes
	 */
	public EWAHCompressedBitmap getDirectSuperClassesBM(Set<String> clsIds) {
		Set<Integer> classIndices = new HashSet<Integer>();
		for (String id : clsIds) {
			classIndices.add(getClassIndex(id));
		}
		return ontoEWAHStore.getDirectSuperClasses(classIndices);
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
	 * @return Bitmap representation of the subset of direct types of i, which
	 *         are descendants of classFilter
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
	 * @param individualIndex
	 * @return bitmap representation of all (direct and indirect) instantiated classes
	 */
	public EWAHCompressedBitmap getTypesBM(int individualIndex) {
		return ontoEWAHStore.getTypes(individualIndex);
	}

  /* (non-Javadoc)
   * @see org.monarchinitiative.owlsim.kb.BMKnowledgeBase#getDirectWeightedTypes(java.lang.String)
   */
  public Map<Integer, Integer> getDirectWeightedTypes(String id) {
      int iix = getIndividualIndex(id);
      return individualToWeightedDirectTypeMap.get(iix);
  }


	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) classes known to be NOT instantiated
	 */
	public EWAHCompressedBitmap getNegatedTypesBM(String id) {
		Preconditions.checkNotNull(id);
		return ontoEWAHStore.getNegatedTypes(getIndividualIndex(id));
	}

	/**
	 * @param id
	 * @return bitmap representation of all (direct and indirect) classes known to be NOT instantiated
	 */
	public EWAHCompressedBitmap getDirectNegatedTypesBM(String id) {
		Preconditions.checkNotNull(id);
		return ontoEWAHStore.getDirectNegatedTypes(getIndividualIndex(id));
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
	 * @return bitmap representation of all (direct and indirect) instantiated classes that are
	 * subclasses of classId
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
	 * @param id CURIE-style
	 * @return OWLAPI Class object
	 */
	private OWLClass getOWLClass(String id) {
		Preconditions.checkNotNull(id);
		return getOWLClass(IRI.create(curieUtil.getIri(id).orElse(id)));
	}

	/**
	 * @param iri
	 * @return OWLAPI Class object
	 */
	private OWLClass getOWLClass(IRI iri) {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLClass(iri);
	}

	/**
	 * @param iri
	 * @return OWLAPI Class object
	 */
	private OWLNamedIndividual getOWLNamedIndividual(IRI iri) {
		return owlOntology.getOWLOntologyManager().getOWLDataFactory().getOWLNamedIndividual(iri);
	}

	/**
	 * @param id
	 *            CURIE-style
	 * @return OWLAPI Class object
	 */
	private OWLNamedIndividual getOWLNamedIndividual(String id) {
		Preconditions.checkNotNull(id);
		return getOWLNamedIndividual(IRI.create(curieUtil.getIri(id).orElse(id)));
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
			return new HashSet<>(m.get(property));
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
		return getShortForm(ind.getIRI());
	}

	@Override
	public EWAHCompressedBitmap getFilteredTypesBM(Set<String> ids, String classId) {

		Set<Integer> classBits = new HashSet<>();
		for (String id : ids) {
			classBits.add(this.getClassIndex(id));
		}

		return ontoEWAHStore.getTypes(classBits, getClassIndex(classId));

	}

	public EWAHCompressedBitmap getFilteredDirectTypesBM(Set<String> classIds, String classId) {

		Set<Integer> classBits = new HashSet<>();
		for (String id : classIds) {
			classBits.add(this.getClassIndex(id));
		}

		return ontoEWAHStore.getDirectTypes(classBits, getClassIndex(classId));

	}

	@Override
	public String resolveIri(String entity) {
		return curieUtil.getIri(entity).orElse(entity);
	}

}
