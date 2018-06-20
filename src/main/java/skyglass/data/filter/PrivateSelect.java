package skyglass.data.filter;

public class PrivateSelect extends PrivateExpression {
	
	private String alias;
	
	public PrivateSelect(String path, String alias) {
		this(path, ExpressionType.Property, alias);
	}

	public PrivateSelect(String path, ExpressionType expressionType, String alias) {
		super(path, expressionType);
		this.alias = alias;
	}

	public String getAlias() {
		return alias;
	}

}
