package skyglass.query.builder;

import skyglass.query.builder.composer.search.Operator;

/**
 * This class contains information on how to build correspondent LIKE part in WHERE clause
 * (See QuerySearchUtil class, which converts the list of SearchField classes to correspondent LIKE part in WHERE clause)
 * 
 */
public class SearchField {
	
	private Operator operator;

	private FieldResolver fieldResolver;

	private String paramName;

	private SearchType searchType;

	private boolean translatable;

	private String lang;

	public SearchField(Operator operator, FieldResolver fieldResolver, String paramName, 
			SearchType searchType, boolean translatable, String lang) {
		this.operator = operator;
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
		return SearchType.isIgnoreCase(searchType)
				&& (operator == Operator.Like
				 || operator == Operator.Equal);
	}

	public String getLang() {
		return lang;
	}
	
	public String getOperator() {
		return operator.getSqlOperator();
	}

}
