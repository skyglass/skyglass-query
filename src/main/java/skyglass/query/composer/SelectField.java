package skyglass.query.composer;

public class SelectField {

	private String alias;

	private String expression;

	public SelectField(String alias, String expression) {
		this.alias = alias;
		this.expression = expression;
	}

	public String getAlias() {
		return alias;
	}

	public String getExpression() {
		return expression;
	}

}
