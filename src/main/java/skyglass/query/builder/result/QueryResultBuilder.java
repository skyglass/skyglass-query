package skyglass.query.builder.result;

import java.util.Collections;
import java.util.List;

import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.QueryResult;
import skyglass.query.builder.composer.QueryComposer;

public class QueryResultBuilder<T> {

	private boolean nativeQuery;

	private boolean distinct;

	private int rowsPerPage;

	private int pageNumber;

	private int offset;

	private int limit;

	private int totalResults;

	private QueryComposer queryStringBuilder;

	private QueryResultProvider<T> queryResultProvider;

	private QueryRequestDTO queryRequest;

	public QueryResultBuilder(QueryComposer queryStringBuilder, QueryResultProvider<T> queryResultProvider) {
		this.queryStringBuilder = queryStringBuilder;
		this.queryResultProvider = queryResultProvider;
		this.queryRequest = queryStringBuilder.getQueryRequest();
		if (this.queryRequest != null) {
			this.rowsPerPage = queryRequest.getRowsPerPage();
			this.pageNumber = queryRequest.getPageNumber();
			this.offset = queryRequest.getOffset();
			this.limit = queryRequest.getLimit();
		}
		this.distinct = queryStringBuilder.isDistinct();
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public int getTotalResults() {
		return totalResults;
	}

	public boolean isDistinct() {
		return distinct;
	}

	protected QueryComposer getQueryStringBuilder() {
		return queryStringBuilder;
	}

	public List<T> getFullResult() {
		List<T> result = queryResultProvider.getUnpagedResult(queryStringBuilder);
		return result;
	}

	private QueryResult<T> pagedResult() {
		int totalCount = returnTotalCount() ? queryResultProvider.getTotalCount(queryStringBuilder) : -1;
		QueryResult<T> result = new QueryResult<T>();
		result.setTotalCount(totalCount);
		if (totalCount == 0) {
			result.setResult(Collections.emptyList());
			return result;
		}
		int firstResult = getFirstResult();
		if (distinct) {
			List<String> uuidList = queryResultProvider.getUuidList(queryStringBuilder, firstResult, getLimit());
			result.setResult(queryResultProvider.getResult(queryStringBuilder, uuidList));
		} else {
			result.setResult(queryResultProvider.getPagedResult(queryStringBuilder, firstResult, getLimit()));
		}
		return result;
	}

	protected QueryResult<T> getEmptyResult() {
		QueryResult<T> result = new QueryResult<T>();
		result.setTotalCount(0);
		result.setResult(Collections.emptyList());
		return result;
	}

	public QueryResult<T> getPagedResult() {
		return getResult();
	}

	public QueryResult<T> getResult() {
		return pagedResult();
	}

	public int getTotalCount() {
		return queryResultProvider.getTotalCount(queryStringBuilder);
	}

	public boolean isNativeQuery() {
		return nativeQuery;
	}

	private boolean returnTotalCount() {
		return limit <= 0 && rowsPerPage > 0;
	}

	private int getFirstResult() {
		return pageNumber == -1 ? offset : (getPageNumber() - 1) * getLimit();
	}

	public int getLimit() {
		return rowsPerPage == -1 ? limit : rowsPerPage;
	}

}
