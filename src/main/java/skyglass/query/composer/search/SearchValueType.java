package skyglass.query.composer.search;

public enum SearchValueType {

	Text, Integer;

	public boolean isNumeric() {
		return this == Integer;
	}

}
