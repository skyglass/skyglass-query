/**
 *
 */
package skyglass.query.composer.bean;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.Query;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryResult;

public interface QueryManager {

	public Query createNativeQuery(String sqlString);

	public <T, DTO> QueryResult<DTO> getDtoResult(QueryComposer queryComposer, Function<T, DTO> entityDtoConverter, Class<T> type);

	public <T> QueryResult<T> getEntityResult(QueryComposer queryComposer, Class<T> type);

	public QueryResult<Object[]> getNativeResult(QueryComposer queryComposer);

	public <DTO> QueryResult<DTO> convertNativeResult(QueryComposer queryComposer, Supplier<DTO> dtoSupplier);

	public <DTO1, DTO2> QueryResult<DTO2> convertNativeResult(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter);

	public <T, DTO> List<DTO> getDtoList(QueryComposer queryComposer, Function<T, DTO> entityDtoConverter, Class<T> type);

	public <T> List<T> getEntityList(QueryComposer queryComposer, Class<T> type);

	public List<Object[]> getNativeList(QueryComposer queryComposer);

	public <DTO> List<DTO> convertNativeList(QueryComposer queryComposer, Supplier<DTO> dtoSupplier);

	public <DTO1, DTO2> List<DTO2> convertNativeList(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter);

}
