package skyglass.query.composer.search;

public enum Operator {
	
	More(">", ">="), Less("<", "<="), Like(":", "LIKE"), Equal("=", "=");
	
	private String symbol;
	
	private String sqlOperator;
	
	private Operator(String symbol, String sqlOperator) {
		this.symbol = symbol;
		this.sqlOperator = sqlOperator;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public String getSqlOperator() {
		return this.sqlOperator;
	}
	
	public static Operator from(String symbol) {
		if (More.symbol.equals(symbol)) {
			return More;
		}
		if (Less.symbol.equals(symbol)) {
			return Less;
		}
		if (Like.symbol.equals(symbol)) {
			return Like;
		}
		return Equal;
	}
	
	public boolean isText() {
		return this == Operator.Like;
	}
	
	public boolean isInteger() {
		return this == Operator.Less || this == Operator.More;
	}


}
