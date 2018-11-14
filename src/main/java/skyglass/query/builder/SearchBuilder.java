package skyglass.query.builder;

import java.util.List;

import skyglass.query.builder.string.QueryRequestDTO;
import skyglass.query.builder.string.SearchField;
import skyglass.query.builder.string.SearchType;
import skyglass.query.builder.string.StringPartBuilder;

public class SearchBuilder {

	private List<SearchField> searchFields;

	private QueryRequestDTO queryRequest;

	private SearchType searchType;

	public SearchBuilder(QueryRequestDTO queryRequest, SearchType searchType) {
		this.searchType = searchType;
		this.queryRequest = queryRequest;
	}

	public SearchBuilder(QueryRequestDTO queryRequest, String... searchFields) {
		this(queryRequest, SearchType.IgnoreCase);
		this.searchFields.add(new SearchField(new FieldResolver(queryRequest, searchFields), StringPartBuilder.SEARCH_TERM_FIELD, searchType, false, queryRequest.getLang()));
	}

	public SearchBuilder(QueryRequestDTO queryRequest, boolean translatable, String... searchFields) {
		this(queryRequest, SearchType.IgnoreCase);
		this.searchFields.add(new SearchField(new FieldResolver(queryRequest, searchFields), StringPartBuilder.SEARCH_TERM_FIELD, searchType, translatable, queryRequest.getLang()));
	}

	public SearchBuilder(QueryRequestDTO queryRequest, SearchType searchType, boolean translatable, String... searchFields) {
		this(queryRequest, searchType);
		this.searchFields.add(new SearchField(new FieldResolver(queryRequest, searchFields), StringPartBuilder.SEARCH_TERM_FIELD, searchType, translatable, queryRequest.getLang()));
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
		this.searchFields.add(new SearchField(new FieldResolver(queryRequest, searchFields), StringPartBuilder.SEARCH_TERM_FIELD, searchType, translatable, queryRequest.getLang()));
		return this;
	}

	public List<SearchField> getSearchFields() {
		return searchFields;
	}

}
