package skyglass.query.builder.string;

public enum SearchType {

	IgnoreCase, StartsIgnoreCase;

	public static boolean isIgnoreCase(SearchType searchType) {
		return true;
	}

	public static boolean isStartsWith(SearchType searchType) {
		return searchType == SearchType.StartsIgnoreCase;
	}

	public static String getExpression(SearchType searchType, String value) {
		return isStartsWith(searchType) ? ("%" + value) : ("%" + value + "%");
	}

}
