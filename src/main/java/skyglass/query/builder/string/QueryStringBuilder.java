package skyglass.query.builder.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import skyglass.query.NativeQueryUtil;
import skyglass.query.builder.OrderBuilder;
import skyglass.query.builder.OrderField;
import skyglass.query.builder.OrderType;
import skyglass.query.builder.result.QueryManager;
import skyglass.query.builder.result.QueryResult;

public class QueryStringBuilder {

	private StringBuilder fromPart;

	private Map<String, QueryParam> params = new HashMap<>();

	private StringPartBuilder selectPart = new StringPartBuilder(this, null);

	private StringPartBuilder joinPart = new StringPartBuilder(this, null);

	private StringPartBuilder leftJoinPart = new StringPartBuilder(this, null);

	private StringPartBuilder joinFetchPart = new StringPartBuilder(this, null);

	private StringPartBuilder leftJoinFetchPart = new StringPartBuilder(this, null);

	private StringPartBuilder wherePart = new StringPartBuilder(this, null);

	private StringPartBuilder groupByPart = new StringPartBuilder(this, null);

	private StringPartBuilder havingPart = new StringPartBuilder(this, null);

	private StringPartBuilder orderByPart = new StringPartBuilder(this, null);

	private StringPartBuilder queryPart = new StringPartBuilder(this, null);

	private String distinctUuidPart;

	private String rootAlias;

	private QueryRequestDTO queryRequest;

	private OrderBuilder orderBuilder;

	private List<SelectField> selectFields = new ArrayList<SelectField>();

	private QueryType queryType;

	private StringBuilder mainSelectResult;

	private boolean distinct = false;

	QueryStringBuilder(QueryRequestDTO queryRequest, String rootAlias, boolean isNative) {
		this(rootAlias, isNative);
		this.queryRequest = queryRequest;
	}

	QueryStringBuilder(String rootAlias, boolean isNative) {
		this.rootAlias = rootAlias;
		this.queryType = isNative ? QueryType.Native : QueryType.Jpa;
	}

	//does nothing, only for indentation
	public QueryStringBuilder ___________________() {
		return this;
	}

	public static QueryStringBuilder nativ() {
		QueryStringBuilder result = new QueryStringBuilder(null, true);
		return result;
	}

	public static QueryStringBuilder nativ(QueryRequestDTO request) {
		QueryStringBuilder result = new QueryStringBuilder(request, null, true);
		return result;
	}

	public static QueryStringBuilder jpa(String rootAlias) {
		QueryStringBuilder result = new QueryStringBuilder(rootAlias, false);
		return result;
	}

	public static QueryStringBuilder jpa(QueryRequestDTO request, String rootAlias) {
		QueryStringBuilder result = new QueryStringBuilder(request, rootAlias, false);
		return result;
	}

	public StringPartBuilder start(String select, String from) {
		this.fromPart = new StringBuilder(from);
		return this.selectPart.start(select);
	}

	public QueryStringBuilder select(String select) {
		this.selectPart.build(new StringBuilder(select));
		return this;
	}

	public QueryStringBuilder select(String select, String from) {
		this.fromPart = new StringBuilder(from);
		this.selectPart.build(new StringBuilder(select));
		return this;
	}

	public StringPartBuilder startAndPart() {
		return this.queryPart.startAnd();
	}

	public StringPartBuilder startOrPart() {
		return this.queryPart.startOr();
	}

	public StringPartBuilder startPart() {
		return queryPart.start();
	}

	public StringPartBuilder startPart(String part) {
		return queryPart.start(part);
	}

	public QueryStringBuilder part(String part) {
		queryPart.build(new StringBuilder(part));
		return this;
	}

	public void setParam(QueryParam param) {
		params.put(param.getName(), param);
	}

	public QueryStringBuilder from(String from) {
		this.fromPart = new StringBuilder(from);
		return this;
	}

	public StringPartBuilder startJoin(String join) {
		return joinPart.start(join);
	}

	public QueryStringBuilder join(String join) {
		joinPart.build(new StringBuilder(join));
		return this;
	}

	public StringPartBuilder startLeftJoin(String join) {
		return leftJoinPart.start(join);
	}

