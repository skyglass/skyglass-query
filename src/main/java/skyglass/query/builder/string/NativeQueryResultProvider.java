package skyglass.query.builder.string;

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
	public List<Object[]> getResult(QueryStringBuilder builder, List<String> uuidList) {
		Query nativeQuery = entityManager.createNativeQuery(builder.buildResultFromUuidList(uuidList));
		setParameters(nativeQuery, builder);
		@SuppressWarnings("unchecked")
		List<Object[]> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	@Override
	public List<Object[]> getPagedResult(QueryStringBuilder builder, int firstResult, int maxResults) {
		Query nativeQuery = entityManager.createNativeQuery(builder.build());
		setParameters(nativeQuery, builder);
		nativeQuery.setFirstResult(firstResult);
		nativeQuery.setMaxResults(maxResults);
		@SuppressWarnings("unchecked")
		List<Object[]> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	@Override
	public List<Object[]> getUnpagedResult(QueryStringBuilder builder) {
		Query nativeQuery = entityManager.createNativeQuery(builder.build());
		setParameters(nativeQuery, builder);
		@SuppressWarnings("unchecked")
		List<Object[]> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	@Override
	public int getTotalCount(QueryStringBuilder builder) {
		Query nativeQuery = entityManager.createNativeQuery(builder.buildCountPart());
		setParameters(nativeQuery, builder);
		Long result = (Long) QueryResultUtil.getSingleResult(nativeQuery);
		return result == null ? 0 : result.intValue();
	}

	@Override
	public List<String> getUuidList(QueryStringBuilder builder, int firstResult, int maxResults) {
		Query nativeQuery = entityManager.createNativeQuery(builder.buildUuidListPart());
		setParameters(nativeQuery, builder);
		nativeQuery.setFirstResult(firstResult);
		nativeQuery.setMaxResults(maxResults);
		@SuppressWarnings("unchecked")
		List<String> results = QueryResultUtil.getListResult(nativeQuery);
		return results;
	}

	private void setParameters(Query nativeQuery, QueryStringBuilder builder) {
		for (QueryParam queryParam : builder.getParams()) {
			if (queryParam.getValue() instanceof Date) {
				nativeQuery.setParameter(queryParam.getName(), (Date) queryParam.getValue(), TemporalType.TIMESTAMP);
			} else {
				nativeQuery.setParameter(queryParam.getName(), queryParam.getValue());
			}
		}
	}

}
