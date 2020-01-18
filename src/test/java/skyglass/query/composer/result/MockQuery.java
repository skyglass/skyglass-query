package skyglass.query.composer.result;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import skyglass.query.composer.QueryParam;

public class MockQuery {

	private Map<String, QueryParam> values = new LinkedHashMap<>();

	public void setParameter(String name, Object value) {
		this.values.put(name, QueryParam.create(name, value));
	}

	public Collection<QueryParam> getParams() {
		return values.values();
	}

}
