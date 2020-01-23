package skyglass.query.composer;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.composer.result.MockQuery;

public class QueryComposerSearchCriteriaTest {

	@Test
	public void testJpaSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("lastName:doe,age>25")
				.from("User u")
				.startAndWhere()
				.addSearch("lastName", "age")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:lastName) AND u.age >= :age", testBuilder.build());
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
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:last) AND u.age >= :age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:last) AND u.age >= :age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u FROM User u WHERE u.age >= :age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:test) AND u.age >= :age AND test = u.test", testBuilder.build());
		checkParam("age", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartWhereWithSearchValueAndRange() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe,age>20,age<25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
				.addAliases("test")
				.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:test) AND u.age >= :age AND u.age <= :age2 AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartWhereWithSearchValueAndRangeWithOr() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe|age>20,age<25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.startAndWhere()
				.addAliases("test")
				.append("test = u.test")
				.end();
		Assert.assertEquals("SELECT u FROM User u WHERE ( LOWER(u.lastName) LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithSearchValueAndRangeWithOr() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.setSearchTerm("test:doe|age>20,age<25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals("SELECT u FROM User u WHERE ( LOWER(u.lastName) LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithAddSearchTerm() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals("SELECT u FROM User u WHERE ( LOWER(u.lastName) LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithAndSearchTerms() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25,")
				.addSearchTerm("test2:doe2|age>23,age<24")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals("SELECT u FROM User u WHERE ( LOWER(u.lastName) "
				+ "LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) "
				+ "AND u.age >= :age3 AND u.age <= :age4 "
				+ "AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("age3", 23, testBuilder);
		checkParam("age4", 24, testBuilder);
		checkParam("test", "%doe%", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithOrSearchTerms() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25|")
				.addSearchTerm("test2:doe2|age>23,age<24")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals("SELECT u FROM User u WHERE ( ( LOWER(u.lastName) "
				+ "LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) "
				+ "OR u.age >= :age3 AND u.age <= :age4 ) "
				+ "AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("age3", 23, testBuilder);
		checkParam("age4", 24, testBuilder);
		checkParam("test", "%doe%", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithOrSearchTerms2() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25|")
				.addSearchTerm("test2:doe2|age>23|age<24")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals("SELECT u FROM User u WHERE ( ( LOWER(u.lastName) "
				+ "LIKE LOWER(:test) OR u.age >= :age AND u.age <= :age2 ) "
				+ "OR ( u.age >= :age3 OR u.age <= :age4 ) ) "
				+ "AND test = u.test", testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("age3", 23, testBuilder);
		checkParam("age4", 24, testBuilder);
		checkParam("test", "%doe%", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithMultipleSearchTerms() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25")
				.addSearchTerm("test:doe|age>21|age<24,age=22")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals(
				"SELECT u FROM User u WHERE ( LOWER(u.lastName) LIKE LOWER(:test) "
						+ "OR u.age >= :age AND u.age <= :age2 ) "
						+ "AND ( ( LOWER(u.lastName) LIKE LOWER(:test2) OR u.age >= :age3 ) "
						+ "AND u.age <= :age4 AND u.age = :age5 ) AND test = u.test",
				testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("age3", 21, testBuilder);
		checkParam("age4", 24, testBuilder);
		checkParam("age5", 22, testBuilder);
		checkParam("test", "%doe%", testBuilder);
	}

	@Test
	public void testJpaSearchAliasStartConditionalWhereWithThreeCombinedSearchTerms() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "u")
				.addSearchTerm("test:doe|age>20,age<25|")
				.addSearchTerm("test:doe|age>21|age<24,age=22|")
				.addSearchTerm("age>2")
				.select("*")
				.from("User u")
				.addAliasResolver("test", "u.lastName")
				.addSearch("test", "age")
				.addConditionalWhere("test = u.test", "test");
		Assert.assertEquals(
				"SELECT u FROM User u WHERE ( ( LOWER(u.lastName) LIKE LOWER(:test) "
						+ "OR u.age >= :age AND u.age <= :age2 ) "
						+ "OR ( ( LOWER(u.lastName) LIKE LOWER(:test2) OR u.age >= :age3 ) "
						+ "AND u.age <= :age4 AND u.age = :age5 ) AND u.age >= :age6 ) AND test = u.test",
				testBuilder.build());
		checkParam("age", 20, testBuilder);
		checkParam("age2", 25, testBuilder);
		checkParam("age3", 21, testBuilder);
		checkParam("age4", 24, testBuilder);
		checkParam("age5", 22, testBuilder);
		checkParam("age6", 2, testBuilder);
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
		Assert.assertEquals("SELECT u FROM User u WHERE u.age >= :age", testBuilder.build());
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
		Assert.assertEquals("SELECT u FROM User u WHERE LOWER(u.lastName) LIKE LOWER(:test) AND u.age >= :age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u.UUID FROM User u WHERE LOWER(u.lastName) LIKE LOWER(?test) AND u.age >= ?age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u.name FROM User u WHERE LOWER(u.lastName) LIKE LOWER(?test) AND u.age >= ?age AND test = u.test", testBuilder.build());
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
		Assert.assertEquals("SELECT u.name FROM User u WHERE LOWER(u.lastName) LIKE LOWER(?test) AND u.age >= ?age AND test = u.test", testBuilder.build());
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
