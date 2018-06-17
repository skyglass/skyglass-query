package skyglass.query.api.test;

import skyglass.query.model.query.jpa.JpaQueryBuilder;

public class TestQueryBuilder extends JpaQueryBuilder<TestRootClazz, TestRootClazz> {

	public TestQueryBuilder() {
		super(TestRootClazz.class, TestRootClazz.class);
	}

}
