/**
 *
 */
package skyglass.query.builder.result;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.Query;

import skyglass.query.builder.QueryResult;
import skyglass.query.builder.string.QueryStringBuilder;

public interface QueryManager {

	public Query createNativeQuery(String sqlString);

	public <T, DTO> QueryResult<DTO> getDtoResult(QueryStringBuilder queryStringBuilder, Function<T, DTO> entityDtoConverter, Class<T> type);

	public <T> QueryResult<T> getEntityResult(QueryStringBuilder queryStringBuilder, Class<T> type);

	public QueryResult<Object[]> getNativeResult(QueryStringBuilder queryStringBuilder);

	public <DTO> QueryResult<DTO> convertNativeResult(QueryStringBuilder queryStringBuilder, Supplier<DTO> dtoSupplier);

	public <DTO1, DTO2> QueryResult<DTO2> convertNativeResult(QueryStringBuilder queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter);

	public <T, DTO> List<DTO> getDtoList(QueryStringBuilder queryStringBuilder, Function<T, DTO> entityDtoConverter, Class<T> type);

	public <T> List<T> getEntityList(QueryStringBuilder queryStringBuilder, Class<T> type);

	public List<Object[]> getNativeList(QueryStringBuilder queryStringBuilder);

	public <DTO> List<DTO> convertNativeList(QueryStringBuilder queryStringBuilder, Supplier<DTO> dtoSupplier);

	public <DTO1, DTO2> List<DTO2> convertNativeList(QueryStringBuilder queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter);

}
