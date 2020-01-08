package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerWithRequestValuesTest {

	@Test
	public void testNullableNullQuery1() {
		String value = "   ";
		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.create(value), "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditionalWhere("sm.test = ?test");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test ORDER BY LOWER(sm.order) ASC", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1Distinct() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addDistinctConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID ORDER BY LOWER(sm.order) ASC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID ORDER BY LOWER(sm.order) ASC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1DistinctPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addDistinctConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.order AS order FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNullQuery2() {
		String value = " ";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.create(value), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test OR sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}
	
	@Test
	public void testNullableListNotNullQuery1DistinctSelectPaged() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("TEST, pl.NAME")
				.from("SpaceMission sm")
				.addConditional("LEFT JOIN PLANET pl ON pl.MISSION_UUID = sm.UUID", "name")
				.addConditional("LEFT JOIN SUN sun ON sun.MISSION_UUID = sm.UUID", "name2")
				.addConditionalWhere("sm.test = ?test")
				.addDistinctConditionalOrWhere("sm.testList IN ?testList")
				.setLimit(10);
		Assert.assertEquals(
				"SELECT tab.UUID, tab.TEST, tab.NAME FROM ( SELECT sm.UUID, sm.TEST, pl.NAME FROM SpaceMission sm WHERE sm.test = ?test OR sm.testList IN (?testList1, ?testList2) GROUP BY sm.UUID, sm.TEST, pl.NAME ) tab",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery2() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkNoParam("test", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListNullQuery1() {
		String value = "not null";
		List<String> list = null;

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", value, testBuilder);
	}

	@Test
	public void testNullableListNullQuery2() {
		String value = null;
		List<String> list = Collections.emptyList();

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list), "sm")
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalWhere("sm.testList IN ?testList");
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkParam("test", value, testBuilder);
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
