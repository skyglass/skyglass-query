package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerWithRequestValuesTest {

	@Test
	public void testNullableNullQuery1() {
		String value = "   ";
		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.create(value))
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditionalWhere("sm.test = ?test");
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		QueryComposer testBuilder = QueryComposer
				.nativ(request)
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order");
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test ORDER BY LOWER(sm.order) ASC", testBuilder.build());
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
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID ORDER BY LOWER(sm.order) ASC", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNullQuery2() {
		String value = " ";

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.create(value))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test");
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test OR sm.testList IN (?testList1, ?testList2)",
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
				.nativ(MockQueryMapRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.testList IN (?testList1, ?testList2)",
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
				.nativ(MockQueryMapRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", value, testBuilder);
	}

	@Test
	public void testNullableListNullQuery2() {
		String value = null;
		List<String> list = Collections.emptyList();

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalOrWhere("sm.testList IN ?testList");
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ(MockQueryMapRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.addConditionalWhere("sm.test = ?test")
				.addConditionalWhere("sm.testList IN ?testList");
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
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
