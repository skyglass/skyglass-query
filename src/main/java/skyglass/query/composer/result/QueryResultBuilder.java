package skyglass.query.composer.result;

import java.util.Collections;
import java.util.List;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryRequestDTO;
import skyglass.query.composer.QueryResult;

public class QueryResultBuilder<T> {

	private boolean nativeQuery;

	private boolean distinct;

	private int rowsPerPage;

	private int pageNumber;

	private int offset;

	private int limit;

	private int totalResults;

	private QueryComposer queryComposer;

	private QueryResultProvider<T> queryResultProvider;

	private QueryRequestDTO queryRequest;

	public QueryResultBuilder(QueryComposer queryComposer, QueryResultProvider<T> queryResultProvider) {
		this.queryComposer = queryComposer;
		this.queryResultProvider = queryResultProvider;
		this.queryRequest = queryComposer.getQueryRequest();
		if (this.queryRequest != null) {
			this.rowsPerPage = queryRequest.getRowsPerPage();
			this.pageNumber = queryRequest.getPageNumber();
			this.offset = queryRequest.getOffset();
			this.limit = queryRequest.getLimit();
		}
		this.distinct = queryComposer.isDistinct();
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
		return queryComposer;
	}

	public List<T> getFullResult() {
		List<T> result = queryResultProvider.getUnpagedResult(queryComposer);
		return result;
	}

	private QueryResult<T> pagedResult() {
		int totalCount = returnTotalCount() ? queryResultProvider.getTotalCount(queryComposer) : -1;
		QueryResult<T> result = new QueryResult<T>();
		result.setTotalCount(totalCount);
		if (totalCount == 0) {
			result.setResult(Collections.emptyList());
			return result;
		}
		int firstResult = getFirstResult();
		if (distinct) {
			List<String> uuidList = queryResultProvider.getUuidList(queryComposer, firstResult, getLimit());
			result.setResult(queryResultProvider.getResult(queryComposer, uuidList));
		} else {
			result.setResult(queryResultProvider.getPagedResult(queryComposer, firstResult, getLimit()));
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
		return queryResultProvider.getTotalCount(queryComposer);
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
