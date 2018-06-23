package skyglass.query.api.test;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.AbstractDataFilter;

public class TestDataFilter extends AbstractDataFilter<TestRootClazz, TestDataFilter> {

	public TestDataFilter(JunctionType junctionType, IFilterRequest request) {
		super(TestRootClazz.class, junctionType, new TestQueryBuilder(), request);
	}

	@Override
	protected TestDataFilter self() {
		return this;
	}

}
