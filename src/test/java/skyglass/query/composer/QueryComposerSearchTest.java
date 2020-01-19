package skyglass.query.composer;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.composer.result.MockQuery;
import skyglass.query.composer.search.SearchType;

public class QueryComposerSearchTest {

	@Test
	public void testSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setSearchTerm("findme")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addSearch("sm.test1", "sm.test2")
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE ( LOWER(sm.test1) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2) LIKE LOWER(?searchTerm0) ) AND sm.test = ?test",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm0", "%findme%", testBuilder);
	}

	@Test
	public void testJpaSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.jpa(MockQueryRequestDto.create(value), "sm")
				.setSearchTerm("findme")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = :test")
				.addSearch("sm.test1", "sm.test2")
				.end();
		Assert.assertEquals("SELECT sm FROM SpaceMission sm WHERE ( LOWER(sm.test1) LIKE LOWER(:searchTerm0) OR LOWER(sm.test2) LIKE LOWER(:searchTerm0) ) AND sm.test = :test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm0", "%findme%", testBuilder);
	}

	@Test
	public void testTranslatableSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setSearchTerm("findme")
				.setLang("cn")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addTranslatableSearch("sm.test1", "sm.test2")
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE ( LOWER(sm.test1.en) LIKE LOWER(?searchTerm0) OR LOWER(sm.test1.de) LIKE LOWER(?searchTerm0) OR LOWER(sm.test1.cn) LIKE LOWER(?searchTerm0) OR LOWER(sm.test1.jp) LIKE LOWER(?searchTerm0) "
						+ "OR LOWER(sm.test1.es) LIKE LOWER(?searchTerm0) OR LOWER(sm.test1.fr) LIKE LOWER(?searchTerm0) OR LOWER(sm.test1.pt) LIKE LOWER(?searchTerm0) "
						+ "OR LOWER(sm.test1.it) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.en) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.de) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.cn) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.jp) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.es) "
						+ "LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.fr) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.pt) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.it) LIKE LOWER(?searchTerm0) ) AND sm.test = ?test",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm0", "%findme%", testBuilder);
	}

	@Test
	public void testTranslatableSearch2() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setSearchTerm("findme")
				.setLang("es")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addSearch("sm.test1.es", "sm.test2.es")
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE ( LOWER(sm.test1.es) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.es) LIKE LOWER(?searchTerm0) ) AND sm.test = ?test",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm0", "%findme%", testBuilder);
	}

	@Test
	public void testCombinedSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setSearchTerm("findme")
				.setLang("es")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.startOr()
				.__().addSearch("sm.test1.es", "sm.test2.es")
				.__().addSearch("searchTerm2", "findme2", SearchType.StartsIgnoreCase, "sm.test3")
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE ( LOWER(sm.test1.es) LIKE LOWER(?searchTerm0) OR LOWER(sm.test2.es) LIKE LOWER(?searchTerm0) ) AND LOWER(sm.test3) LIKE LOWER(?searchTerm20) AND sm.test = ?test",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm0", "%findme%", testBuilder);
		checkParam("searchTerm20", "findme2%", testBuilder);
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