	public QueryStringBuilder leftJoin(String join) {
		leftJoinPart.build(new StringBuilder(join));
		return this;
	}

	public StringPartBuilder startJoinFetch(String joinFetch) {
		return joinFetchPart.start(joinFetch);
	}

	public QueryStringBuilder joinFetch(String joinFetch) {
		joinFetchPart.build(new StringBuilder(joinFetch));
		return this;
	}

	public StringPartBuilder startLeftJoinFetch(String joinFetch) {
		return leftJoinFetchPart.start(joinFetch);
	}

	public QueryStringBuilder leftJoinFetch(String joinFetch) {
		leftJoinFetchPart.build(new StringBuilder(joinFetch));
		return this;
	}

	public StringPartBuilder startAndWhere() {
		return startAndWhere(null);
	}

	public StringPartBuilder startAndWhere(String where) {
		return wherePart.start(where).startAnd();
	}

	public StringPartBuilder startOrWhere() {
		return startOrWhere(null);
	}

	public StringPartBuilder startOrWhere(String where) {
		return wherePart.start(where).startOr();
	}

	public StringPartBuilder startGroupBy(String groupBy) {
		return groupByPart.start(groupBy);
	}

	public QueryStringBuilder groupBy(String groupBy) {
		groupByPart.build(new StringBuilder(groupBy));
		return this;
	}

	public StringPartBuilder startHaving(String having) {
		return havingPart.start(having);
	}

	public QueryStringBuilder having(String having) {
		havingPart.build(new StringBuilder(having));
		return this;
	}

	public StringPartBuilder startOrderBy(String orderBy) {
		return orderByPart.start(orderBy);
	}

	public QueryStringBuilder orderBy(String orderBy) {
		orderByPart.build(new StringBuilder(orderBy));
		return this;
	}

	public QueryStringBuilder setDistinct(String distinctUuidPart) {
		this.distinct = true;
		this.distinctUuidPart = distinctUuidPart;
		return this;
	}

	public QueryStringBuilder setPaging(int rowsPerPage, int pageNumber) {
		setRowsPerPage(rowsPerPage);
		setPageNumber(pageNumber);
		return this;
	}

	public QueryStringBuilder setLimit(int offset, int limit) {
		setOffset(offset);
		setLimit(limit);
		return this;
	}

	public QueryStringBuilder setOffset(int offset) {
		getQueryRequest().setOffset(offset);
		return this;
	}

	public QueryStringBuilder setLimit(int limit) {
		getQueryRequest().setLimit(limit);
		return this;
	}

	public QueryStringBuilder setRowsPerPage(int rowsPerPage) {
		getQueryRequest().setRowsPerPage(rowsPerPage);
		return this;
	}

	public QueryStringBuilder setPageNumber(int pageNumber) {
		getQueryRequest().setPageNumber(pageNumber);
		return this;
	}

	public QueryStringBuilder setSearchTerm(String searchTerm) {
		getQueryRequest().setSearchTerm(searchTerm);
		return this;
	}

	public QueryStringBuilder setOrderField(String orderField) {
		getQueryRequest().setOrderField(orderField);
		return this;
	}

	public QueryStringBuilder setOrderType(OrderType orderType) {
		getQueryRequest().setOrderType(orderType);
		return this;
	}

	public QueryStringBuilder setLang(String lang) {
		getQueryRequest().setLang(lang);
		return this;
	}

	public QueryStringBuilder bindOrder(String alias, String... orderFields) {
		getOrderBuilder().bindOrder(alias, orderFields);
		return this;
	}

	public QueryStringBuilder bindTranslatableOrder(String alias, String... orderFields) {
		getOrderBuilder().bindTranslatableOrder(alias, orderFields);
		return this;
	}

	public QueryStringBuilder addOrder(OrderType orderType, String... orderFields) {
		getOrderBuilder().addOrder(orderType, orderFields);
		return this;
	}

	public QueryStringBuilder setOrder(OrderType orderType, String... orderFields) {
		getOrderBuilder().setOrder(orderType, orderFields);
		return this;
	}

	public QueryStringBuilder setDefaultOrder(OrderType orderType, String... orderFields) {
		getOrderBuilder().setDefaultOrder(orderType, orderFields);
		return this;
	}

