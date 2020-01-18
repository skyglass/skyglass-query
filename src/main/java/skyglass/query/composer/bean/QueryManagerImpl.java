/**
 *
 */
package skyglass.query.composer.bean;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import skyglass.data.common.model.IdObject;
import skyglass.query.composer.JpaQueryResultProvider;
import skyglass.query.composer.NativeQueryResultProvider;
import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryResult;
import skyglass.query.composer.result.DtoConverter;
import skyglass.query.composer.result.DtoDtoConverter;
import skyglass.query.composer.result.EntityDtoConverter;
import skyglass.query.composer.result.QueryResultBuilder;
import skyglass.query.composer.result.QueryResultProvider;

@Repository
@Transactional(propagation = Propagation.MANDATORY)
public class QueryManagerImpl implements QueryManager {

	@PersistenceContext
	private EntityManager entityManager;

	@Override
	public <T, DTO> QueryResult<DTO> getDtoResult(QueryComposer queryComposer, Function<T, DTO> entityDtoConverter, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new JpaQueryResultProvider<T>(entityManager, type);
		return new EntityDtoConverter<>(queryComposer, queryResultProvider, entityDtoConverter).convert();
	}

	@Override
	public <T> QueryResult<T> getEntityResult(QueryComposer queryComposer, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new JpaQueryResultProvider<T>(entityManager, type);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult();
	}

	@Override
	public QueryResult<Object[]> getNativeResult(QueryComposer queryComposer) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult();
	}

	@Override
	public <DTO> QueryResult<DTO> convertNativeResult(QueryComposer queryComposer, Supplier<DTO> dtoSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new DtoConverter<>(queryComposer, queryResultProvider, dtoSupplier).convert();
	}

	@Override
	public <DTO1, DTO2> QueryResult<DTO2> convertNativeResult(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		QueryResultProvider<Object[]> queryResultProvider = new NativeQueryResultProvider(entityManager);
		return new DtoDtoConverter<>(queryComposer, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert();
	}

	@Override
	public <T, DTO> List<DTO> getDtoList(QueryComposer queryComposer, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return getDtoResult(queryComposer, entityDtoConverter, type).getResult();
	}

	@Override
	public <T> List<T> getEntityList(QueryComposer queryComposer, Class<T> type) {
		return getEntityResult(queryComposer, type).getResult();
	}

	@Override
	public List<Object[]> getNativeList(QueryComposer queryComposer) {
		return getNativeResult(queryComposer).getResult();
	}

	@Override
	public <DTO> List<DTO> convertNativeList(QueryComposer queryComposer, Supplier<DTO> dtoSupplier) {
		return convertNativeResult(queryComposer, dtoSupplier).getResult();
	}

	@Override
	public <DTO1, DTO2> List<DTO2> convertNativeList(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter) {
		return convertNativeResult(queryComposer, dto1Supplier, dto1Dto2Converter).getResult();
	}

	@Override
	public Query createNativeQuery(String sqlString) {
		return entityManager.createNativeQuery(sqlString);
	}

}
