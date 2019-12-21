package skyglass.query.builder.string;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import skyglass.query.QueryResultUtil;
import skyglass.query.builder.result.QueryManager;

public class NativeQueryBuilder {

	@SuppressWarnings("unchecked")
	public static <T> List<T> getListResult(QueryManager queryManager, QueryComposer queryStringBuilder) {
		Query nativeQuery = queryManager.createNativeQuery(queryStringBuilder.build());
		for (QueryParam queryParam : queryStringBuilder.getParams()) {
			if (queryParam.getValue() instanceof Date) {
				nativeQuery.setParameter(queryParam.getName(), (Date) queryParam.getValue(), TemporalType.TIMESTAMP);
			} else {
				nativeQuery.setParameter(queryParam.getName(), queryParam.getValue());
			}
		}
		return QueryResultUtil.getListResult(nativeQuery);
	}

}
