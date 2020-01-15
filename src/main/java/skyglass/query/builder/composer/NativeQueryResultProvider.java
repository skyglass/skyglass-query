package skyglass.query.builder.composer;

import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TemporalType;

import skyglass.query.QueryResultUtil;
import skyglass.query.builder.result.QueryResultProvider;

public class NativeQueryResultProvider implements QueryResultProvider<Object[]> {

	private EntityManager entityManager;

	public NativeQueryResultProvider(EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public List<Object[]> getResult(QueryComposer builder, List<String> uuidList) {
		throw new UnsupportedOperationException("This method is not supported for native queries. Please return result directly, without"
				+ " intermediately returning uuid list");
	}

	@Override
	public List<Object[]> getPagedResult(QueryComposer builder, int firstResult, int maxResults) {
		Query nativeQuery = entityManager.createNativeQuery(builder.build());
		setParameters(nativeQuery, builder);
		nativeQuery.setFirstResult(firstResult);
		nativeQuery.setMaxResults(maxResults);
		@SuppressWarnings("unchecked")
		List<Object[]> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	@Override
	public List<Object[]> getUnpagedResult(QueryComposer builder) {
		Query nativeQuery = entityManager.createNativeQuery(builder.build());
		setParameters(nativeQuery, builder);
		@SuppressWarnings("unchecked")
		List<Object[]> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	@Override
	public int getTotalCount(QueryComposer builder) {
		Query nativeQuery = entityManager.createNativeQuery(builder.buildCountPart());
		setParameters(nativeQuery, builder);
		Long result = (Long) QueryResultUtil.getSingleResult(nativeQuery);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public List<String> getUuidList(QueryComposer builder, int firstResult, int maxResults) {
		Query nativeQuery = entityManager.createNativeQuery(builder.buildUuidListPart());
		setParameters(nativeQuery, builder);
		nativeQuery.setFirstResult(firstResult);
		nativeQuery.setMaxResults(maxResults);
		@SuppressWarnings("unchecked")
		List<String> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	private void setParameters(Query nativeQuery, QueryComposer builder) {
		for (QueryParam queryParam : builder.getParams()) {
			if (queryParam.getValue() instanceof Date) {
				nativeQuery.setParameter(queryParam.getName(), (Date) queryParam.getValue(), TemporalType.TIMESTAMP);
			} else {
				nativeQuery.setParameter(queryParam.getName(), queryParam.getValue());
			}
		}
	}

}
