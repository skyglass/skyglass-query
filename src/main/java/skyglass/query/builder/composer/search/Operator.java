package skyglass.query.builder.composer.search;

public enum Operator {
	
	More(">"), Less("<"), Like(":"), Equal("=");
	
	private String symbol;
	
	private Operator(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
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
	
	public String getSqlOperator() {
		if (isText()) return "LIKE";
		return this.symbol;
	}
	
	public boolean isText() {
		return this == Operator.Like;
	}
	
	public boolean isInteger() {
		return this == Operator.Less || this == Operator.More;
	}


}
