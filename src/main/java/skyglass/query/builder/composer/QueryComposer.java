package skyglass.query.builder.composer;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;

import skyglass.data.common.model.IdObject;
import skyglass.query.NativeQueryUtil;
import skyglass.query.QueryRequestUtil;
import skyglass.query.builder.FieldType;
import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.QueryResult;
import skyglass.query.builder.SearchType;
import skyglass.query.builder.composer.search.SearchTerm;
import skyglass.query.builder.config.Constants;
import skyglass.query.builder.result.QueryManager;

public class QueryComposer {

	private StringBuilder fromPart;

	private Map<String, QueryParam> params = new HashMap<>();

	private StringPartBuilder joinPart = new StringPartBuilder(this, null);

	private StringPartBuilder leftJoinPart = new StringPartBuilder(this, null);

	private StringPartBuilder joinFetchPart = new StringPartBuilder(this, null);

	private StringPartBuilder leftJoinFetchPart = new StringPartBuilder(this, null);

	private StringPartBuilder wherePart = new StringPartBuilder(this, null);

	private StringPartBuilder havingPart = new StringPartBuilder(this, null);

	private StringPartBuilder queryPart = new StringPartBuilder(this, null);

	private String rootAlias;

	private QueryRequestDTO queryRequest;

	private QueryType queryType;
	
	private QueryComposerBuilder queryComposer;
	
	private boolean hasCustomWherePart;
	
	private String uuidField = Constants.UUID;
	
	private String uuidAlias = Constants.UUID_ALIAS;
	
	private boolean skipUuid = false;

	QueryComposer(QueryRequestDTO queryRequest, String rootAlias, boolean isNative) {
		init(rootAlias, isNative);
		this.queryRequest = queryRequest;
		this.queryComposer = new QueryComposerBuilder(this, queryRequest, rootAlias);
	}
	
	QueryComposer(QueryRequestDTO queryRequest, String rootAlias) {
		this(queryRequest, rootAlias, true);
	}

	QueryComposer(String rootAlias, boolean isNative) {
		this(null, rootAlias, isNative);
	}
	
	private void init(String rootAlias, boolean isNative) {
		this.rootAlias = rootAlias;
		this.queryType = isNative ? QueryType.Native : QueryType.Jpa;
	}
	
	private void initComposer() {
		queryComposer.resetAndInit();
	}

	//does nothing, only for indentation
	public QueryComposer ___________________() {
		return this;
	}

	public static QueryComposer nativ(String rootAlias) {
		QueryComposer result = new QueryComposer(rootAlias, true);
		return result;
	}
	
	public static QueryComposer nativ(QueryRequestDTO request, String rootAlias) {
		QueryComposer result = new QueryComposer(request, rootAlias, true);
		return result;
	}

	public static QueryComposer jpa(String rootAlias) {
		return jpa(null, rootAlias);
	}

