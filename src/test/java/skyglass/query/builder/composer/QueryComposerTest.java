package skyglass.query.builder.composer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.composer.QueryComposer;
import skyglass.query.builder.composer.QueryParam;
import skyglass.query.builder.result.MockQuery;

public class QueryComposerTest {

	@Test
	public void testSimpleQuery() {
		String test = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").build();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", test);
	}

	@Test
	public void testNullableNullQuery1() {
		String value = "   ";
		QueryComposer testBuilder = QueryComposer
				.nativ("sm")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value)
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery1() {
		String value = "not null";

		QueryComposer testBuilder = QueryComposer
				.nativ("sm")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value)
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNullQuery2() {
		String value = " ";

		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test = ?test").setParameter("test", value).end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ("sm")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value)
				.appendNullableList("sm.testList", list)
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?sm_testList1, ?sm_testList2)",
				testBuilder.build());
		checkParam("test", "not null", testBuilder);
		checkParam("sm_testList1", list.get(0), testBuilder);
		checkParam("sm_testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListNotNullQuery2() {
		String value = " ";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer
				.nativ("sm")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value)
				.appendNullableList("sm.testList", list)
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.testList IN (?sm_testList1, ?sm_testList2)",
				testBuilder.build());
		checkNoParam("test", testBuilder);
		checkParam("sm_testList1", list.get(0), testBuilder);
		checkParam("sm_testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListNullQuery1() {
		String value = "not null";
		Collection<String> list = null;

		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value).appendNullableList("sm.testList", list).end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", value, testBuilder);
	}

	@Test
	public void testNullableListNullQuery2() {
		String value = null;
		Collection<String> list = Collections.emptyList();

		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value).appendNullableList("sm.testList", list).end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNullQuery1() {
		String value = "not null";
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });

		QueryComposer testBuilder = QueryComposer.nativ("sm")
				.select("*")
				.from("SpaceMission sm")
				.startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", value)
				.appendNullableListOrElseFalse("sm.testList", list)
				.end();
		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND sm.testList IN (?sm_testList1, ?sm_testList2)",
				testBuilder.build());
		checkParam("test", value, testBuilder);
		checkParam("sm_testList1", list.get(0), testBuilder);
		checkParam("sm_testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNotNull() {
		List<String> list = Arrays.asList(new String[] { "test-list1", "test-list2" });
		QueryComposer testBuilder = QueryComposer
				.nativ("sm")
				.select("*").from("SpaceMission sm")
				.startAndWhere()
				.__().appendNullableValue("sm.test = ?test", "test", " ")
				.__().appendNullableListOrElseFalse("sm.testList", list)
				.end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.testList IN (?sm_testList1, ?sm_testList2)",
				testBuilder.build());
		checkNoParam("test", testBuilder);
		checkParam("sm_testList1", list.get(0), testBuilder);
		checkParam("sm_testList2", list.get(1), testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseNull() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", "not null")
				.appendNullableListOrElseFalse("sm.testList", null).end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test AND 1 = 0", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableListOrElseFalseListNull() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.appendNullableValue("sm.test = ?test", "test", null)
				.appendNullableListOrElseFalse("sm.testList", Collections.emptyList()).end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE 1 = 0", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableOrElseOtherQuery1() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test = ?test").setParameter("test", " ").orElse("1 = 1").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE 1 = 1", testBuilder.build());
		checkNoParam("test", testBuilder);
	}

	@Test
	public void testNullableOrElseOtherQuery2() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test = ?test").setParameter("test", "not null").orElse("1 = 1").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNullableNotNullQuery2() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("*").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test = ?test").setParameter("test", "not null").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test = ?test", testBuilder.build());
		checkParam("test", "not null", testBuilder);
	}

	@Test
	public void testNestedNullableNullQuery() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", " ").startNullablePart("sm.test2 = ?test2")
				.setParameter("test2", "not null").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm", testBuilder.build());
		checkNoParam("test1", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testNestedNullableNotNullQuery1() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", "not null").startNullablePart("sm.test2 = ?test2")
				.setParameter("test2", " ").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test1 = ?test1", testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkNoParam("test2", testBuilder);
	}

	@Test
	public void testNestedNullableNotNullQuery2() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", "not null")
				.startNullablePart(" AND sm.test2 = ?test2").setParameter("test2", "not null").end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test1 = ?test1 AND sm.test2 = ?test2",
				testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkParam("test2", "not null", testBuilder);
	}

	@Test
	public void testJoiningNullableNotNullQuery2() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", "not null").stopNullable()
				.startNullablePart("sm.test2 = ?test2").setParameter("test2", "not null").stopNullable().end();
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE sm.test1 = ?test1 AND sm.test2 = ?test2",
				testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkParam("test2", "not null", testBuilder);
	}

	@Test
	public void testCombinedJoinNullableNotNullQuery() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", "not null").stopNullable()
				.startNullablePart("sm.test2 = ?test2").setParameter("test2", "not null").stopNullable()
				.startOr()
				.__().append("sm.test3 = 'test3'")
				.__().appendNullableList("sm.test4", "pr", Arrays.asList(new String[] { "test41", "test42" }))
				.__().appendNullableList("sm.test5", "wr", Collections.emptyList())
				.end();

		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test1 = ?test1 AND sm.test2 = ?test2 AND ( sm.test3 = 'test3' OR sm.test4 IN (?pr1, ?pr2) )",
				testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkParam("test2", "not null", testBuilder);
		checkParam("pr1", "test41", testBuilder);
		checkParam("pr2", "test42", testBuilder);
	}

	@Test
	public void testNestedCombinedJoinNullableNotNullQuery() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startNullablePart("sm.test1 = ?test1").setParameter("test1", "not null").stopNullable()
				.startNullablePart("sm.test2 = ?test2").setParameter("test2", "not null").stopNullable()
				.startOr()
				.__().append("sm.test3 = 'test3'")
				.__().startOr()
				.____().append("1 = 1")
				.____().startOr()
				.______().append("2 = 2")
				.______().append("3 = 3")
				.____().stopOr()
				.____().append("4 = 4")
				.__().stopOr()
				.appendNullableList("sm.test4", "pr", Arrays.asList(new String[] { "test41", "test42" }))
				.appendNullableList("sm.test5", "wr", Collections.emptyList()).startOr()
				.append("sm.test6 = 'test6'")
				.appendNullableList("sm.test7", "zr", Arrays.asList(new String[] { "test71", "test72" }))
				.appendNullableList("sm.test8", "xr", Collections.emptyList()).end();

		Assert.assertEquals(
				"SELECT sm.UUID FROM SpaceMission sm WHERE sm.test1 = ?test1 AND sm.test2 = ?test2 AND ( sm.test3 = 'test3' OR ( 1 = 1 OR ( 2 = 2 OR 3 = 3 ) OR 4 = 4 ) OR sm.test4 IN (?pr1, ?pr2) OR ( sm.test6 = 'test6' OR sm.test7 IN (?zr1, ?zr2) ) )",
				testBuilder.build());
		checkParam("test1", "not null", testBuilder);
		checkParam("test2", "not null", testBuilder);
		checkParam("pr1", "test41", testBuilder);
		checkParam("pr2", "test42", testBuilder);
		checkNoParam("wr1", testBuilder);
		checkNoParam("wr2", testBuilder);
		checkParam("zr1", "test71", testBuilder);
		checkParam("zr2", "test72", testBuilder);
		checkNoParam("xr1", testBuilder);
		checkNoParam("xr2", testBuilder);
	}

	@Test
	public void testNestedCombinedIfQuery() {
		QueryComposer testBuilder = QueryComposer.nativ("sm")
				.select("sm")
				.from("SpaceMission sm")
				.startAndWhere()
				.startIf("0 = 0").addCondition(true).stopIf()
				.startIf(" AND 7 = 7").addCondition(false).stopIf()
				.startIf()
				.__().append("6 = 6")
				.__().startIf()
				.____().append(" AND 1 = 1")
				.____().startIf()
				.______().append(" AND 2 = 2")
				.______().append(" AND 3 = 3")
				.______().setCondition(false)
				.____().stopIf()
				.____().append(" AND 4 = 4")
				.____().setCondition(false)
				.__().stopIf()
				.stopIf()
				.stopIf()
				.append(" AND 9 = 9")
				.end();

		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE 0 = 0 AND 6 = 6 AND 9 = 9", testBuilder.build());
	}

	@Test
	public void testNestedCombinedIfElseQuery() {
		QueryComposer testBuilder = QueryComposer.nativ("sm").select("sm").from("SpaceMission sm").startAndWhere()
				.startIf("0 = 0").addCondition(false).startElse("00 = 00").stopElse().startIf(" 7 = 7")
				.addCondition(false).startElse("77 = 77").stopElse().startIf().append("6 = 6").startIf()
				.append(" AND 1 = 1").stopIf().append(" AND 9 = 9").setCondition(false).stopIf().end();

		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm WHERE 00 = 00 AND 77 = 77", testBuilder.build());
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
