package skyglass.query.builder.string;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import skyglass.query.QueryResultUtil;
import skyglass.query.builder.result.QueryResultProvider;

public class JpaQueryResultProvider<T> implements QueryResultProvider<T> {

	private EntityManager entityManager;

	private Class<T> type;

	public JpaQueryResultProvider(EntityManager entityManager, Class<T> type) {
		this.entityManager = entityManager;
		this.type = type;
	}

	@Override
	public List<T> getResult(QueryStringBuilder builder, List<String> uuidList) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.buildResultFromUuidList(uuidList), type);
		setParameters(typedQuery, builder);
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	@Override
	public List<T> getPagedResult(QueryStringBuilder builder, int firstResult, int maxResults) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.build(), type);
		setParameters(typedQuery, builder);
		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(maxResults);
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	@Override
	public List<T> getUnpagedResult(QueryStringBuilder builder) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.build(), type);
		setParameters(typedQuery, builder);
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	@Override
	public int getTotalCount(QueryStringBuilder builder) {
		TypedQuery<Long> typedQuery = entityManager.createQuery(builder.buildCountPart(), Long.class);
		setParameters(typedQuery, builder);
		Long result = QueryResultUtil.getSingleResult(typedQuery);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public List<String> getUuidList(QueryStringBuilder builder, int firstResult, int maxResults) {
		TypedQuery<String> typedQuery = entityManager.createQuery(builder.buildUuidListPart(), String.class);
		setParameters(typedQuery, builder);
		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(maxResults);
		List<String> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	private void setParameters(TypedQuery<?> typedQuery, QueryStringBuilder builder) {
		for (QueryParam queryParam : builder.getParams()) {
			typedQuery.setParameter(queryParam.getName(), queryParam.getValue());
		}
	}

}