	public QueryStringBuilder setDefaultOrders(OrderType orderType, String... orderFields) {
		getOrderBuilder().setDefaultOrders(orderType, orderFields);
		return this;
	}

	public QueryStringBuilder setDefaultTranslatableOrder(OrderType orderType, String... orderFields) {
		getOrderBuilder().setDefaultTranslatableOrder(orderType, orderFields);
		return this;
	}

	public QueryStringBuilder setDefaultTranslatableOrders(OrderType orderType, String... orderFields) {
		getOrderBuilder().setDefaultTranslatableOrders(orderType, orderFields);
		return this;
	}

	public String build() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		if (distinct) {
			sb.append("DISTINCT ");
		}
		build(sb, buildMainSelectResult());
		sb.append(" FROM ");
		build(sb, fromPart);
		if (joinPart.hasResult()) {
			sb.append(" INNER JOIN ");
			build(sb, joinPart.getResult());
		}
		if (leftJoinPart.hasResult()) {
			sb.append(" LEFT JOIN ");
			build(sb, leftJoinPart.getResult());
		}
		if (joinFetchPart.hasResult()) {
			sb.append(" JOIN FETCH ");
			build(sb, joinFetchPart.getResult());
		}
		if (leftJoinFetchPart.hasResult()) {
			sb.append(" LEFT JOIN FETCH ");
			build(sb, leftJoinFetchPart.getResult());
		}
		if (wherePart.hasResult()) {
			sb.append(" WHERE ");
			build(sb, wherePart.getResult());
		}
		if (groupByPart.hasResult()) {
			sb.append(" GROUP BY ");
			build(sb, groupByPart.getResult());
		}
		if (havingPart.hasResult()) {
			sb.append(" HAVING ");
			build(sb, havingPart.getResult());
		}
		buildOrderByPart(sb);
		return sb.toString();
	}

	public String buildPart() {
		StringBuilder sb = new StringBuilder();
		build(sb, queryPart.getResult());
		return sb.toString();
	}

	public String buildResultFromUuidList(List<String> uuidList) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT ");
		build(sb, buildMainSelectResult());
		sb.append(" FROM ");
		build(sb, fromPart);
		if (joinPart.hasResult()) {
			sb.append(" INNER JOIN ");
			build(sb, joinPart.getResult());
		}
		if (leftJoinPart.hasResult()) {
			sb.append(" LEFT JOIN ");
			build(sb, leftJoinPart.getResult());
		}
		if (joinFetchPart.hasResult()) {
			sb.append(" JOIN FETCH ");
			build(sb, joinFetchPart.getResult());
		}
		if (leftJoinFetchPart.hasResult()) {
			sb.append(" LEFT JOIN FETCH ");
			build(sb, leftJoinFetchPart.getResult());
		}

		sb.append(" WHERE ");
		sb.append(distinctUuidPart);
		sb.append(" IN ");
		build(sb, new StringBuilder(NativeQueryUtil.getInString(uuidList)));

		if (groupByPart.hasResult()) {
			sb.append(" GROUP BY ");
			build(sb, groupByPart.getResult());
		}
		buildOrderByPart(sb);
		return sb.toString();
	}

	public String buildUuidListPart() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT tab.UUID FROM ( SELECT DISTINCT ");
		sb.append(distinctUuidPart);
		sb.append(" AS UUID");
		sb.append(" FROM ");
		build(sb, fromPart);
		if (wherePart.hasResult()) {
			sb.append(" WHERE ");
			build(sb, wherePart.getResult());
		}
		if (groupByPart.hasResult()) {
			sb.append(" GROUP BY ");
			build(sb, groupByPart.getResult());
		}
		if (havingPart.hasResult()) {
			sb.append(" HAVING ");
			build(sb, havingPart.getResult());
		}
		buildOrderByPart(sb);
		sb.append(" ) tab");

		return sb.toString();
	}

	public String buildCountPart() {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT COUNT(" + (isNativeQuery() ? "*" : rootAlias) + ") FROM ");
		build(sb, fromPart);
		if (wherePart.hasResult()) {
			sb.append(" WHERE ");
			build(sb, wherePart.getResult());
		}
		if (groupByPart.hasResult()) {
			sb.append(" GROUP BY ");
			build(sb, groupByPart.getResult());
		}
		if (havingPart.hasResult()) {
			sb.append(" HAVING ");
			build(sb, havingPart.getResult());
		}

		return sb.toString();
	}

	public <T, DTO> QueryResult<DTO> getDtoResult(QueryManager queryManager, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return queryManager.getDtoResult(this, entityDtoConverter, type);
	}

	public <T> QueryResult<T> getEntityResult(QueryManager queryManager, Class<T> type) {
		return queryManager.getEntityResult(this, type);
	}

	public QueryResult<Object[]> getNativeResult(QueryManager queryManager) {
		return queryManager.getNativeResult(this);
	}

	public <DTO> QueryResult<DTO> convertNativeResult(QueryManager queryManager, Supplier<DTO> dtoSupplier) {
		return queryManager.convertNativeResult(this, dtoSupplier);
	}

	public <DTO, DTO2> QueryResult<DTO2> convertNativeResult(QueryManager queryManager, Supplier<DTO> dtoSupplier,
			Function<DTO, DTO2> dtoDto2Converter) {
		return queryManager.convertNativeResult(this, dtoSupplier, dtoDto2Converter);
	}

	public <T, DTO> List<DTO> getDtoList(QueryManager queryManager, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return queryManager.getDtoList(this, entityDtoConverter, type);
	}

	public <T> List<T> getEntityList(QueryManager queryManager, Class<T> type) {
		return queryManager.getEntityList(this, type);
	}

	public List<Object[]> getNativeList(QueryManager queryManager) {
		return queryManager.getNativeList(this);
	}

	public <DTO> List<DTO> convertNativeList(QueryManager queryManager, Supplier<DTO> dtoSupplier) {
		return queryManager.convertNativeList(this, dtoSupplier);
	}

	public <DTO, DTO2> List<DTO2> convertNativeList(QueryManager queryManager, Supplier<DTO> dtoSupplier,
			Function<DTO, DTO2> dtoDto2Converter) {
		return queryManager.convertNativeList(this, dtoSupplier, dtoDto2Converter);
	}

	Collection<QueryParam> getParams() {
		return params.values();
	}

	protected boolean isNativeQuery() {
		return queryType == QueryType.Native;
	}

	public boolean isDistinct() {
		return distinct;
	}

	QueryStringBuilder buildOrder(List<OrderField> orderFields) {
		if (!orderByPart.isAlreadyBuilt()) {
			orderByPart.append(QueryProcessor.applyOrder(orderFields));
		}
		return this;
	}

	QueryStringBuilder buildSelect(List<SelectField> selectFields) {
		this.selectFields = selectFields;
		return this;
	}

	public List<SelectField> getSelectFields() {
		buildMainSelectResult();
		return selectFields;
	}

	private StringBuilder buildMainSelectResult() {
		if (mainSelectResult != null) {
			return mainSelectResult;
		}
		mainSelectResult = new StringBuilder();
		String selectResult = selectPart.getResult().toString();
		if (StringUtils.isNotBlank(selectResult)) {
			mainSelectResult.append(selectResult);
			if (CollectionUtils.isNotEmpty(selectFields)) {
				mainSelectResult.append(", ");
			}
		}
		if (CollectionUtils.isNotEmpty(selectFields)) {
			mainSelectResult.append(QueryProcessor.applySelect(selectFields));
		}
		selectFields.addAll(QueryProcessor.parseSelect(selectResult));
		return mainSelectResult;
	}

	private void build(StringBuilder result, StringBuilder part) {
		if (part != null && part.length() > 0) {
			result.append(part);
		}
	}

	public QueryRequestDTO getQueryRequest() {
		if (queryRequest == null) {
			queryRequest = new QueryRequestDTO();
		}
		return queryRequest;
	}

	private OrderBuilder getOrderBuilder() {
		if (orderBuilder == null) {
			orderBuilder = new OrderBuilder(getQueryRequest());
		}
		return orderBuilder;
	}

	private void buildOrderByPart(StringBuilder sb) {
		buildOrder(getOrderBuilder().getOrderFields());
		if (orderByPart.hasResult()) {
			sb.append(" ORDER BY ");
			build(sb, orderByPart.getResult());
		}
	}

}
