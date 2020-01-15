package skyglass.query.builder;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.builder.composer.QueryComposer;
import skyglass.query.builder.composer.search.SearchTerm;

/**
 * This class allows to build search fields from QueryRequestDTO in a declarative way.
 * Each SearchField class contains information on how to build correspondent SQL LIKE part in WHERE clause
 * 
 */
public class SearchBuilder {

	public static final String SEARCH_TERM_PARAM_NAME = "searchTerm";

	private List<SearchField> searchFields = new ArrayList<>();
	
	private QueryRequestDTO queryRequest;

	private SearchType searchType;
	
	private String paramName;
	
	private SearchTerm searchTerm;

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, SEARCH_TERM_PARAM_NAME, false, searchFields);
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, String paramName, boolean translatable, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, paramName, translatable, searchFields);
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, boolean translatable, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, SEARCH_TERM_PARAM_NAME, translatable, searchFields);
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, SearchType searchType, String paramName, boolean translatable, String... searchFields) {
		this.searchType = searchType;
		this.queryRequest = queryRequest;
		this.paramName = paramName;
		this.searchTerm = searchTerm;
		if (searchTerm.hasField()) {
			for (String searchField: searchFields) {
				if (searchField.equals(searchTerm.getField())) {
					if (root != null) {
						root.setSearchParameter(paramName, searchTerm.getValue(), searchType);
					}
					this.searchFields.add(new SearchField(searchTerm.getOperator(), new FieldResolver(searchField), paramName, searchType, translatable, queryRequest.getLang()));					
				}
			}
		} else {
			if (root != null) {
				root.setSearchParameter(paramName, searchTerm.getValue(), searchType);
			}
			this.searchFields.add(new SearchField(searchTerm.getOperator(), new FieldResolver(searchFields), paramName, searchType, translatable, queryRequest.getLang()));
		}
	}

	public SearchBuilder addSearch(String... searchFields) {
		return addSearch(searchType, false, searchFields);
	}

	public SearchBuilder addTranslatableSearch(String... searchFields) {
		return addSearch(searchType, true, searchFields);
	}

	public SearchBuilder addSearch(SearchType searchType, String... searchFields) {
		return addSearch(searchType, false, searchFields);
	}

	public SearchBuilder addTranslatableSearch(SearchType searchType, String... searchFields) {
		return addSearch(searchType, true, searchFields);
	}

	private SearchBuilder addSearch(SearchType searchType, boolean translatable, String... searchFields) {
		this.searchFields.add(new SearchField(searchTerm.getOperator(), new FieldResolver(searchFields), paramName, searchType, translatable, queryRequest.getLang()));
		return this;
	}

	public SearchField[] getSearchFields() {
		return searchFields.toArray(new SearchField[0]);
	}

}
