package skyglass.query.composer;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.composer.MockQueryRequestDto;
import skyglass.query.builder.composer.QueryComposer;
import skyglass.query.builder.composer.QueryParam;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerSearchCriteriaTest {
	
	@Test
	public void testJpaSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("lastName:doe,age>25")
				.select("*")
				.from("User u")
				.startAndWhere()
				.addSearch("lastName", "age")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE ( LOWER(u.lastName) LIKE LOWER(:lastName) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("lastName", "%doe%", testBuilder);
	}
	
	@Test
	public void testJpaSearchAlias() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("last:doe,age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("last", "u.lastName")
				.addSearch("last", "age")
				.addConditionalWhere("test = u.test", "last");
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(:lastName) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("lastName", "%doe%", testBuilder);
	}
	
	public static void checkParam(String name, Object value, QueryComposer builder) {
		for (QueryParam param : builder.getParams()) {
			if (param.getName().equals(name)) {
				Assert.assertEquals(value, param.getValue());
				return;
			}
		}
		Assert.fail("parameter was not found");
	}

	public static void checkNoParam(String name, QueryComposer builder) {
		for (QueryParam param : builder.getParams()) {
			if (param.getName().equals(name)) {
				Assert.fail("parameter was found");
			}
		}
	}

	public static void checkNoParam(String name, MockQuery query) {
		for (QueryParam param : query.getParams()) {
			if (param.getName().equals(name)) {
				Assert.fail("parameter was found");
			}
		}
	}

	public static void checkParam(String name, Object value, MockQuery query) {
		for (QueryParam param : query.getParams()) {
			if (param.getName().equals(name)) {
				Assert.assertEquals(value, param.getValue());
				return;
			}
		}
		Assert.fail("parameter was not found");
	}

}
