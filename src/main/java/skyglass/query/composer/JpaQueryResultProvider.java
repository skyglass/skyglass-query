package skyglass.query.composer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import skyglass.data.common.model.IdObject;
import skyglass.data.common.util.reflection.ReflectionMethodsHelper;
import skyglass.query.composer.result.QueryResultProvider;
import skyglass.query.composer.util.QueryResultUtil;

public class JpaQueryResultProvider<T> implements QueryResultProvider<T> {

	private EntityManager entityManager;

	private Class<T> type;

	public JpaQueryResultProvider(EntityManager entityManager, Class<T> type) {
		this.entityManager = entityManager;
		this.type = type;
	}

	@Override
	public List<T> getResult(QueryComposer builder, List<String> uuidList) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.buildResultFromUuidList(uuidList), type);
		setParameters(typedQuery, builder);
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		Map<String, T> idMap = new HashMap<>();
		for (T result: results) {
			String uuid = ReflectionMethodsHelper.getPropertyValue(result, builder.getUuidAlias(), String.class);
			idMap.put(uuid, result);
		}
		List<T> finalResult = new ArrayList<>();
		for (String uuid: uuidList) {
			finalResult.add(idMap.get(uuid));
		}
		return finalResult;
	}

	@Override
	public List<T> getPagedResult(QueryComposer builder, int firstResult, int maxResults) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.build(), type);
		setParameters(typedQuery, builder);
		if (firstResult >= 0) {
			typedQuery.setFirstResult(firstResult);
		}
		if (maxResults >= 0) {
			typedQuery.setMaxResults(maxResults);
		}
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	@Override
	public List<T> getUnpagedResult(QueryComposer builder) {
		TypedQuery<T> typedQuery = entityManager.createQuery(builder.build(), type);
		setParameters(typedQuery, builder);
		List<T> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	@Override
	public int getTotalCount(QueryComposer builder) {
		TypedQuery<Long> typedQuery = entityManager.createQuery(builder.buildCountPart(), Long.class);
		setParameters(typedQuery, builder);
		Long result = QueryResultUtil.getSingleResult(typedQuery);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public List<String> getUuidList(QueryComposer builder, int firstResult, int maxResults) {
		TypedQuery<String> typedQuery = entityManager.createQuery(builder.buildUuidListPart(), String.class);
		setParameters(typedQuery, builder);
		typedQuery.setFirstResult(firstResult);
		typedQuery.setMaxResults(maxResults);
		List<String> results = QueryResultUtil.getListResult(typedQuery);
		return results;
	}

	private void setParameters(TypedQuery<?> typedQuery, QueryComposer builder) {
		for (QueryParam queryParam : builder.getParams()) {
			typedQuery.setParameter(queryParam.getName(), queryParam.getValue());
		}
	}

}
