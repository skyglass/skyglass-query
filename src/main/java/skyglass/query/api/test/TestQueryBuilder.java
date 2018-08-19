package skyglass.query.api.test;

import skyglass.query.api.jpa.AbstractJpaQueryBuilder;
import skyglass.query.model.criteria.ITypedQuery;

public class TestQueryBuilder extends AbstractJpaQueryBuilder<TestRootClazz, TestRootClazz> {

	public TestQueryBuilder() {
		super(new TestMetadataHelper());
	}

	@Override
	public <T> ITypedQuery<T> createQuery(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypedQuery<TestRootClazz> createQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypedQuery<Long> createCountQuery() {
		// TODO Auto-generated method stub
		return null;
	}

}
