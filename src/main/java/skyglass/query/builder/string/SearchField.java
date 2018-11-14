package skyglass.query.builder.string;

import skyglass.query.builder.FieldResolver;

public class SearchField {

	private FieldResolver fieldResolver;

	private String paramName;

	private SearchType searchType;

	private boolean translatable;

	private String lang;

	public SearchField(FieldResolver fieldResolver, String paramName, SearchType searchType, boolean translatable, String lang) {
		this.fieldResolver = fieldResolver;
		this.paramName = paramName;
		this.searchType = searchType;
		this.translatable = translatable;
		this.lang = lang;
	}

	public FieldResolver getFieldResolver() {
		return fieldResolver;
	}

	public String getParamName() {
		return paramName;
	}

	public SearchType getSearchType() {
		return searchType;
	}

	public boolean isMultiple() {
		return fieldResolver.isMultiple();
	}

	public boolean isSingle() {
		return fieldResolver.isSingle();
	}

	public boolean isTranslatable() {
		return translatable;
	}

	public void setTranslatable(boolean translatable) {
		this.translatable = translatable;
	}

	public boolean isIgnoreCase() {
		return SearchType.isIgnoreCase(searchType);
	}

	public boolean isStartsWith() {
		return SearchType.isStartsWith(searchType);
	}

	public String getLang() {
		return lang;
	}

}
