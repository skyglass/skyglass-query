package skyglass.query.builder.string;

public class QueryParam {

	private String name;

	private Object value;
	
	public static QueryParam create(String name, Object value) {
		if (value instanceof AliasType) {
			return new AliasParam(name);
		}
		return new QueryParam(name, value);
	}

	protected QueryParam(String name, Object value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Object getValue() {
		return value;
	}

}
