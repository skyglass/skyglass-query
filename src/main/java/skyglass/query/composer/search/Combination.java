package skyglass.query.composer.search;

public enum Combination {
	
	And(","), Or("|");
	
	private String symbol;
	
	private Combination(String symbol) {
		this.symbol = symbol;
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public static Combination from(String symbol) {
		if (And.symbol.equals(symbol)) {
			return And;
		}
		if (Or.symbol.equals(symbol)) {
			return Or;
		}
		return And;
	}


}
