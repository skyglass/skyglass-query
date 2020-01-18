package skyglass.query.composer.result;

import java.util.List;

import skyglass.query.composer.QueryComposer;

public interface QueryResultProvider<T> {

	public List<T> getResult(QueryComposer builder, List<String> uuidList);

	public List<T> getPagedResult(QueryComposer builder, int firstResult, int maxResults);

	public List<T> getUnpagedResult(QueryComposer builder);

	public int getTotalCount(QueryComposer builder);

	public List<String> getUuidList(QueryComposer builder, int firstResult, int maxResults);

}
