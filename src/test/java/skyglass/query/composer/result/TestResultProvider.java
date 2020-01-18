package skyglass.query.composer.result;

import java.util.List;
import java.util.function.Supplier;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.result.QueryResultProvider;

public class TestResultProvider<T> implements QueryResultProvider<T> {

	private Supplier<List<T>> listSupplier;

	public TestResultProvider(Supplier<List<T>> listSupplier) {
		this.listSupplier = listSupplier;
	}

	@Override
	public List<T> getResult(QueryComposer builder, List<String> uuidList) {
		return listSupplier.get();
	}

	@Override
	public List<T> getPagedResult(QueryComposer builder, int firstResult, int maxResults) {
		return listSupplier.get();
	}

	@Override
	public List<T> getUnpagedResult(QueryComposer builder) {
		return listSupplier.get();
	}

	@Override
	public int getTotalCount(QueryComposer builder) {
		return 1;
	}

	@Override
	public List<String> getUuidList(QueryComposer builder, int firstResult, int maxResults) {
		return null;
	}

}
