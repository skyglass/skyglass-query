package skyglass.query.builder.result;

import java.util.List;

import skyglass.query.builder.string.QueryStringBuilder;

public interface QueryResultProvider<T> {

	public List<T> getResult(QueryStringBuilder builder, List<String> uuidList);

	public List<T> getPagedResult(QueryStringBuilder builder, int firstResult, int maxResults);

	public List<T> getUnpagedResult(QueryStringBuilder builder);

	public int getTotalCount(QueryStringBuilder builder);

	public List<String> getUuidList(QueryStringBuilder builder, int firstResult, int maxResults);

}
