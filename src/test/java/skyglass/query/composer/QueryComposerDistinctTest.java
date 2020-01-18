package skyglass.query.composer;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.composer.OrderType;
import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryParam;
import skyglass.query.composer.result.MockQuery;

public class QueryComposerDistinctTest {

	@Test
	public void testSimple() {
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });
		String value = "   ";
		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setDistinct()
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm GROUP BY sm.UUID", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm GROUP BY sm.UUID", testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.UUID IN ('test-list1', 'test-list2')", testBuilder.buildResultFromUuidList(list));
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testWithOrderBy() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value), "sm")
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test", "sm.test")
				.select("*")
				.setDistinct()
				.from("SpaceMission sm")
				.startAndWhere()
				.startNullablePart("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm GROUP BY sm.UUID ORDER BY LOWER(sm.test) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm GROUP BY sm.UUID", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm GROUP BY sm.UUID ORDER BY LOWER(sm.test) DESC", testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.UUID IN ('test-list1', 'test-list2')", testBuilder.buildResultFromUuidList(list));
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testWithOrderByAndParams() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.createWithList(value, list), "sm")
				.setDistinct()
				.setDefaultOrder(OrderType.Desc, "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) GROUP BY sm.UUID ORDER BY LOWER(sm.test) DESC",
				testBuilder.build());
		Assert.assertEquals(
				"SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) GROUP BY sm.UUID",
				testBuilder.buildCountPart());
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) GROUP BY sm.UUID ORDER BY LOWER(sm.test) DESC",
				testBuilder.buildUuidListPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.UUID IN ('test-list1', 'test-list2')", testBuilder.buildResultFromUuidList(list));
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
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
