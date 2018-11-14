/**
 *
 */
package skyglass.query.builder.result;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import skyglass.query.builder.string.JpaQueryResultProvider;
import skyglass.query.builder.string.NativeQueryResultProvider;
import skyglass.query.builder.string.QueryStringBuilder;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class QueryManagerImpl implements QueryManager {

	@PersistenceContext(unitName = "platform")
	private EntityManager entityManager;

	@Override
	public <T, DTO> QueryResult<DTO> getDtoResult(QueryStringBuilder queryStringBuilder, Function<T, DTO> entityDtoConverter, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new JpaQueryResultProvider<T>(entityManager, type);
		return new EntityDtoConverter<>(queryStringBuilder, queryResultProvider, entityDtoConverter).convert();
	}

	@Override
	public <T> QueryResult<T> getEntityResult(QueryStringBuilder queryStringBuilder, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new JpaQueryResultProvider<T>(entityManager, type);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult();
	}

	@Override
	public QueryResult<Object[]> getNativeResult(QueryStringBuilder queryStringBuilder) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult();
	}

	@Override
	public <DTO> QueryResult<DTO> convertNativeResult(QueryStringBuilder queryStringBuilder, Supplier<DTO> dtoSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new DtoConverter<>(queryStringBuilder, queryResultProvider, dtoSupplier).convert();
	}

	@Override
	public <DTO1, DTO2> QueryResult<DTO2> convertNativeResult(QueryStringBuilder queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new DtoDtoConverter<>(queryStringBuilder, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert();
	}

	@Override
	public <T, DTO> List<DTO> getDtoList(QueryStringBuilder queryStringBuilder, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return getDtoResult(queryStringBuilder, entityDtoConverter, type).getResult();
	}

	@Override
	public <T> List<T> getEntityList(QueryStringBuilder queryStringBuilder, Class<T> type) {
		return getEntityResult(queryStringBuilder, type).getResult();
	}

	@Override
	public List<Object[]> getNativeList(QueryStringBuilder queryStringBuilder) {
		return getNativeResult(queryStringBuilder).getResult();
	}

	@Override
	public <DTO> List<DTO> convertNativeList(QueryStringBuilder queryStringBuilder, Supplier<DTO> dtoSupplier) {
		return convertNativeResult(queryStringBuilder, dtoSupplier).getResult();
	}

	@Override
	public <DTO1, DTO2> List<DTO2> convertNativeList(QueryStringBuilder queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		return convertNativeResult(queryStringBuilder, dto1Supplier, dto1Dto2Converter).getResult();
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return entityManager.createNativeQuery(sqlString);
	}

}
