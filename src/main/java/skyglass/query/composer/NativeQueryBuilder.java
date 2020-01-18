package skyglass.query.composer;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;
import javax.persistence.TemporalType;

import skyglass.query.composer.bean.QueryManager;
import skyglass.query.composer.util.QueryResultUtil;

public class NativeQueryBuilder {

	@SuppressWarnings("unchecked")
	public static <T> List<T> getListResult(QueryManager queryManager, QueryComposer queryComposer) {
		Query nativeQuery = queryManager.createNativeQuery(queryComposer.build());
		for (QueryParam queryParam : queryComposer.getParams()) {
			if (!(queryParam instanceof AliasParam)) {
				if (queryParam.getValue() instanceof Date) {
					nativeQuery.setParameter(queryParam.getName(), (Date) queryParam.getValue(), TemporalType.TIMESTAMP);
				} else {
					nativeQuery.setParameter(queryParam.getName(), queryParam.getValue());
				}
			}
		}
		return QueryResultUtil.getListResult(nativeQuery);
	}

}
