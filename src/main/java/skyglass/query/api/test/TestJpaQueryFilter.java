package skyglass.query.api.test;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.AbstractQueryFilter;

public class TestJpaQueryFilter extends AbstractQueryFilter<TestRootClazz, TestJpaQueryFilter> {

	public TestJpaQueryFilter(JunctionType junctionType, IFilterRequest request) {
		super(TestRootClazz.class, junctionType, new TestQueryBuilder(), request);
	}

	@Override
	protected TestJpaQueryFilter self() {
		return this;
	}

}
