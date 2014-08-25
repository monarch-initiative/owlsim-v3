package org.monarchinitiative.owlsim.kb.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;

import com.googlecode.javaewah.EWAHCompressedBitmap;

/**
 * Applies a filter on a set of individuals
 * 
 * @author cjm
 *
 */
public class FilterEngine {
	
	BMKnowledgeBase knowledgeBase;
	private Logger LOG = Logger.getLogger(FilterEngine.class);

	
	/**
	 * @param filter
	 * @return individual ids
	 * @throws UnknownFilterException 
	 */
	public List<String> applyFilter(Filter filter) throws UnknownFilterException {
		List<String> ids = new ArrayList<String>();
		for (String id : knowledgeBase.getIndividualIdsInSignature()) {
			if (test(id, filter)) {
				ids.add(id);
			}
		}
		return ids;	
	}

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
			boolean contains = false;
			if (values.contains(fpv.getFiller()))
				contains = true;
			if (fpv.isNegated())
				return !contains;
			else
				return contains;
		}
		else if (filter instanceof TypeFilter) {
			//TODO
			//int tix = knowledgeBase.get ((TypeFilter)filter).getTypeId();
			//EWAHCompressedBitmap typeBM = knowledgeBase.getTypesBM(id);
			//if (typeBM.getPositions().contains(filter))
			return false;
		}
		else {
			throw new UnknownFilterException(filter.toString());
		}
	}

}
