package skyglass.query.composer.search;

public enum SearchOperator {

	More(">", ">="), //
	Less("<", "<="), //
	Like(":", "LIKE"), // 
	Equal("=", "="), //
	Negation("!", "!="), //
	StartsWith(":", "LIKE"), //
	EndsWith(":", "LIKE"), //
	Contains(":", "LIKE");

	public static final String[] SIMPLE_OPERATION_SET = { ":", "!", ">", "<", "=" };

	public static final String ZERO_OR_MORE_REGEX = "*";

	private String symbol;

	private String sqlOperator;

	private SearchOperator(String symbol, String sqlOperator) {
		this.symbol = symbol;
		this.sqlOperator = sqlOperator;
	}

	public String getSymbol() {
		return symbol;
	}

	public String getSqlOperator() {
		return this.sqlOperator;
	}

	public static SearchOperator from(String symbol) {
		if (More.symbol.equals(symbol)) {
			return More;
		}
		if (Less.symbol.equals(symbol)) {
			return Less;
		}
		if (Like.symbol.equals(symbol)) {
			return Like;
		}
		if (Negation.symbol.equals(symbol)) {
			return Negation;
		}
		return Equal;
	}

	public boolean isText() {
		return this == SearchOperator.Like
				|| this == SearchOperator.StartsWith
				|| this == SearchOperator.EndsWith
				|| this == SearchOperator.Contains
				|| this == SearchOperator.Equal;
	}

	public boolean isInteger() {
		return this == SearchOperator.Less || this == SearchOperator.More;
	}

}
