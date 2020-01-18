package skyglass.query.composer.result;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryResult;
import skyglass.query.composer.result.DtoConverter;
import skyglass.query.composer.result.DtoDtoConverter;
import skyglass.query.composer.result.EntityDtoConverter;
import skyglass.query.composer.result.QueryResultBuilder;
import skyglass.query.composer.result.QueryResultProvider;

public class TestQueryManager {

	public static <T> QueryResult<T> getTestEntityResult(QueryComposer queryComposer, Supplier<List<T>> listSupplier, Class<T> type) {
		QueryResultProvider<T> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult();
	}

	public static <E, DTO> QueryResult<DTO> getEntityDtoTestResult(QueryComposer queryComposer, Function<E, DTO> entityDtoConverter, Supplier<List<E>> listSupplier,
			Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new EntityDtoConverter<>(queryComposer, queryResultProvider, entityDtoConverter).convert();
	}

	public static QueryResult<Object[]> getNativeTestResult(QueryComposer queryComposer, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult();
	}

	public static <DTO> QueryResult<DTO> getNativeDtoTestResult(QueryComposer queryComposer, Supplier<DTO> dtoSupplier, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoConverter<>(queryComposer, queryResultProvider, dtoSupplier).convert();
	}

	public static <DTO1, DTO2> QueryResult<DTO2> getDtoDtoTestResult(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter,
			Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoDtoConverter<>(queryComposer, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert();
	}

	public static <E> List<E> getEntityTestList(QueryComposer queryComposer, Supplier<List<E>> listSupplier, Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult().getResult();
	}

	public static <E, DTO> List<DTO> getEntityDtoTestList(QueryComposer queryComposer, Function<E, DTO> entityDtoConverter, Supplier<List<E>> listSupplier,
			Class<E> type) {
		QueryResultProvider<E> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new EntityDtoConverter<>(queryComposer, queryResultProvider, entityDtoConverter).convert().getResult();
	}

	public static List<Object[]> getNativeTestList(QueryComposer queryComposer, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new QueryResultBuilder<>(queryComposer, queryResultProvider).getResult().getResult();
	}

	public static <DTO> List<DTO> getNativeTestList(QueryComposer queryComposer, Supplier<DTO> activeRecordSupplier, Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoConverter<>(queryComposer, queryResultProvider, activeRecordSupplier).convert().getResult();
	}

	public static <DTO1, DTO2> List<DTO2> getDtoDtoTestList(QueryComposer queryComposer, Supplier<DTO1> dto1Supplier,
			Function<DTO1, DTO2> dto1Dto2Converter,
			Supplier<List<Object[]>> listSupplier) {
		QueryResultProvider<Object[]> queryResultProvider = new TestResultProvider<>(listSupplier);
		return new DtoDtoConverter<>(queryComposer, queryResultProvider, dto1Supplier, dto1Dto2Converter).convert().getResult();
	}

}
