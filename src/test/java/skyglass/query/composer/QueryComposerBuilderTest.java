package skyglass.query.composer;

import java.util.Arrays;
import java.util.Date;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
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
import skyglass.query.builder.string.QueryComposer;
import skyglass.query.builder.string.QueryComposerBuilder;

public class QueryComposerBuilderTest {

	@Test
	public void testQueryComposer1() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setLang("en");
		queryRequest.setOrderField("planetDescription");
		queryRequest.setOrderType(OrderType.Desc);
		queryRequest.setSearchTerms(Arrays.asList(new String[] { "test1" }));
		queryRequest.setLimit(10);

		String expectedResult = getExpectedResult1(queryRequest, QueryTranslationUtil.coalesce(queryRequest.getLang(), "trDescription"), "planetDescription");

		QueryComposer queryComposer = createQueryComposer1(queryRequest, true);

		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
		
		queryRequest.setOrderField("planetName");
		queryComposer.addAliasResolver("localName", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trName"));
		queryComposer.addAliasResolver("localPlanetName", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trLocalName"));
		queryComposer.addAliasResolver("planetName", QueryFunctions.coalesce("${localName}", "${localPlanetName}"));
		result = queryComposer.getQueryStr();
		expectedResult = getExpectedResult1(queryRequest, QueryFunctions.coalesce(
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trName"), 
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trLocalName")), 
				"planetName");
		Assert.assertEquals(expectedResult, result);
		
		
		queryRequest.setOrderField("createdBy");
		result = queryComposer.getQueryStr();
		expectedResult = getExpectedResult1(queryRequest, "user.name", "createdBy");
		Assert.assertEquals(expectedResult, result);
		
		queryRequest.setOrderField("planetName");
		queryRequest.setLang("de");
		queryComposer.addAliasResolver("planetDescription", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trDescription"));
		queryComposer.addAliasResolver("localName", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trName"));
		queryComposer.addAliasResolver("localPlanetName", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trLocalName"));
		queryComposer.addAliasResolver("planetName", QueryFunctions.coalesce("${localName}", "${localPlanetName}"));
		result = queryComposer.getQueryStr();
		expectedResult = getExpectedResult1(queryRequest, QueryFunctions.coalesce(
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trName"), 
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trLocalName")), 
				"planetName");
		Assert.assertEquals(expectedResult, result);
		
		queryRequest.setOrderField("createdBy");
		queryRequest.setOrderType(OrderType.Asc);
		queryRequest.setLimit(-1);
		queryComposer.setDistinct(false);
		expectedResult = getExpectedResult2(queryRequest, "user.name", "createdBy");
		result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
		
		queryRequest.setOrderField("planetName");
		queryRequest.setOrderType(OrderType.Asc);
		queryComposer.setDistinct(false);
		expectedResult = getExpectedResult2(queryRequest, QueryFunctions.coalesce(
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trName"), 
				QueryTranslationUtil.coalesce(queryRequest.getLang(), "trLocalName")), 
				"planetName");
		result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}
	
	private QueryComposer createQueryComposer1(QueryRequestDTO queryRequest, boolean distinct) {
		queryRequest.set("fromDate", new Date());
		queryRequest.set("toDate", new Date());
		QueryComposer queryComposer = QueryComposer.nativ(queryRequest, "sm");

		queryComposer.select("sm.UUID");
		queryComposer.add("FROM SPACEMISSION sm");

		queryComposer.addConditional("JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid", "planetId");
		queryComposer.addConditional("JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID", "planetName");
		queryComposer.addConditional("JOIN USER user ON sm.CREATEDBY_UUID = user.uuid", "createdBy");
		queryComposer.addConditional("LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID", "planetDescription");
		queryComposer.addConditional("LEFT JOIN PLANETINFO pi ON pi.planet_UUID = pl.UUID", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN TranslatedField trLocalName ON trLocalName.UUID = pi.nameI18n_UUID", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID", true, "bparamValue");
		queryComposer.addWhere("sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate");
		queryComposer.addSearch("sm.planetId", "sm.from", "sm.destination", "sm.operator", "createdBy", "planetDescription", "direction");
		
		queryComposer.addAliasResolver("planetDescription", QueryTranslationUtil.coalesce(queryRequest.getLang(), "trDescription"));
		queryComposer.addAliasResolver("direction", QueryFunctions.ordinalToString(Direction.values(), "sm.direction"));
		queryComposer.addAliasResolver("createdBy", "user.name");

		queryComposer.setDefaultOrder(OrderType.Desc, FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("createdAt", FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("planetId", "sm.planetId");
		queryComposer.bindOrder("planetName", "planetName");
		queryComposer.bindOrder("planetDescription", "tab.planetDescription");
		queryComposer.bindOrder("from", "sm.from");
		queryComposer.bindOrder("destination", "sm.destination");
		queryComposer.bindOrder("currentPosition", "sm.currentPosition");
		queryComposer.bindOrder("operator", "sm.operator");
		queryComposer.bindOrder("createdBy", "createdBy");
		queryComposer.bindOrder("direction", "direction");
		
		if (distinct) {
			queryComposer.setDistinct();
		}
		
		return queryComposer;
	}
	
	private String getExpectedResult1(QueryRequestDTO queryRequest, String orderField, String orderAlias) {
		String expectedResult = "SELECT tab.UUID FROM ( SELECT sm.UUID, "
				+ orderField + " AS " + orderAlias + " "
				+ "FROM SPACEMISSION sm "
				+ "JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid "
				+ (orderAlias.equals("planetName") ? "JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID " : "")
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate AND "
				+ "( ( LOWER(sm.planetId) LIKE LOWER(?searchTerm0) OR LOWER(sm.from) LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(sm.destination) LIKE LOWER(?searchTerm0) OR LOWER(sm.operator) LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(user.name) LIKE LOWER(?searchTerm0) OR LOWER(" + QueryTranslationUtil.coalesce(queryRequest.getLang(), "trDescription")
				+ ") LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(" + QueryFunctions.ordinalToString(Direction.values(), "sm.direction")
				+ ") LIKE LOWER(?searchTerm0) "
				+ ") ) GROUP BY sm.UUID, "
				+ orderField
				+ " ) tab"
				+ getOrderByPart(queryRequest);
		return expectedResult;
	}
	
	private String getExpectedResult2(QueryRequestDTO queryRequest, String orderField, String orderAlias) {
		String expectedResult = "SELECT sm.UUID "
				+ "FROM SPACEMISSION sm "
				+ "JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid "
				+ (orderAlias.equals("planetName") ? "JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID " : "")
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate AND "
				+ "( ( LOWER(sm.planetId) LIKE LOWER(?searchTerm0) OR LOWER(sm.from) LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(sm.destination) LIKE LOWER(?searchTerm0) OR LOWER(sm.operator) LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(user.name) LIKE LOWER(?searchTerm0) OR LOWER(" + QueryTranslationUtil.coalesce(queryRequest.getLang(), "trDescription")
				+ ") LIKE LOWER(?searchTerm0) "
				+ "OR LOWER(" + QueryFunctions.ordinalToString(Direction.values(), "sm.direction")
				+ ") LIKE LOWER(?searchTerm0) "
				+ ") ) ORDER BY LOWER("
				+ orderField + ") ASC";
		return expectedResult;
	}
	
	@Test
	public void testCountQueryComposer1() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setOrderField("createdAt");
		queryRequest.setOrderType(OrderType.Desc);
		queryRequest.setSearchTerms(Arrays.asList(new String[] { "test1" }));
		queryRequest.set("fromDate", new Date());
		queryRequest.set("toDate", new Date());

		String expectedResult = "SELECT COUNT(1) FROM SPACEMISSION sm "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate AND "
				+ "( ( LOWER(user.name) LIKE LOWER(?searchTerm0) ) )";

		QueryComposer queryComposer = QueryComposer.nativ(queryRequest, "sm");

		queryComposer.select("sm.UUID");
		queryComposer.add("FROM SPACEMISSION sm");

		queryComposer.addConditional("JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid", "planetId");
		queryComposer.addConditional("JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID", "planetName");
		queryComposer.addConditional("JOIN USER user ON sm.CREATEDBY_UUID = user.uuid", "createdBy");
		queryComposer.addConditional("LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID", "planetDescription");
		queryComposer.addConditional("LEFT JOIN PLANETINFO pi ON pi.planet_UUID = pl.UUID ", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN TranslatedField trLocalName ON trLocalName.UUID = pi.nameI18n_UUID", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID", false, "bparamValue");
		queryComposer.addWhere("sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate");
		queryComposer.addSearch("createdBy");

		queryComposer.setDefaultOrder(OrderType.Desc, FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("createdAt", FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("planetId", "sm.planetId");
		queryComposer.bindOrder("planetName", "COALESCE(tab.planetName, tab.localPlanetName)");
		queryComposer.bindOrder("planetDescription", "tab.planetDescription");
		queryComposer.bindOrder("from", "sm.from");
		queryComposer.bindOrder("destination", "sm.destination");
		queryComposer.bindOrder("currentPosition", "sm.currentPosition");
		queryComposer.bindOrder("operator", "sm.operator");
		queryComposer.bindOrder("createdBy", "createdBy");
		queryComposer.bindOrder("direction", "direction");
		
		queryComposer.addAliasResolver("createdBy", "user.name");

		String result = queryComposer.getCountQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerOrderAndSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setOrderField("createdAt");
		queryRequest.setOrderType(OrderType.Asc);
		queryRequest.setSearchTerms(Arrays.asList(new String[] { "test1", "test2" }));

		String expectedResult = getExpectedResult(queryRequest, false);

		QueryComposer queryComposer = createQueryComposer(queryRequest, null, false, false, null);
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithoutOrderAndSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("");

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "LEFT JOIN PLANETINFO pi ON pi.planet_UUID = pl.UUID LEFT JOIN TranslatedField trLocalName ON trLocalName.UUID = pi.nameI18n_UUID "
				+ "LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate ORDER BY sm.createdAt DESC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, null, false, false, "sm.uuid");
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithSearchWithoutWrappingQuery() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "user.name", "bparam.value");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " AND " + searchPart + " ORDER BY sm.createdAt DESC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, false, "sm.UUID");
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithGlobalDistinctSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");

		String expectedResult = getExpectedResult(queryRequest, true, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, false, null);
		queryComposer.setDistinct();
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithDistinctSearch() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");

		String expectedResult = getExpectedResult(queryRequest, true, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), true, false, null);
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithTranslatableOrder() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("planetDescription");
		queryRequest.setOrderType(OrderType.Asc);

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "user.name", "bparam.value");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " AND " + searchPart + " ORDER BY " 
		+ QueryFunctions.lower(QueryTranslationUtil.coalesce("trDescription")) + " ASC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, false, "sm.UUID");
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithDistinctTranslatableOrder() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("planetName");
		queryRequest.setPageNumber(1);

		String expectedResult = getExpectedResult(queryRequest, true, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, true, null);
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithOrderByFormulaField() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("planetName");

		String expectedResult = getExpectedResult(queryRequest, false, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, false, null);
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithOrderByAliasField() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("planetName");

		String expectedResult = getExpectedResult(queryRequest, false, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, false, null);
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithOrderByDistinctField() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("planetName");

		String expectedResult = getExpectedResult(queryRequest, true, "user.name", "bparam.value");

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("createdBy", "bparamValue"), false, true, null);
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	@Test
	public void testQueryComposerWithOrderWithoutWrappingQuery() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("createdBy");
		queryRequest.setOrderType(OrderType.Asc);

		String expectedResult = "SELECT sm.UUID FROM SPACEMISSION sm "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID "
				+ "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "bparam.value");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " AND " + searchPart + " ORDER BY LOWER(user.name) ASC";

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("bparamValue"), false, false, "sm.uuid");
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		String result = queryComposer.getQueryStr();
		Assert.assertEquals(expectedResult, result);
	}
	
	@Test
	public void testCountQueryComposerWithOrderWithoutWrappingQuery() {

		QueryRequestDTO queryRequest = MockQueryRequestDto.create("");
		queryRequest.setSearchTerm("test1");
		queryRequest.setOrderField("createdBy");
		queryRequest.setOrderType(OrderType.Asc);

		String expectedResult = "SELECT COUNT(1) FROM SPACEMISSION sm "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID "
				+ "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate";

		String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + "0";
		SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, "bparam.value");
		String searchPart = QuerySearchUtil.applySearch(true, searchBuilder);

		expectedResult += " AND " + searchPart;

		QueryComposer queryComposer = createQueryComposer(queryRequest, q -> q.addSearch("bparamValue"), false, false, "sm.uuid");
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		String result = queryComposer.getCountQueryStr();
		Assert.assertEquals(expectedResult, result);
	}

	private QueryComposer createQueryComposer(QueryRequestDTO queryRequest, Consumer<QueryComposer> searchConsumer, boolean distinctSearch, boolean distinctOrder, String selectString) {
		QueryComposer queryComposer = QueryComposer.nativ(queryRequest, "sm");
		queryRequest.set("fromDate", new Date());
		queryRequest.set("toDate", new Date());
		queryRequest.setLimit(10);

		String languageCode = QueryRequestUtil.getCurrentLanguageCode(queryRequest);

		String selectStr = selectString != null ? selectString
				: ("sm.UUID, sm.planetId, sm.from, sm.destination, sm.currentPosition, sm.operator, user.name AS createdBy, bparam.value AS bparamValue, "
						+ QueryFunctions.ordinalToString(Direction.values(), "sm.direction") + " AS direction, finalPlanetName, finalPlanetDescription");

		queryComposer.select(selectStr);
		if (selectString == null) {
			addPlanetInfoSelectPart(queryComposer, languageCode);
		}
		queryComposer.add("FROM SPACEMISSION sm");

		queryComposer.addConditional("JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid", "planetId");
		queryComposer.addConditional("JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID", "planetName");
		queryComposer.addConditional("JOIN USER user ON sm.CREATEDBY_UUID = user.uuid", "createdBy");
		queryComposer.addConditional("LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID", "planetDescription");
		queryComposer.addConditional("LEFT JOIN PLANETINFO pi ON pi.planet_UUID = pl.UUID", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN TranslatedField trLocalName ON trLocalName.UUID = pi.nameI18n_UUID", "localPlanetName");
		queryComposer.addConditional("LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID", distinctSearch, "bparamValue");
		queryComposer.addWhere("sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate");
		if (searchConsumer == null) {
			queryComposer.addSearch("sm.planetId", "sm.from", "sm.destination", "sm.operator", "createdBy", "bparamValue", "direction", "planetName", "planetDescription", "localPlanetName",
					"localPlanetDescription");
		} else {
			searchConsumer.accept(queryComposer);
		}
		//queryComposer.setDistinct();
		queryComposer.addAliasResolver("createdBy", "user.name");
		queryComposer.addAliasResolver("bparamValue", "bparam.value");
		queryComposer.addAliasResolver("planetName", QueryTranslationUtil.coalesce("trName"));
		queryComposer.addAliasResolver("planetDescription", QueryTranslationUtil.coalesce("trDescription"));
		queryComposer.addAliasResolver("localPlanetName", QueryTranslationUtil.coalesce("trLocalName"));
		queryComposer.addAliasResolver("localPlanetDescription", QueryTranslationUtil.coalesce("trLocalDescription"));
		queryComposer.addAliasResolver("direction", QueryFunctions.ordinalToString(Direction.values(), "sm.direction"));
		queryComposer.addAliasResolver("finalPlanetName",
				QueryFunctions.coalesce("${planetName}", "${localPlanetName}"));
		queryComposer.addAliasResolver("finalPlanetDescription",
				QueryFunctions.coalesce("${planetDescription}", "${localPlanetDescription}"));

		queryComposer.setDefaultOrder(OrderType.Desc, FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("createdAt", FieldType.Date, "sm.createdAt");
		queryComposer.bindOrder("planetId", "sm.planetId");
		queryComposer.bindOrder("planetName", "finalPlanetName");
		queryComposer.bindOrder("planetDescription", "planetDescription");
		queryComposer.bindOrder("from", "sm.from");
		queryComposer.bindOrder("destination", "sm.destination");
		queryComposer.bindOrder("currentPosition", "sm.currentPosition");
		queryComposer.bindOrder("operator", "sm.operator");
		queryComposer.bindOrder("createdBy", "createdBy");
		queryComposer.bindOrder("direction", "direction");

		if (distinctOrder) {
			queryComposer.setDistinct();
		}
		
		if (distinctSearch) {
			queryComposer.setDistinct();
		}

		return queryComposer;
	}

	private String getExpectedResult(QueryRequestDTO queryRequest, boolean distinct, String... searchFields) {
		String innerFields = "sm.UUID, sm.planetId, sm.from, sm.destination, sm.currentPosition, sm.operator, user.name AS createdBy, bparam.value AS bparamValue, "
				+ QueryFunctions.ordinalToString(Direction.values(), "sm.direction") + " AS direction, "
				+ QueryFunctions.coalesce(QueryTranslationUtil.coalesce("trName"), QueryTranslationUtil.coalesce("trLocalName")) + " AS finalPlanetName, "
				+ QueryFunctions.coalesce(QueryTranslationUtil.coalesce("trDescription"), QueryTranslationUtil.coalesce("trLocalDescription")) + " AS finalPlanetDescription, "
				+ QueryTranslationUtil.coalesce("trName") + " AS planetName, "
				+ QueryTranslationUtil.coalesce("trDescription") + " AS planetDescription, "
				+ QueryTranslationUtil.coalesce("trLocalName") + " AS localPlanetName, "
				+ QueryTranslationUtil.coalesce("trLocalDescription") + " AS localPlanetDescription, "
				+ "sm.createdAt";
		
		String groupByFields = "sm.UUID, sm.planetId, sm.from, sm.destination, sm.currentPosition, sm.operator, user.name, bparam.value, "
				+ QueryFunctions.ordinalToString(Direction.values(), "sm.direction") + ", "
				+ QueryFunctions.coalesce(QueryTranslationUtil.coalesce("trName"), QueryTranslationUtil.coalesce("trLocalName")) + ", "
				+ QueryFunctions.coalesce(QueryTranslationUtil.coalesce("trDescription"), QueryTranslationUtil.coalesce("trLocalDescription")) + ", "
				+ QueryTranslationUtil.coalesce("trName") + ", "
				+ QueryTranslationUtil.coalesce("trDescription") + ", "
				+ QueryTranslationUtil.coalesce("trLocalName") + ", "
				+ QueryTranslationUtil.coalesce("trLocalDescription") + ", "
				+ "sm.createdAt";
		String fromBasicQueryStr = "SELECT " + innerFields
				+ " FROM SPACEMISSION sm "
				+ "JOIN PLANET pl ON sm.PLANET_UUID = pl.uuid "
				+ "JOIN TranslatedField trName ON trName.UUID = pl.nameI18n_UUID "
				+ "JOIN USER user ON sm.CREATEDBY_UUID = user.uuid "
				+ "LEFT JOIN TranslatedField trDescription ON trDescription.UUID = pl.descriptionI18n_UUID "
				+ "LEFT JOIN PLANETINFO pi ON pi.planet_UUID = pl.UUID "
				+ "LEFT JOIN TranslatedField trLocalName ON trLocalName.UUID = pi.nameI18n_UUID "
				+ "LEFT JOIN BASICPARAMETER bparam ON bparam.SPACEMISSION_UUID = sm.UUID ";

		String whereQueryStr = "WHERE sm.createdAt >= ?fromDate AND sm.createdAt <= ?toDate ";

		String queryCompositeStr = null;

		String selectOuterQueryCompositeStr = "SELECT tab.UUID, tab.planetId, tab.from, tab.destination, tab.currentPosition, tab.operator, tab.createdBy, tab.bparamValue, tab.direction, tab.finalPlanetName, tab.finalPlanetDescription, tab.planetName, tab.planetDescription, tab.localPlanetName, tab.localPlanetDescription, tab.createdAt ";

		String fromQueryStr = fromBasicQueryStr
				+ whereQueryStr
				+ getSearchPart(queryRequest, searchFields);

		queryCompositeStr = fromQueryStr + getBasicOrderByPart(queryRequest) + getPagedPart(queryRequest);
		
		if (distinct) {
			queryCompositeStr = selectOuterQueryCompositeStr 
					+ "FROM ( " + fromQueryStr + " GROUP BY " + groupByFields + " ) tab" + getOrderByPart(queryRequest);
		}

		return queryCompositeStr;
	}

	private String getSearchPart(QueryRequestDTO request, String... searchFields) {
		if (!QueryComposerBuilder.shouldApplySearch(request)) {
			return "";
		}
		String searchPart = null;
		int size = request.getSearchTerms().size() > 0 ? request.getSearchTerms().size() : StringUtils.isNotBlank(request.getSearchTerm()) ? 1 : 0;
		for (int i = 0; i < size; i++) {
			String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + Integer.toString(i);
			SearchBuilder searchBuilder = null;
			if (searchFields.length > 0) {
				searchBuilder = new SearchBuilder(request, searchTermField, false, searchFields);
			} else {
				searchBuilder = new SearchBuilder(request, searchTermField, false, "sm.planetId", "sm.from", "sm.destination", "sm.operator", "user.name",
						"bparam.value", QueryFunctions.ordinalToString(Direction.values(), "sm.direction"),
						QueryTranslationUtil.coalesce("trName"),
						QueryTranslationUtil.coalesce("trDescription"),
						QueryTranslationUtil.coalesce("trLocalName"),
						QueryTranslationUtil.coalesce("trLocalDescription"));
			}
			searchPart = QueryFunctions.and(searchPart, QuerySearchUtil.applySearch(true, searchBuilder));
		}

		return searchPart == null ? "" : ("AND " + searchPart);
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
		orderBuilder.bindOrder("planetName", "tab.planetName");
		orderBuilder.bindOrder("planetDescription", "tab.planetDescription");
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
		orderBuilder.bindOrder("planetName", QueryFunctions.coalesce(QueryTranslationUtil.coalesce("trName"), QueryTranslationUtil.coalesce("trLocalName")));
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private void addPlanetInfoSelectPart(QueryComposer queryComposer, String languageCode) {
		queryComposer.addSelect("planetName", "planetName");
		queryComposer.addSelect("planetDescription", "planetDescription");
		queryComposer.addSelect("localPlanetName", "localPlanetName");
		queryComposer.addSelect("localPlanetDescription", "localPlanetDescription");
		queryComposer.addSelect("createdAt", "sm.createdAt");
	}

}
