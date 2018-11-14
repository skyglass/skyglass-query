package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.result.MockQuery;

public class QueryStringBuilderWithRequestTest {

	@Test
	public void testNullableNullQuery1() {
		String value = "   ";
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(value))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery1() {
		String value = "not null";

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(value))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNullQuery2() {
		String value = " ";

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(value))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.startNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery2() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
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

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", value, testBuilder);
	}

	@Test
	public void testNullableListNullQuery2() {
		String value = null;
		List<String> list = Collections.emptyList();

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullable("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(value, list))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullableOrElseFalse("sm.testList IN ?testList")
				.end();
		Assert.assertEquals(
				"SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkParam("test", value, testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNull() {
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(list))
				.select("*").from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test", "test", " ")
				.__().appendNullableOrElseFalse("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.testList IN (?testList1, ?testList2)",
				testBuilder.build());
		checkNoParam("test", testBuilder);
		checkParam("testList1", list.get(0), testBuilder);
		checkParam("testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNull() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList("not null", null))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullableOrElseFalse("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test AND 1 = 0", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseListNull() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.createWithList(null, Collections.emptyList()))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullable("sm.test = ?test")
				.__().appendNullableOrElseFalse("sm.testList IN ?testList")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE 1 = 0", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableOrElseOtherQuery1() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(""))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test = ?test")
				.__().orElse("1 = 1")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE 1 = 1", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableOrElseOtherQuery2() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create("not null"))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test = ?test")
				.__().orElse("1 = 1")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery2() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create("not null"))
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test = ?test")
				.end();
		Assert.assertEquals("SELECT * FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNestedNullableNullQuery() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create(" ", null))
				.select("sm")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test1 = ?test1").stopNullable()
				.__().startNullable("sm.test2 = ?test2").stopNullable()
				.end();
		Assert.assertEquals("SELECT sm FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test1", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testNestedNullableNotNullQuery1() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create("not null", " "))
				.select("sm")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test1 = ?test1").stopNullable()
				.__().startNullable("sm.test2 = ?test2").stopNullable()
				.end();
		Assert.assertEquals("SELECT sm FROM SpaceMission sm WHERE sm.test1 = ?test1", testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testNestedNullableNotNullQuery2() {
		QueryStringBuilder testBuilder = QueryStringBuilder
				.nativ(MockQueryRequestDto.create("not null", " not null"))
				.select("sm")
				.from("SpaceMission sm")
				.startAndWhere()
				.__().startNullable("sm.test1 = ?test1").stopNullable()
				.__().startNullable("sm.test2 = ?test2").stopNullable()
				.end();
		Assert.assertEquals("SELECT sm FROM SpaceMission sm WHERE sm.test1 = ?test1 AND sm.test2 = ?test2", testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkParam("test2", " not null", testBuilder);
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
