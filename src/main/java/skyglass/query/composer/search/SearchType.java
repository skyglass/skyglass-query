package skyglass.query.composer.search;

public enum SearchType {

	IgnoreCase, StartsIgnoreCase, CaseSensitive;

	public static boolean isIgnoreCase(SearchType searchType) {
		return searchType != CaseSensitive;
	}

}
