package skyglass.query.builder.result;

import java.util.List;
import java.util.function.Supplier;

import skyglass.query.builder.result.QueryResultProvider;
import skyglass.query.builder.string.QueryStringBuilder;

public class TestResultProvider<T> implements QueryResultProvider<T> {

	private Supplier<List<T>> listSupplier;

	public TestResultProvider(Supplier<List<T>> listSupplier) {
		this.listSupplier = listSupplier;
	}

	@Override
	public List<T> getResult(QueryStringBuilder builder, List<String> uuidList) {
		return listSupplier.get();
	}

	@Override
	public List<T> getPagedResult(QueryStringBuilder builder, int firstResult, int maxResults) {
		return listSupplier.get();
	}

	@Override
	public List<T> getUnpagedResult(QueryStringBuilder builder) {
		return listSupplier.get();
	}

	@Override
	public int getTotalCount(QueryStringBuilder builder) {
		return 1;
	}

	@Override
	public List<String> getUuidList(QueryStringBuilder builder, int firstResult, int maxResults) {
		return null;
	}

}
