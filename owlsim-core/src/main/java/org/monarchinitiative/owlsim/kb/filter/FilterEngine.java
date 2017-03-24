package org.monarchinitiative.owlsim.kb.filter;

import com.googlecode.javaewah.EWAHCompressedBitmap;
import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Applies a {@link Filter} on a set of individuals.
 * 
 * @author cjm
 *
 */
public class FilterEngine {
	
	private Logger LOG = Logger.getLogger(FilterEngine.class);

	BMKnowledgeBase knowledgeBase;
	//private Logger LOG = Logger.getLogger(FilterEngine.class);
	

	
	private FilterEngine(BMKnowledgeBase knowledgeBase) {
		super();
		this.knowledgeBase = knowledgeBase;
	}

	/**
	 * @param knowledgeBase
	 * @return FilterEngine
	 */
	public static FilterEngine create(BMKnowledgeBase knowledgeBase) {
		return new FilterEngine(knowledgeBase);
	}
	
	/**
	 * @param filter
	 * @return individual ids
	 * @throws UnknownFilterException 
	 */
	public List<String> applyFilter(Filter filter) throws UnknownFilterException {
		if (filter == null) {
			return new ArrayList<String>(knowledgeBase.getIndividualIdsInSignature());
		}
		List<String> ids = new ArrayList<String>();
		for (String id : knowledgeBase.getIndividualIdsInSignature()) {
			if (test(id, filter)) {
				ids.add(id);
			}
		}
		if (filter instanceof AnonIndividualFilter) {
		    // always include anon individuals
		    ids.add(((AnonIndividualFilter)filter).toString());
		}
		return ids;	
	}

	// returns true if id has properties that match Filter
	private boolean test(String id, Filter filter) throws UnknownFilterException {
		if (filter instanceof FilterSet) {
			FilterSet fs = (FilterSet)filter;
			for (Filter f2 : fs.getFilters()) {
				if (!test(id, f2)) {
					return false;
				}
			}
			return true;
		}
		else if (filter instanceof PropertyValueFilter) {
			PropertyValueFilter fpv = (PropertyValueFilter)filter;
			Set<Object> values = knowledgeBase.getPropertyValues(id, fpv.getPropertySymbol());
			LOG.info(id+ " VALs="+values);
			boolean contains = false;
			if (values.contains(fpv.getFiller()))
				contains = true;
			if (fpv.isNegated())
				return !contains;
			else
				return contains;
		}
		else if (filter instanceof TypeFilter) {
		    // TODO : provide a convenience method in kb
		    TypeFilter tf = (TypeFilter)filter;
		    EWAHCompressedBitmap typesBM;
            if (tf.isExact()) {
                typesBM = knowledgeBase.getDirectTypesBM(id);
            }
            else {
                typesBM = knowledgeBase.getTypesBM(id);
            }
//            LOG.info("typeId = " + tf.getTypeId());
            int ix = knowledgeBase.getClassIndex(tf.getTypeId());
            return typesBM.getPositions().contains(ix) ^ tf.isNegated();
		}
        else if (filter instanceof IdFilter) {
            IdFilter idf = (IdFilter)filter;
            if (idf.getIds().contains(id)) {
                return true;
            } else {
                return false;
            }
        }
        else if (filter instanceof IdPrefixFilter) {
            IdPrefixFilter idf = (IdPrefixFilter)filter;
            if (id.startsWith(idf.getPrefix())) {
                return true;
            } else {
                return false;
            }
        }
        else if (filter instanceof AnonIndividualFilter) {
            return false;
        }
		else {
			throw new UnknownFilterException(filter.toString());
		}
	}

}
