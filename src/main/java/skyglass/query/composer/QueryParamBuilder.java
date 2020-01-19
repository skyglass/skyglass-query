package skyglass.query.composer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class QueryParamBuilder {

	private Map<String, QueryParam> params = new HashMap<>();

	boolean hasEmptyParamValue() {
		if (params == null) {
			return false;
		}
		for (QueryParam queryParam : params.values()) {
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
		return StringUtils.isBlank(value);
	}

	@SuppressWarnings("rawtypes")
	void _doSetParameters(QueryComposer root, String name, Collection values) {
		if (CollectionUtils.isNotEmpty(values)) {
			if (root.isNativeQuery()) {
				int index = 1;
				for (Object value : values) {
					String paramName = name + Integer.toString(index);
					params.put(paramName, QueryParam.create(paramName, value));
					index++;
				}
			} else {
				params.put(name, QueryParam.create(name, values));
			}
		} else {
			// should add null parameter explicitly, otherwise isFalseCondition()
			// will return false, and the query part will be appended
			params.put(name, QueryParam.create(name, null));
		}
	}

	void _doSetParameterValue(String name, Object value) {
		params.put(name, QueryParam.create(name, value));
	}

	void _doSetQueryParam(String name, QueryParam value) {
		params.put(name, value);
	}

	void setNonEmptyParams(QueryComposer root, boolean wherePart) {
		for (QueryParam queryParam : params.values()) {
			if (!isEmpty(queryParam.getValue())) {
				root.setParam(queryParam);
				if (!root.hasCustomWherePart() && wherePart) {
					root.setCustomWherePart(true);
				}
			}
		}
	}

	protected Collection<QueryParam> _getParams() {
		return params.values();
	}

}
