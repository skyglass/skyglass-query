package skyglass.query.builder.composer;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class QueryParamBuilder {
	
	protected Collection<QueryParam> params = new ArrayList<>();
	
	boolean hasEmptyParamValue() {
		if (params == null) {
			return false;
		}
		for (QueryParam queryParam : params) {
			if (isEmpty(queryParam.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	protected boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (isCollection(value) && CollectionUtils.isEmpty((Collection<?>) value)) {
			return true;
		}
		if (value instanceof String) {
			return isStringEmpty((String) value);
		}
		return false;
	}

	boolean isCollection(Object value) {
		return value instanceof Collection;
	}

	protected boolean isStringEmpty(String value) {
		return StringUtils.isBlank((String) value);
	}
	
	@SuppressWarnings("rawtypes")
	void _doSetParameters(QueryComposer root, String name, Collection values) {
		if (CollectionUtils.isNotEmpty(values)) {
			if (root.isNativeQuery()) {
				int index = 1;
				for (Object value : values) {
					params.add(QueryParam.create(name + Integer.toString(index), value));
					index++;
				}
			} else {
				params.add(QueryParam.create(name, values));
			}
		} else {
			// should add null parameter explicitly, otherwise isFalseCondition()
			// will return false, and the query part will be appended
			params.add(QueryParam.create(name, null));
		}
	}
	
	void _doSetParameter(String name, Object value) {
		params.add(QueryParam.create(name, value));
	}
	
	boolean setNonEmptyParams(QueryComposer root, boolean wherePart) {
		boolean addWhere = false;
		for (QueryParam queryParam : params) {
			if (!isEmpty(queryParam.getValue())) {
				root.setParam(queryParam);
				if (!root.hasCustomWherePart() && wherePart) {
					root.setCustomWherePart(true);
					addWhere = true;
				}
			}
		}
		return addWhere;
	}

}
