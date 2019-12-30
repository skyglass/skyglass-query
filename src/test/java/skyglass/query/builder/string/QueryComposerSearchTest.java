package skyglass.query.builder.string;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.SearchType;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerSearchTest {

	@Test
	public void testSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setSearchTerm("findme")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addSearch("sm.test1", "sm.test2")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND ( ( LOWER(sm.test1) LIKE LOWER(?searchTerm) OR LOWER(sm.test2) LIKE LOWER(?searchTerm) ) )", testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm", "%findme%", testBuilder);
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
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = :test AND ( ( LOWER(sm.test1) LIKE LOWER(:searchTerm) OR LOWER(sm.test2) LIKE LOWER(:searchTerm) ) )", testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm", "%findme%", testBuilder);
	}

	@Test
	public void testTranslatableSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setSearchTerm("findme")
				.setLang("cn")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addTranslatableSearch("sm.test1", "sm.test2")
				.end();
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND ( ( LOWER(sm.test1.en) LIKE LOWER(?searchTerm) OR LOWER(sm.test1.de) LIKE LOWER(?searchTerm) OR LOWER(sm.test1.cn) LIKE LOWER(?searchTerm) OR LOWER(sm.test1.jp) LIKE LOWER(?searchTerm) "
						+ "OR LOWER(sm.test1.es) LIKE LOWER(?searchTerm) OR LOWER(sm.test1.fr) LIKE LOWER(?searchTerm) OR LOWER(sm.test1.pt) LIKE LOWER(?searchTerm) "
						+ "OR LOWER(sm.test1.it) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.en) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.de) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.cn) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.jp) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.es) "
						+ "LIKE LOWER(?searchTerm) OR LOWER(sm.test2.fr) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.pt) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.it) LIKE LOWER(?searchTerm) ) )",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm", "%findme%", testBuilder);
	}

	@Test
	public void testTranslatableSearch2() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setSearchTerm("findme")
				.setLang("es")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.addSearch("sm.test1.es", "sm.test2.es")
				.end();
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND ( ( LOWER(sm.test1.es) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.es) LIKE LOWER(?searchTerm) ) )",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm", "%findme%", testBuilder);
	}

	@Test
	public void testCombinedSearch() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
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
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND ( ( ( LOWER(sm.test1.es) LIKE LOWER(?searchTerm) OR LOWER(sm.test2.es) LIKE LOWER(?searchTerm) ) ) OR ( ( LOWER(sm.test3) LIKE LOWER(?searchTerm2) ) ) )",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("searchTerm", "%findme%", testBuilder);
		checkParam("searchTerm2", "%findme2", testBuilder);
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
