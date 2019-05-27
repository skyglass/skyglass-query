package skyglass.query.composer;

import java.util.Arrays;
import java.util.function.Consumer;

import org.apache.commons.collections.CollectionUtils;
import org.junit.Assert;
import org.junit.Test;

import skyglass.query.QueryFunctions;
import skyglass.query.QueryOrderUtil;
import skyglass.query.QueryRequestUtil;
import skyglass.query.QuerySearchUtil;
import skyglass.query.QueryTranslationUtil;
import skyglass.query.builder.FieldType;
import skyglass.query.builder.OrderBuilder;
import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.SearchBuilder;
import skyglass.query.builder.string.MockQueryRequestDto;

public class QueryComposerTest {

	//TODO: create test with search which requires only basic parameters, but set to distinct. Therefore it should be wrapped in the outer query, but only once

	@Test
	public void testQueryComposerOrderAndSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setOrderField("createdAt");
		queryRequest.setOrderType(OrderType.Desc);
		queryRequest.setSearchTerms(Arrays.asList(new String[] { "test1", "test2" }));

		String expectedResult = getExpectedResult(queryRequest);

		QueryComposer queryComposer = createQueryComposer(queryRequest, null);
		String result = queryComposer.getQueryStr(queryRequest);
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithoutOrderAndSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("");

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID LEFT "
				+ "JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate ORDER BY sm.createdAt DESC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, null);
		String result = queryComposer.getQueryStr(queryRequest);
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithSearchWithoutWrappingQuery() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID LEFT "
				+ "JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "createdBy", "bparamValue");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " WHERE " + searchPart + " ORDER BY sm.createdAt DESC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"));
		String result = queryComposer.getQueryStr(queryRequest);
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithDistinctBasicSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID LEFT "
				+ "JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "createdBy", "bparamValue");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " WHERE " + searchPart + " ORDER BY sm.createdAt DESC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"));
		queryComposer.setDistinct();
		String result = queryComposer.getQueryStr(queryRequest);
		Assert.assertEquals(expectedResult, result);
	}

	private QueryComposer createQueryComposer(QueryRequestDTO queryRequest, Consumer<QueryComposer> searchConsumer) {
		QueryComposer queryComposer = new QueryComposer(queryRequest, "sm", "SPACEMISSION");

		String languageCode = QueryRequestUtil.getCurrentLanguageCode(queryRequest);

		queryComposer.addSearchParameters();

		queryComposer.addSelect("sm.UUID, sm.planetId, sm.from, sm.destination, sm.currentPosition, sm.operator, user.name AS createdBy, bparam.value AS bparamValue, "
				+ QueryFunctions.ordinalToString(Direction.values(), "sm.direction") + " AS direction");
		addPlanetInfoSelectPart(queryComposer, languageCode);
		queryComposer.add("FROM SPACEMISSION sm ");
		queryComposer.addConditional("JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid ", "planetId");
		queryComposer.addConditional("JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID ", "planetName");
		queryComposer.addConditional("JOIN USER user ON sm.CREATEDBY_UUID = user.uuid ", "createdBy");
		queryComposer.addConditional("LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID ", "planetDescription");
		queryComposer.addConditional("LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID ", "bparamValue");
		queryComposer.add("WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate");
		if (searchConsumer == null) {
			queryComposer.addSearch("sm.planetId", "sm.from", "sm.destination", "sm.operator", "createdBy", "bparamValue", "direction", "planetName", "planetDescription", "localPlanetName",
					"localPlanetDescription");
		} else {
			searchConsumer.accept(queryComposer);
		}
		//queryComposer.setDistinct();

		queryComposer.setDefaultOrder(OrderType.Desc, FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("createdAt", FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("planetId", "sm.planetId");
		queryComposer.bindOrder("from", "sm.from");
		queryComposer.bindOrder("destination", "sm.destination");
		queryComposer.bindOrder("currentPosition", "sm.currentPosition");
		queryComposer.bindOrder("operator", "sm.operator");
		queryComposer.bindOrder("createdBy", "user.name");
		queryComposer.bindOrder("direction", "direction");

		return queryComposer;
	}

	private String getExpectedResult(QueryRequestDTO queryRequest) {
		String fromBasicQueryStr = "SELECT sm.UUID, sm.planetId, sm.from, sm.destination, sm.currentPosition, sm.operator, user.name AS createdBy, bparam.value AS bparamValue, "
				+ "CASE sm.direction WHEN 0 THEN 'IN' WHEN 1 THEN 'OUT' WHEN 2 THEN 'NONE' END AS direction, "
				+ "COALESCE(trName.en, trName.de, trName.cn, trName.jp, trName.es, trName.fr, trName.pt, trName.it) AS planetName, "
				+ "COALESCE(trDescription.en, trDescription.de, trDescription.cn, trDescription.jp, trDescription.es, trDescription.fr, trDescription.pt, trDescription.it) AS planetDescription, "
				+ "COALESCE(trLocalName.en, trLocalName.de, trLocalName.cn, trLocalName.jp, trLocalName.es, trLocalName.fr, trLocalName.pt, trLocalName.it) AS localPlanetName, "
				+ "COALESCE(trLocalDescription.en, trLocalDescription.de, trLocalDescription.cn, trLocalDescription.jp, trLocalDescription.es, trLocalDescription.fr, trLocalDescription.pt, trLocalDescription.it) AS localPlanetDescription, "
				+ "sm.createdAt "
				+ "FROM SPACEMISSION sm "
				+ "JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid "
				+ "JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID ";

		String whereQueryStr = "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate ";

		String queryCompositeStr = null;
		String countQueryCompositeStr = null;
		String queryStr = null;
		String countQueryStr = null;

		String selectOuterQueryCompositeStr = "SELECT tab.UUID, tab.createdAt ";
		String selectInnerQueryCompositeStr = "SELECT tab.UUID, tab.createdAt ";

		String fromQueryStr = "FROM ( "
				+ selectInnerQueryCompositeStr
				+ "FROM ( "
				+ fromBasicQueryStr
				+ whereQueryStr
				+ ") tab"
				+ getSearchPart(queryRequest)
				+ " ) tab "
				+ "GROUP BY tab.UUID, tab.createdAt ";

		queryCompositeStr = selectOuterQueryCompositeStr + fromQueryStr + getOrderByPart(queryRequest) + getPagedPart(queryRequest);
		countQueryCompositeStr = "SELECT DISTINCT COUNT(*) OVER () " + fromQueryStr;

		String selectQueryStr = "SELECT sm.UUID ";
		queryStr = selectQueryStr + fromBasicQueryStr + whereQueryStr + getBasicOrderByPart(queryRequest) + getPagedPart(queryRequest);
		countQueryStr = "SELECT COUNT(*) " + fromBasicQueryStr + whereQueryStr;

		return queryCompositeStr;
	}

	private String getSearchPart(QueryRequestDTO request) {
		if (CollectionUtils.isEmpty(request.getSearchTerms())) {
			return "";
		}
		String searchPart = null;
		for (int i = 0; i < request.getSearchTerms().size(); i++) {
			String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + Integer.toString(i);
			SearchBuilder searchBuilder = new SearchBuilder(request, searchTermField, false, "tab.planetId", "tab.from", "tab.destination", "tab.operator", "tab.createdBy",
					"tab.bparamValue", "tab.direction", "tab.planetName", "tab.planetDescription", "tab.localPlanetName", "tab.localPlanetDescription");
			searchPart = QueryFunctions.and(searchPart, QuerySearchUtil.applySearch(true, searchBuilder));
		}

		return searchPart == null ? "" : (" WHERE " + searchPart);
	}

	private String getPagedPart(QueryRequestDTO request) {
		return QueryRequestUtil.isPaged(request) ? " LIMIT ?limit OFFSET ?offset" : "";
	}

	private String getOrderByPart(QueryRequestDTO request) {
		OrderBuilder orderBuilder = new OrderBuilder(request);
		orderBuilder.setDefaultOrder(OrderType.Desc, FieldType.Date, "tab.createdAt");
		orderBuilder.bindOrder("createdAt", FieldType.Date, "tab.createdAt");
		orderBuilder.bindOrder("planetId", "tab.planetId");
		orderBuilder.bindOrder("from", "tab.from");
		orderBuilder.bindOrder("destination", "tab.destination");
		orderBuilder.bindOrder("currentPosition", "tab.currentPosition");
		orderBuilder.bindOrder("operator", "tab.operator");
		orderBuilder.bindOrder("createdBy", "tab.createdBy");
		orderBuilder.bindOrder("planetName", "COALESCE(tab.planetName, tab.localPlanetName)");
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getBasicOrderByPart(QueryRequestDTO request) {
		OrderBuilder orderBuilder = new OrderBuilder(request);
		orderBuilder.setDefaultOrder(OrderType.Desc, FieldType.Date, "sm.createdAt");
		orderBuilder.bindOrder("createdAt", FieldType.Date, "sm.createdAt");
		orderBuilder.bindOrder("planetId", "sm.planetId");
		orderBuilder.bindOrder("from", "sm.from");
		orderBuilder.bindOrder("destination", "sm.destination");
		orderBuilder.bindOrder("currentPosition", "sm.currentPosition");
		orderBuilder.bindOrder("operator", "sm.operator");
		orderBuilder.bindOrder("createdBy", "user.name");
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private void addPlanetInfoSelectPart(QueryComposer queryComposer, String languageCode) {
		queryComposer.addSelect("planetName", QueryTranslationUtil.getTranslatedField(languageCode, "trName"));
		queryComposer.addSelect("planetDescription", QueryTranslationUtil.getTranslatedField(languageCode, "trDescription"));
		queryComposer.addSelect("localPlanetName", QueryTranslationUtil.getTranslatedField(languageCode, "trLocalName"));
		queryComposer.addSelect("localPlanetDescription", QueryTranslationUtil.getTranslatedField(languageCode, "trLocalDescription"));
	}

}
