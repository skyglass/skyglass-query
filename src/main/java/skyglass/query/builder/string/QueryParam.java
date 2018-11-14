package skyglass.query.builder.string;

public class QueryParam {

	private String name;

	private Object value;

	public QueryParam(String name, Object value) {
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
