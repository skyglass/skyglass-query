package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.OrderType;
import skyglass.query.builder.result.MockQuery;

public class QueryStringBuilderDistinctTest {

	@Test
	public void testSimple() {
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });
		String value = "   ";
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(value))
				.setDistinct("sm.uuid")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT COUNT(*) FROM SpaceMission sm", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT DISTINCT sm.uuid AS UUID FROM SpaceMission sm ) tab", testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.uuid IN ('test-list1', 'test-list2')", testBuilder.buildResultFromUuidList(list));
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testWithOrderBy() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(value))
				.setDistinct("sm.uuid")
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test", "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.startNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT DISTINCT * FROM SpaceMission sm ORDER BY LOWER(sm.test) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(*) FROM SpaceMission sm", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT DISTINCT sm.uuid AS UUID FROM SpaceMission sm ORDER BY LOWER(sm.test) DESC ) tab", testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.uuid IN ('test-list1', 'test-list2') ORDER BY LOWER(sm.test) DESC", testBuilder.buildResultFromUuidList(list));
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testWithOrderByAndParams() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.setDistinct("sm.uuid")
				.setDefaultOrder(OrderType.Desc, "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals(
				"SELECT DISTINCT * FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) ORDER BY LOWER(sm.test) DESC",
				testBuilder.build());
		Assert.assertEquals(
				"SELECT COUNT(*) FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
				testBuilder.buildCountPart());
		Assert.assertEquals(
				"SELECT tab.UUID FROM ( SELECT DISTINCT sm.uuid AS UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) ORDER BY LOWER(sm.test) DESC ) tab",
				testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.uuid IN ('test-list1', 'test-list2') ORDER BY LOWER(sm.test) DESC", testBuilder.buildResultFromUuidList(list));
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	public static void checkParam(String name, Object value, QueryStringBuilder builder) {
		for (QueryParam param : builder.getParams()) {
			if (param.getName().equals(name)) {
				Assert.assertEquals(value, param.getValue());
				return;
			}
		}
		Assert.fail("parameter was not found");
	}

	public static void checkNoParam(String name, QueryStringBuilder builder) {
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
