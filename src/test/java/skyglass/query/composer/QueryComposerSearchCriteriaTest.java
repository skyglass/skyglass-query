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
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(:last) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("last", "%doe%", testBuilder);
	}
	
	@Test
	public void testJpaSearchAliasStartWhere() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("last:doe,age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("last", "u.lastName")
				.addSearch("last", "age")
				.startAndWhere()
					.addAliases("last")
					.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(:last) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("last", "%doe%", testBuilder);
	}
	
	@Test
	public void testJpaSearchAliasStartWhereWithRequestValue() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.appendNullable("test = u.test", "test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testJpaSearchAliasStartWhereWithSearchValue() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.addAliases("test")
					.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(:test) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}
	
	@Test
	public void testJpaSearchAliasStartWhereWithNullRequestValue() {
		String value = null;

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.appendNullable("test = u.test", "test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkNoParam("test", testBuilder);
	}
	
	@Test
	public void testJpaSearchAliasWithNotNullSearchAndNullRequestValue() {
		String value = null;

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.addAliases("test")
					.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(:test) ) AND ( u.age > :age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}
	
	@Test
	public void testNativeSearchAliasWithNotNullSearchAndNullRequestValue() {
		String value = null;

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.addAliases("test")
					.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u.UUID FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(?test) ) AND ( u.age > ?age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}
	
	@Test
	public void testNativeSearchWithSkipUuid() {
		String value = null;

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>25")
				.skipUuid()
				.select("name")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
					.addAliases("test")
					.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u.name FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(?test) ) AND ( u.age > ?age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}
	
	@Test
	public void testNativeSearchWithSkipUuidAndNotEmptyWherePart() {
		String value = null;

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>25")
				.skipUuid()
				.select("name")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addWhere("test = u.test");
		Assert.assertEquals("SELECT u.name FROM User u WHERE test = u.test AND ( LOWER(u.lastName) LIKE LOWER(?test) ) AND ( u.age > ?age )", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
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
