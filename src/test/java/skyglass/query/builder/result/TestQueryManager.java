package skyglass.query.builder.result;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import skyglass.query.builder.QueryResult;
import skyglass.query.builder.string.QueryComposer;

public class TestQueryManager {

	public static <T> QueryResult<T> getTestEntityResult(QueryComposer queryStringBuilder, Supplier<List<T>> listSupplier, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult();
	}

	public static <E, DTO> QueryResult<DTO> getEntityDtoTestResult(QueryComposer queryStringBuilder, Function<E, DTO> entityDtoConverter, Supplier<List<E>> listSupplier,
			Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new EntityDtoConverter<>(queryStringBuilder, queryResultProvider, entityDtoConverter).convert();
	}

	public static QueryResult<Object[]> getNativeTestResult(QueryComposer queryStringBuilder, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult();
	}

	public static <DTO> QueryResult<DTO> getNativeDtoTestResult(QueryComposer queryStringBuilder, Supplier<DTO> dtoSupplier, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoConverter<>(queryStringBuilder, queryResultProvider, dtoSupplier).convert();
	}

	public static <DTO1, DTO2> QueryResult<DTO2> getDtoDtoTestResult(QueryComposer queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter,
			Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoDtoConverter<>(queryStringBuilder, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert();
	}

	public static <E> List<E> getEntityTestList(QueryComposer queryStringBuilder, Supplier<List<E>> listSupplier, Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult().getResult();
	}

	public static <E, DTO> List<DTO> getEntityDtoTestList(QueryComposer queryStringBuilder, Function<E, DTO> entityDtoConverter, Supplier<List<E>> listSupplier,
			Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new EntityDtoConverter<>(queryStringBuilder, queryResultProvider, entityDtoConverter).convert().getResult();
	}

	public static List<Object[]> getNativeTestList(QueryComposer queryStringBuilder, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryStringBuilder, queryResultProvider).getResult().getResult();
	}

	public static <DTO> List<DTO> getNativeTestList(QueryComposer queryStringBuilder, Supplier<DTO> activeRecordSupplier, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoConverter<>(queryStringBuilder, queryResultProvider, activeRecordSupplier).convert().getResult();
	}

	public static <DTO1, DTO2> List<DTO2> getDtoDtoTestList(QueryComposer queryStringBuilder, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter,
			Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoDtoConverter<>(queryStringBuilder, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert().getResult();
	}

}
