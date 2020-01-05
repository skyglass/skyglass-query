package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.OrderType;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerOrderByAndCountTest {

	@Test
	public void testCountPart() {
		String value = "   ";
		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm", testBuilder.buildCountPart());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNoOrderBy() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.buildCountPart());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNoOrderBy2() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test2", "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.buildCountPart());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testOrderByBind() {
		String value = " ";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test", "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.startNullablePart("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm ORDER BY LOWER(sm.test) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm", testBuilder.buildCountPart());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testMultipleFieldsOrderBy() {
		String value = " ";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.create(value))
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test", "sm.test1", "sm.test2")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.startNullablePart("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm ORDER BY CONCAT(COALESCE(LOWER(sm.test1), ''), COALESCE(LOWER(sm.test2), '')) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm", testBuilder.buildCountPart());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testDefaultOrderBy() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.setDefaultOrder(OrderType.Desc, "sm.test")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2) ORDER BY LOWER(sm.test) DESC",
				testBuilder.build());
		Assert.assertEquals(
				"SELECT COUNT(1) FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
				testBuilder.buildCountPart());
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testCombinedOrderBy() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.setOrderField("test")
				.setOrderType(OrderType.Desc)
				.bindOrder("test", "sm.test")
				.addOrder(OrderType.Asc, "sm.test2")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.testList IN (?testList1, ?testList2) ORDER BY LOWER(sm.test) DESC, LOWER(sm.test2) ASC",
				testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm WHERE sm.testList IN (?testList1, ?testList2)",
				testBuilder.buildCountPart());
		checkNoParam("test", testBuilder);
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
