package skyglass.query.builder.composer;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.composer.QueryComposer;
import skyglass.query.builder.composer.QueryParam;
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
				.bindOrder("order");
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
				.bindOrder("order");
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
				.bindOrder("order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.order FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.order FROM SpaceMission sm WHERE sm.test = ?test GROUP BY sm.UUID, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	
	@Test
	public void testNullableNotNullQuery1DistinctGroupByPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("sm.groupBy as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addDistinctConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("sm.groupBy as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, sm.groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupBy() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("sm.groupBy as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order");
		Assert.assertEquals("SELECT sm.UUID, sm.groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy ORDER BY LOWER(sm.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID, sm.groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, sm.groupBy ORDER BY LOWER(sm.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("g.field as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field AS groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, sm.order", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field AS groupBy, sm.order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, sm.order ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAlias() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("g.field as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "sm.order");
		Assert.assertEquals("SELECT sm.UUID, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field ORDER BY LOWER(sm.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field ORDER BY LOWER(sm.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasOrderPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("g.field as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "g.field2")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field AS groupBy, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field AS groupBy, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderPaged() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("order")
				.groupBy("g.field as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "g.field2")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2, g.field ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2, g.field", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2, g.field ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderSamePaged1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("order")
				.groupBy("order")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "g.field2")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderSamePaged2() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.addSelect("order", "g.field2")
				.groupBy("order")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderSamePaged2SkipPart1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.addSelect("order", "g.field2")
				.groupBy("order")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditional("LEFT JOIN GROUPBY2 g2 ON g2.UUID = sm.groupBy_uuid", "order2")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderSamePaged2SkipPart2() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order2");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.addSelect("order2", "g.field2")
				.groupBy("order2")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditional("LEFT JOIN GROUPBY2 g2 ON g2.UUID = sm.groupBy_uuid", "order2")
				.addConditionalWhere("sm.test = ?test", "order2")
				.bindOrder("order2")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order2 FROM ( SELECT sm.UUID, g.field2 AS order2 FROM SpaceMission sm LEFT JOIN GROUPBY2 g2 ON g2.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY2 g2 ON g2.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order2 FROM SpaceMission sm LEFT JOIN GROUPBY2 g2 ON g2.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order2) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectOrderSamePaged3() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("order")
				.addGroupBy("order", "g.field2")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab ORDER BY LOWER(tab.order) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectSamePaged1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("g.field2 as order")
				.groupBy("order")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectSamePaged2() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.addSelect("order", "g.field2")
				.groupBy("order")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectSamePaged3() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.addSelect("order", "order")
				.addGroupBy("order", "g.field2")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasSelectSamePaged4() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("order")
				.addGroupBy("order", "g.field2")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "order")
				.addConditionalWhere("sm.test = ?test", "order")
				.setLimit(10);
		Assert.assertEquals("SELECT tab.UUID, tab.order FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT tab.UUID FROM ( SELECT sm.UUID, g.field2 AS order FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field2 ) tab", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1GroupByAliasOrder() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("order");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.groupBy("g.field as groupBy")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "groupBy")
				.addConditionalWhere("sm.test = ?test", "order")
				.bindOrder("order", "g.field2");
		Assert.assertEquals("SELECT sm.UUID, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field ORDER BY LOWER(g.field2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT DISTINCT COUNT(1) OVER () FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID, g.field AS groupBy FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test GROUP BY sm.UUID, g.field ORDER BY LOWER(g.field2) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1OrderByAliasSearch1() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("search");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "search")
				.addConditionalWhere("sm.test = ?test", "search")
				.addSearch("search")
				.bindOrder("search", "g.field2");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test ORDER BY LOWER(g.field2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test ORDER BY LOWER(g.field2) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1OrderByAliasSearch1Found() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("search");
		request.setOrderType(OrderType.Desc);
		request.setSearchTerm("test");
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "search")
				.addConditionalWhere("sm.test = ?test", "search")
				.addSearch("search")
				.bindOrder("search", "g.field2");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) ) ORDER BY LOWER(g.field2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) )", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) ) ORDER BY LOWER(g.field2) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1OrderByAliasSearch2() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("search");
		request.setOrderType(OrderType.Desc);
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "search")
				.addConditionalWhere("sm.test = ?test", "search2")
				.addAliasResolver("search2", "sm.found")
				.addSearch("search", "search2")
				.bindOrder("search", "g.field2");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test ORDER BY LOWER(g.field2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test ORDER BY LOWER(g.field2) DESC", testBuilder.buildUuidListPart());
		checkParam("test", "not null", testBuilder);
	}
	
	@Test
	public void testNullableNotNullQuery1OrderByAliasSearch2Found() {
		String value = "not null";

		QueryRequestDTO request = MockQueryMapRequestDto.create(value);
		request.setOrderField("search");
		request.setOrderType(OrderType.Desc);
		request.setSearchTerm("test");
		QueryComposer testBuilder = QueryComposer
				.nativ(request, "sm")
				.select("*")
				.add("FROM SpaceMission sm")
				.addConditional("LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid", "search")
				.addConditionalWhere("sm.test = ?test", "search2")
				.addAliasResolver("search2", "sm.found")
				.addSearch("search", "search2")
				.bindOrder("search", "g.field2");
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) OR LOWER(sm.found) LIKE LOWER(?searchTerm0) ) ORDER BY LOWER(g.field2) DESC", testBuilder.build());
		Assert.assertEquals("SELECT COUNT(1) FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) OR LOWER(sm.found) LIKE LOWER(?searchTerm0) )", testBuilder.buildCountPart());
		Assert.assertEquals("SELECT sm.UUID FROM SpaceMission sm LEFT JOIN GROUPBY g ON g.UUID = sm.groupBy_uuid WHERE sm.test = ?test AND ( LOWER(g.field2) LIKE LOWER(?searchTerm0) OR LOWER(sm.found) LIKE LOWER(?searchTerm0) ) ORDER BY LOWER(g.field2) DESC", testBuilder.buildUuidListPart());
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
