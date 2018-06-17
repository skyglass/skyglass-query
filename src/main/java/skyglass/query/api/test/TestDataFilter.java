package skyglass.query.api.test;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.DataFilter;

public class TestDataFilter extends DataFilter<TestRootClazz> {

	public TestDataFilter(JunctionType junctionType, IFilterRequest request) {
		super(TestRootClazz.class, junctionType, new TestQueryBuilder(), request);
	}

}
