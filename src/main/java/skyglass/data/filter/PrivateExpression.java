package skyglass.data.filter;

public class PrivateExpression {
	
	private String path;
	
	private ExpressionType expressionType;
	
	public PrivateExpression(String path) {
		this(path, ExpressionType.Property);
	}

	public PrivateExpression(String path, ExpressionType expressionType) {
		this.path = path;
		this.expressionType = expressionType;
	}

	public String getPath() {
		return path;
	}

	public ExpressionType getExpressionType() {
		return expressionType;
	}

}
