package skyglass.query.builder.composer;

import skyglass.query.builder.SearchType;

public class SearchParameter {

	private String paramName;

	private String value;

	private SearchType searchType;

	public SearchParameter(String paramName, String value, SearchType searchType) {
		this.paramName = paramName;
		this.value = value;
		this.searchType = searchType;
	}

	public String getParamName() {
		return paramName;
	}

	public String getValue() {
		return value;
	}

	public SearchType getSearchType() {
		return searchType;
	}

}