	public static QueryComposer jpa(QueryRequestDTO request, String rootAlias) {
		QueryComposer result = new QueryComposer(request, rootAlias, false);
		return result;
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

	public QueryComposer part(String part) {
		queryPart.build(new StringBuilder(part));
		return this;
	}

	public void setParam(QueryParam param) {
		params.put(param.getName(), param);
	}

	public QueryComposer from(String from) {
		this.fromPart = new StringBuilder(from);
		return this;
	}

	public StringPartBuilder startJoin(String join) {
		return joinPart.start(join);
	}

	public QueryComposer join(String join) {
		joinPart.build(new StringBuilder(join));
		return this;
	}

	public StringPartBuilder startLeftJoin(String join) {
		return leftJoinPart.start(join);
	}

	public QueryComposer leftJoin(String join) {
		leftJoinPart.build(new StringBuilder(join));
		return this;
	}

	public StringPartBuilder startJoinFetch(String joinFetch) {
		return joinFetchPart.start(joinFetch);
	}

	public QueryComposer joinFetch(String joinFetch) {
		joinFetchPart.build(new StringBuilder(joinFetch));
		return this;
	}

	public StringPartBuilder startLeftJoinFetch(String joinFetch) {
		return leftJoinFetchPart.start(joinFetch);
	}

	public QueryComposer leftJoinFetch(String joinFetch) {
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

	public QueryComposer groupBy(String groupByString) {
		queryComposer.groupBy(groupByString);
		return this;
	}

	public QueryComposer addGroupBy(String alias, String path) {
		queryComposer.addGroupBy(alias, path);
		return this;
	}
	
	public QueryComposer addGroupBy(String alias) {
		return addGroupBy(alias, alias);
	}

	public StringPartBuilder startHaving(String having) {
		return havingPart.start(having);
	}

	public QueryComposer having(String having) {
		havingPart.build(new StringBuilder(having));
		return this;
	}

	public QueryComposer setPaging(int rowsPerPage, int pageNumber) {
		setRowsPerPage(rowsPerPage);
		setPageNumber(pageNumber);
		return this;
	}

	public QueryComposer setLimit(int offset, int limit) {
		setOffset(offset);
		setLimit(limit);
		return this;
	}

	public QueryComposer setOffset(int offset) {
		getQueryRequest().setOffset(offset);
		return this;
	}

	public QueryComposer setLimit(int limit) {
		getQueryRequest().setLimit(limit);
		return this;
	}

	public QueryComposer setRowsPerPage(int rowsPerPage) {
		getQueryRequest().setRowsPerPage(rowsPerPage);
		return this;
	}

	public QueryComposer setPageNumber(int pageNumber) {
		getQueryRequest().setPageNumber(pageNumber);
		return this;
	}

	public QueryComposer setSearchTerm(String searchTerm) {
		getQueryRequest().setSearchTerm(searchTerm);
		return this;
	}

	public QueryComposer setOrderField(String orderField) {
		getQueryRequest().setOrderField(orderField);
		return this;
	}

	public QueryComposer setOrderType(OrderType orderType) {
		getQueryRequest().setOrderType(orderType);
		return this;
	}

	public QueryComposer setLang(String lang) {
		getQueryRequest().setLang(lang);
		return this;
	}

	public QueryComposer addOrder(OrderType orderType, String... orderFields) {
		queryComposer.addDefaultOrder(orderType, orderFields);
		return this;
	}

	public QueryComposer setOrder(OrderType orderType, String... orderFields) {
		queryComposer.setDefaultOrder(orderType, orderFields);
		return this;
	}

	public String build() {
		return build(false);
	}
	
	public String buildUuidListPart() {
		return build(true);
	}
	
	private String build(boolean isUuids) {
		initComposer();
		StringBuilder sb = new StringBuilder();
		if (applyOuterQuery()) {
			buildSelectPart(sb, isUuids, false);
			sb.append(" FROM ( ");
			buildInner(sb, isUuids, false, false);
			sb.append(" ) " + Constants.OUTER_QUERY_PREFIX);
			buildOrderByPart(sb, false);
		} else {
			buildInner(sb, isUuids, false, false);
		}
		return sb.toString();
	}
	
	public String buildCountPart() {
		initComposer();
		StringBuilder sb = new StringBuilder();
		if (applyDistinctCount()) {
			sb.append("SELECT DISTINCT COUNT(1) OVER ()");
			buildInner(sb, false, true, false);
		} else {
			sb.append("SELECT COUNT(" + (isNativeQuery() ? "1" : rootAlias) + ")");
			buildInner(sb, false, true, false);
		}
		return sb.toString();
	}
	
	public String buildResultFromUuidList(List<String> uuidList) {
		initComposer();
		StringBuilder sb = new StringBuilder();
		buildInner(sb, false, false, true);
		sb.append(" WHERE ");
		sb.append(rootAlias + "." + getUuidField());
		sb.append(" IN ");
		build(sb, new StringBuilder(NativeQueryUtil.getInString(uuidList)));
		return sb.toString();
	}
	

	
	private void buildInner(StringBuilder sb, boolean isUuids, boolean isCount, boolean fromUuidList) {
		if (!isCount) {
			buildSelectPart(sb, isUuids, true);
		}
		if (fromPart != null) {
			sb.append(" FROM ");
			build(sb, fromPart);
		}
		if (!fromUuidList) {
			if (joinPart.hasResult()) {
				sb.append(" INNER JOIN ");
				build(sb, joinPart.getResult());
			}
			if (leftJoinPart.hasResult()) {
				sb.append(" LEFT JOIN ");
				build(sb, leftJoinPart.getResult());
			}
		}
		if (joinFetchPart.hasResult()) {
			sb.append(" JOIN FETCH ");
			build(sb, joinFetchPart.getResult());
		}
		if (leftJoinFetchPart.hasResult()) {
			sb.append(" LEFT JOIN FETCH ");
			build(sb, leftJoinFetchPart.getResult());
		}

		if (!fromUuidList) {
			StringBuilder queryParts = new StringBuilder();
			List<QueryPartString> parts = queryComposer.resolveInnerFrom();
			boolean first = true;
			boolean hasWherePart = false;
			for (QueryPartString queryPart : parts) {
				queryParts.append(" ");	
				if (first && queryPart.getFirstDelimiter() != null) {
					first = false;
					queryParts.append(queryPart.getFirstDelimiter());					
				} else if (queryPart.getDelimiter() != null) {
					queryParts.append(queryPart.getDelimiter());	
				} 
				if (!hasWherePart && queryPart.isWherePart()) {
					hasWherePart = true;
				}
				queryParts.append(queryPart.getPart());
			}
			build(sb, queryParts);
			
			String andPart = null;
			String orPart = null;
			if (hasWherePart || wherePart.hasResult() || hasCustomWherePart()) {
				if (!hasCustomWherePart() && !hasWherePart) {
					sb.append(" WHERE ");
				} else if (wherePart.hasResult()) {
					sb.append(" AND ");		
				}
				build(sb, wherePart.getResult());
				andPart = queryComposer.getAndSearchPart(true);
				orPart = queryComposer.getOrSearchPart(true);
			} else {
				andPart = queryComposer.getAndSearchPart(hasCustomWherePart());
				orPart = queryComposer.getOrSearchPart(hasCustomWherePart());
			}
			build(sb, new StringBuilder(andPart));
			build(sb, new StringBuilder(orPart));
			buildGroupByPart(sb, true);
			if (havingPart.hasResult()) {
				sb.append(" HAVING ");
				build(sb, havingPart.getResult());
			}
			if (!isCount) {
				buildOrderByPart(sb, true);
			}
		}
	}

	public String buildPart() {
		StringBuilder sb = new StringBuilder();
		build(sb, queryPart.getResult());
		return sb.toString();
	}

	public <T extends IdObject, DTO> QueryResult<DTO> getDtoResult(QueryManager queryManager, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return queryManager.getDtoResult(this, entityDtoConverter, type);
	}

	public <T extends IdObject> QueryResult<T> getEntityResult(QueryManager queryManager, Class<T> type) {
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

	public <T extends IdObject, DTO> List<DTO> getDtoList(QueryManager queryManager, Function<T, DTO> entityDtoConverter, Class<T> type) {
		return queryManager.getDtoList(this, entityDtoConverter, type);
	}

	public <T extends IdObject> List<T> getEntityList(QueryManager queryManager, Class<T> type) {
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

	public Collection<QueryParam> getParams() {
		return params.values();
	}

	protected boolean isNativeQuery() {
		return queryType == QueryType.Native;
	}

	public boolean isDistinct() {
		return queryComposer.isDistinct();
	}

	public List<SelectField> getSelectFields() {
		initComposer();
		return queryComposer.getSelectFields();
	}
	
	private void buildSelectPart(StringBuilder sb, boolean isUuids, boolean isInner) {
		String result = null;
		if (isUuids) {
			if (applyOuterQuery() && !isInner) {
				result = Constants.OUTER_QUERY_PREFIX + "." + getUuidField();
			} else if (applyOuterQuery() && isInner) {
				result = queryComposer.getInnerFields(false);
			} else {
				result = queryComposer.getDistinctUuidFields(false);
			}
		} else {
			if (applyOuterQuery() && !isInner) {
				result = queryComposer.getOuterSelectFields();
			} else {
				result = queryComposer.getInnerFields(false);
			}
		}
		
		if (result != null) {
			build(sb, new StringBuilder("SELECT " + result));
		}
	}
	
	private void buildOrderByPart(StringBuilder sb, boolean isInner) {
		String result = null;
		if (!applyOuterQuery() || !isInner) {
			result = queryComposer.getOrderByPart();
		}
		if (result != null) {
			build(sb, new StringBuilder(" ORDER BY " + result));
		}
	}
	
	private void buildGroupByPart(StringBuilder sb, boolean isInner) {		
		String result = null;
		if (applyOuterQuery() && isInner) {
			result = queryComposer.getInnerFields(true);
		} else if (isDistinct() && isInner) {
			result = queryComposer.getDistinctGroupByFields(true);
		} else {
			result = queryComposer.getGroupByFields(true);			
		}
		if (StringUtils.isNotBlank(result)) {
			build(sb, new StringBuilder(" GROUP BY " + result));
		}
	}	
	
	public QueryComposer setDistinct() {
		queryComposer.setDistinct();
		return this;
	}
	
	void _setDistinct(boolean distinct) {
		queryComposer._setDistinct(distinct);
	}
	
	public QueryComposer setDistinct(boolean distinct) {
		queryComposer.setDistinct(distinct);
		return this;
	}

	public QueryComposer add(String queryPart) {
		queryComposer.add(queryPart);
		return this;
	}
	
	public QueryComposer addWhere(String queryPart) {
		queryComposer.addWhere(queryPart);
		return this;
	}
	
	public QueryComposer addOrWhere(String queryPart) {
		queryComposer.addOrWhere(queryPart);
		return this;
	}
	
	public QueryComposer addDistinctWhere(String queryPart) {
		queryComposer.addDistinctWhere(queryPart);
		return this;
	}
	
	public QueryComposer addDistinctOrWhere(String queryPart) {
		queryComposer.addDistinctOrWhere(queryPart);
		return this;
	}
	
	public QueryComposer addConditionalWhere(String queryPart, String... aliases) {
		queryComposer.addConditionalWhere(queryPart, aliases);
		return this;
	}

	public QueryComposer addDistinctConditionalWhere(String queryPart, String... aliases) {
		queryComposer.addDistinctConditionalWhere(queryPart, aliases);
		return this;
	}	
	
	public QueryComposer addConditionalOrWhere(String queryPart, String... aliases) {
		queryComposer.addConditionalOrWhere(queryPart, aliases);
		return this;
	}

	public QueryComposer addDistinctConditionalOrWhere(String queryPart, String... aliases) {
		queryComposer.addDistinctConditionalOrWhere(queryPart, aliases);
		return this;
	}

	public QueryComposer addDistinct(String queryPart) {
		queryComposer.addDistinct(queryPart);
		return this;
	}

	public QueryComposer add(String queryPart, boolean distinct) {
		queryComposer.add(queryPart, distinct);
		return this;
	}

	public QueryComposer addConditional(String queryPart, String... aliases) {
		queryComposer.addConditional(queryPart, aliases);
		return this;
	}

	public QueryComposer addDistinctConditional(String queryPart, String... aliases) {
		queryComposer.addDistinctConditional(queryPart, aliases);
		return this;
	}

	public QueryComposer addConditional(String queryPart, boolean distinct, String... aliases) {
		queryComposer.addConditional(queryPart, distinct, aliases);
		return this;
	}
	
	public QueryComposer addSearch(String... paths) {
		queryComposer.addSearch(paths);
		return this;
	}
	
	public QueryComposer addSearch(String paramName, String paramValue, SearchType searchType, String... paths) {
		queryComposer.addSearch(paramName, paramValue, searchType, paths);
		return this;
	}
	
	public QueryComposer addTranslatableSearch(String... paths) {
		queryComposer.addTranslatableSearch(paths);
		return this;
	}
	
	public QueryComposer addTranslatableSearch(String paramName, String paramValue, SearchType searchType, String... paths) {
		queryComposer.addTranslatableSearch(paramName, paramValue, searchType, paths);
		return this;
	}

	public QueryComposer addAliasResolver(String alias, String path) {
		queryComposer.addAliasResolver(alias, path);
		return this;
	}
	
	public QueryComposer addDefaultOrder(OrderType orderType, FieldType fieldType, String... path) {
		queryComposer.addDefaultOrder(orderType, fieldType, path);
		return this;
	}

	public QueryComposer addDefaultOrder(OrderType orderType, String... path) {
		queryComposer.addDefaultOrder(orderType, path);
		return this;
	}

	public QueryComposer addDefaultOrder(String alias, OrderType orderType, FieldType fieldType, String... path) {
		queryComposer.addDefaultOrder(orderType, fieldType, path);
		return this;
	}

	public QueryComposer addDefaultOrder(String alias, OrderType orderType, String... path) {
		queryComposer.addDefaultOrder(alias, orderType, path);
		return this;
	}
	
	public QueryComposer setDefaultOrder(OrderType orderType, FieldType fieldType, String... path) {
		queryComposer.setDefaultOrder(orderType, fieldType, path);
		return this;
	}

	public QueryComposer setDefaultOrder(OrderType orderType, String... path) {
		queryComposer.setDefaultOrder(orderType, path);
		return this;
	}

	public QueryComposer setDefaultOrder(String alias, OrderType orderType, FieldType fieldType, String... path) {
		queryComposer.setDefaultOrder(orderType, fieldType, path);
		return this;
	}

	public QueryComposer setDefaultOrder(String alias, OrderType orderType, String... path) {
		queryComposer.setDefaultOrder(alias, orderType, path);
		return this;
	}
	
	public QueryComposer bindOrder(String name, FieldType fieldType) {
		queryComposer.bindOrder(name, fieldType);
		return this;
	}

	public QueryComposer bindOrder(String name) {
		return bindOrder(name, name);
	}

	public QueryComposer bindOrder(String name, FieldType fieldType, String... path) {
		queryComposer.bindOrder(name, fieldType, path);
		return this;
	}

	public QueryComposer bindOrder(String name, String... path) {
		queryComposer.bindOrder(name, path);
		return this;
	}
	
	public QueryComposer select(String selectString) {
		queryComposer.select(selectString);
		return this;
	}
	
	public QueryComposer addSelect(String alias) {
		return addSelect(alias, alias);
	}

	public QueryComposer addSelect(String alias, String path) {
		queryComposer.addSelect(alias, path);
		return this;
	}
	
	public QueryComposer setUuidField(String uuidField) {
		this.uuidField = uuidField;
		return this;
	}
	
	public QueryComposer setUuidAlias(String uuidAlias) {
		this.uuidAlias = uuidAlias;
		return this;
	}
	
	public QueryComposer skipUuid() {
		this.skipUuid = true;
		return this;
	}
	
	public String getCountQueryStr() {
		return buildCountPart();
	}

	public String getQueryStr() {
		return build();
	}		
	
	private void build(StringBuilder result, StringBuilder part) {
		if (part != null && part.length() > 0) {
			result.append(part);
		}
	}

	public QueryRequestDTO getQueryRequest() {
		return queryRequest;
	}
	
	boolean shouldBeAdded(Collection<String> aliases) {
		return queryComposer.shouldBeAdded(aliases);
	}
	
	boolean shouldBeAdded(boolean isDistinct, Collection<String> aliases) {
		return queryComposer.shouldBeAdded(isDistinct, aliases);
	}
	
	void setCustomWherePart(boolean hasCustomWherePart) {
		this.hasCustomWherePart = hasCustomWherePart;
	}
	
	boolean hasCustomWherePart() {
		return this.hasCustomWherePart;
	}
	
	boolean applyOuterQuery() {
		return queryComposer.isApplyOuterQuery(isNativeQuery())
				&& QueryRequestUtil.isPaged(queryRequest);
	}
	
	boolean applyDistinctCount() {
		return queryComposer.isApplyOuterQuery(isNativeQuery());
	}
	
	String getUuidField() {
		return uuidField;
	}
	
	String getUuidAlias() {
		return uuidAlias;
	}
	
	boolean isSkipUuid() {
		return skipUuid;
	}
	
	boolean isShowUuidAlias() {
		return !uuidField.equalsIgnoreCase(uuidAlias);
	}
	
	public void  setSearchParameter(String name, SearchTerm searchTerm, SearchType searchType) {
		Object value = searchTerm.getValue();
		if (searchTerm.isNotStringValueEmpty()) {
			value = SearchType.getExpression(searchTerm, searchType);
		}
		setParameter(name, value);
	}
	
	private void setParameter(String name, Object value) {
		params.put(name, QueryParam.create(name, value));
	}
	
}
