package skyglass.query.composer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import skyglass.query.composer.search.SearchField;
import skyglass.query.composer.search.SearchPath;
import skyglass.query.composer.search.SearchTerm;
import skyglass.query.composer.search.SearchType;

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

	private static SearchPath[] toSearchPath(String[] arr) {
		return Arrays.asList(arr).stream().map(s -> new SearchPath(s, s)).collect(Collectors.toList()).toArray(new SearchPath[0]);
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, SEARCH_TERM_PARAM_NAME, false, toSearchPath(searchFields));
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, String paramName, boolean translatable, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, paramName, translatable, toSearchPath(searchFields));
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm, boolean translatable, String... searchFields) {
		this(root, queryRequest, searchTerm, SearchType.IgnoreCase, SEARCH_TERM_PARAM_NAME, translatable, toSearchPath(searchFields));
	}

	public SearchBuilder(QueryComposer root, QueryRequestDTO queryRequest, SearchTerm searchTerm,
			SearchType searchType, String paramName, boolean translatable, SearchPath... searchPaths) {
		this.searchType = searchType;
		this.queryRequest = queryRequest;
		this.paramName = paramName;
		this.searchTerm = searchTerm;
		if (searchTerm.hasField()) {
			for (SearchPath searchPath : searchPaths) {
				if (searchPath.getAlias().equals(searchTerm.getAlias())
						|| searchPath.getPath().equals(searchTerm.getAlias())) {
					if (root != null) {
						root.setSearchParameter(paramName, searchTerm, searchType);
					}
					this.searchFields.add(new SearchField(searchTerm, new FieldResolver(searchPath.getPath()), paramName, searchType, translatable, queryRequest.getLang()));
				}
			}
		} else {
			if (root != null) {
				root.setSearchParameter(paramName, searchTerm, searchType);
			}
			this.searchFields.add(new SearchField(searchTerm, new FieldResolver(
					Arrays.asList(searchPaths).stream().map(s -> s.getPath()).collect(Collectors.toList()).toArray(new String[0])),
					paramName, searchType, translatable, queryRequest.getLang()));
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
		this.searchFields.add(new SearchField(searchTerm, new FieldResolver(searchFields), paramName, searchType, translatable, queryRequest.getLang()));
		return this;
	}

	public SearchField[] getSearchFields() {
		return searchFields.toArray(new SearchField[0]);
	}

	public SearchTerm getSearchTerm() {
		return searchTerm;
	}

	public boolean isNotEmpty() {
		return searchFields.size() > 0;
	}

}
