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
		if (this == Operator.Like) return "LIKE";
		return this.symbol;
	}


}
