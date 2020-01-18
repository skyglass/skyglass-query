package skyglass.query.composer;

import skyglass.query.composer.search.SearchTerm;

public enum SearchType {

	IgnoreCase, StartsIgnoreCase;

	public static boolean isIgnoreCase(SearchType searchType) {
		return true;
	}

	public static Object getExpression(SearchTerm searchTerm, SearchType searchType) {
		if (!searchTerm.getOperator().isText()) {
			return searchTerm.getValue();
		}
		return searchType == SearchType.StartsIgnoreCase ? ("%" + searchTerm.getValue()) : ("%" + searchTerm.getValue() + "%");
	}

}
