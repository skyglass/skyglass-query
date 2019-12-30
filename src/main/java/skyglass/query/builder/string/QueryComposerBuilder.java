package skyglass.query.builder.string;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import skyglass.query.QueryFunctions;
import skyglass.query.QueryOrderUtil;
import skyglass.query.QueryRequestUtil;
import skyglass.query.QuerySearchUtil;
import skyglass.query.builder.FieldType;
import skyglass.query.builder.OrderBuilder;
import skyglass.query.builder.OrderField;
import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.SearchBuilder;
import skyglass.query.builder.config.Constants;

public class QueryComposerBuilder {

	private static final Pattern ALIAS_REGEX_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

	private final String OUTER_QUERY_PREFIX = "tab";

	private QueryRequestDTO queryRequest;

	private boolean applyOuterQuery;

	private Map<String, Set<String>> queryMap;

	private Collection<QueryPart> queryParts;

	private List<FieldItem> fieldItems;

	private Map<String, FieldItem> fieldMap;

	private Map<String, FieldItem> fieldPathMap;

	private Map<String, String> aliasResolverMap;

	private Map<String, FieldItem> selectFieldMap;

	private Map<String, FieldItem> orderFieldMap;

	private List<String> searchPartSuppliers;

	private List<Runnable> searchPartInitRunners = new ArrayList<>();

	private List<Runnable> orderBuilderRunners = new ArrayList<>();

	private List<Runnable> defaultOrderBuilderRunners = new ArrayList<>();

	private List<Runnable> selectBuilderRunners = new ArrayList<>();

	private List<Runnable> queryPartBuilderRunners = new ArrayList<>();
	
	private List<Runnable> initRunners = new ArrayList<>();
	
	private OrderBuilder orderBuilder;

	private String rootAlias;

	private String queryStr;

	private String countQueryStr;
	
	private QueryComposer queryStringBuilder;
	
	private String customSelectPart;
	
	private String customGroupByPart;
	
	private String customOrderByPart;

	public QueryComposerBuilder(QueryRequestDTO queryRequest, String rootAlias) {
		this.queryRequest = queryRequest;
		this.orderBuilder = new OrderBuilder(queryRequest);
		this.rootAlias = rootAlias;
	}
	
	public QueryComposerBuilder(QueryComposer queryStringBuilder, QueryRequestDTO queryRequest, String rootAlias) {
		this(queryRequest, rootAlias);
		this.queryStringBuilder = queryStringBuilder;
	}
	
	public boolean isDistinct() {
		return this.applyOuterQuery;
	}

	public void setDistinct() {
		initRunners.add(() -> {
			this.applyOuterQuery = true;
		});
	}
	
	public void setDistinct(boolean distinct) {
		initRunners.add(() -> {
			this.applyOuterQuery = distinct;
		});
	}

	public void add(String queryPart) {
		add(queryPart, false);
	}

	public void addDistinct(String queryPart) {
		add(queryPart, true);
	}

	public void add(String queryPart, boolean distinct) {
		queryPartBuilderRunners.add(() -> {
			doAdd(queryPart, distinct);
		});
	}

	public void addConditional(String queryPart, String... aliases) {
		queryPartBuilderRunners.add(() -> {
			doAddConditional(queryPart, aliases);
		});
	}

	public void addDistinctConditional(String queryPart, String... aliases) {
		queryPartBuilderRunners.add(() -> {
			doAddDistinctConditional(queryPart, aliases);
		});
	}

	public void addConditional(String queryPart, boolean distinct, String... aliases) {
		queryPartBuilderRunners.add(() -> {
			doAddConditional(queryPart, distinct, aliases);
		});
	}
	
	public void addSearch(String... paths) {
		addSearchRunner(paths);
	}

	public void addAliasResolver(String alias, String path) {
		initRunners.add(() -> {
			aliasResolverMap.put(alias, resolveAliasPath(path));
		});
	}
	
	public void setDefaultOrder(OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, path);
	}

	public void setDefaultOrder(OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, path);
	}

	public void setDefaultOrder(String alias, OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, alias, path);
	}

	public void setDefaultOrder(String alias, OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, null, alias, path);
	}

	public void bindOrder(String name, FieldType fieldType, String... path) {
		addBindOrderRunner(name, fieldType, path);
	}

	public void bindOrder(String name, String... path) {
		addBindOrderRunner(name, null, path);
	}
	
	public void addSelect(String selectString) {
		selectBuilderRunners.add(() -> {
			doAddSelect(selectString);
		});
	}

	public void addSelect(String alias, String path) {
		selectBuilderRunners.add(() -> {
			doAddSelect(alias, path);
		});
	}
	
	public String getCountQueryStr() {
		if (countQueryStr == null) {
			getQueryStr();
		}
		return countQueryStr;
	}

	public String getQueryStr() {
		resetAndInit();
		return queryStr;
	}
	
	void resetAndInit() {
		reset();
		init();
	}
	
	public List<OrderField> getOrderFields() {
		return orderBuilder.getOrderFields();
	}
	
	public void addOrder(OrderType orderType, String... orderFields) {
		orderBuilder.addOrder(orderType, orderFields);
	}

	public void setOrder(OrderType orderType, String... orderFields) {
		orderBuilder.setOrder(orderType, orderFields);
	}

	public void setDefaultOrders(OrderType orderType, String... orderFields) {
		orderBuilder.setDefaultOrders(orderType, orderFields);
	}

	private void reset() {
		applyOuterQuery = false;
		aliasResolverMap = new HashMap<>();
		queryMap = new LinkedHashMap<>();
		queryParts = new ArrayList<>();
		fieldItems = new ArrayList<>();
		fieldMap = new LinkedHashMap<>();
		fieldPathMap = new LinkedHashMap<>();
		selectFieldMap = new LinkedHashMap<>();
		orderFieldMap = new LinkedHashMap<>();
		queryStr = null;
		countQueryStr = null;
		searchPartSuppliers = new ArrayList<>();
		
	}

	private void doAdd(String queryPart, boolean distinct) {
		queryParts.add(new QueryPart(queryPart, distinct));
		queryMap.computeIfAbsent(queryPart, a -> new HashSet<>()).add(Constants.UUID);
	}

	private void doAddConditional(String queryPart, String... aliases) {
		doAddConditional(queryPart, false, aliases);
	}

	private void doAddDistinctConditional(String queryPart, String... aliases) {
		doAddConditional(queryPart, true, aliases);
	}

	private void doAddConditional(String queryPart, boolean distinct, String... aliases) {
		queryParts.add(new QueryPart(queryPart, distinct));
		for (String alias : aliases) {
			queryMap.computeIfAbsent(queryPart, a -> new HashSet<>()).add(alias);
		}
	}

	private boolean shouldBeAdded(QueryPart queryPart) {
		Collection<String> aliases = queryMap.get(queryPart.getQueryPart());
		return shouldBeAdded(queryPart.isDistinct(), aliases);
	}
	
	public boolean shouldBeAdded(Collection<String> aliases) {
		if (CollectionUtils.isEmpty(aliases)) {
			return false;
		}
		boolean result = aliases.contains(Constants.UUID) || aliases.contains(Constants.UUID.toLowerCase());
		if (!result) {
			for (String alias : fieldMap.keySet()) {
				result = aliases.contains(alias);
				if (result) {
					break;
				}
				String aliasResolver = aliasResolverMap.get(alias);
				if (StringUtils.isNotBlank(aliasResolver)) {
					result = aliases.contains(aliasResolver);
				}
				if (result) {
					break;
				}
			}
		}
		return result;
	}
	
	public boolean shouldBeAdded(boolean isDistinct, Collection<String> aliases) {
		boolean result = shouldBeAdded(aliases);
		if (result && isDistinct) {
			this.applyOuterQuery = true;
		}
		return result;
	}

	private String resolveAliasPath(String path) {
		StringBuilder output = new StringBuilder();
		Matcher matcher = ALIAS_REGEX_PATTERN.matcher(path);
		int lastStart = 0;
		while (matcher.find()) {
			String subString = path.substring(lastStart, matcher.start());
			String varName = matcher.group(1);
			String replacement = aliasResolverMap.get(varName);
			if (StringUtils.isBlank(replacement)) {
				replacement = "";
			}
			output.append(subString).append(replacement);
			lastStart = matcher.end();
		}
		output.append(path.substring(lastStart));
		return output.toString();
	}

	private void addDefaultOrderRunner(OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, null, null, path);
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, null, path);
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, String alias, String... paths) {
		if (alias == null) {
			alias = paths.length > 0 ? adaptAlias(alias, paths[0]).getKey() : null;
		}
		final String finalAlias = alias;
		final boolean useAlias = alias != null;
		defaultOrderBuilderRunners.add(() -> {
			if (orderBuilder.shouldSetDefaultOrder()) {
				String[] resolvedPaths = new String[paths.length];
				for (int i = 0; i < paths.length; i++) {
					resolvedPaths[i] = resolvePath(finalAlias, paths[i]);
				}
				if (fieldType == null) {
					orderBuilder.setDefaultOrder(orderType, resolvedPaths);
				} else {
					orderBuilder.setDefaultOrder(orderType, fieldType, resolvedPaths);
				}
				if (useAlias) {
					for (int i = 0; i < paths.length; i++) {
						addOrderFieldResolver(finalAlias, resolvedPaths[i], paths[i]);
					}
				} else {
					for (int i = 0; i < paths.length; i++) {
						addOrderFieldResolver(resolvedPaths[i], paths[i]);
					}
				}
			}
		});
	}

	private void addBindOrderRunner(String name, FieldType fieldType, String... paths) {
		orderBuilderRunners.add(() -> {
			if (orderBuilder.shouldBindOrder(name)) {
				String[] resolvedPaths = new String[paths.length];
				for (int i = 0; i < paths.length; i++) {
					resolvedPaths[i] = resolvePath(name, paths[i]);
				}
				if (fieldType == null) {
					orderBuilder.bindOrder(name, resolvedPaths);
				} else {
					orderBuilder.bindOrder(name, fieldType, resolvedPaths);
				}
				for (int i = 0; i < paths.length; i++) {
					addOrderFieldResolver(name, resolvedPaths[i], paths[i]);
				}
			}
		});
	}

	private void addSearchRunner(String... paths) {
		searchPartInitRunners.add(() -> {
			for (int j = 0; j < paths.length; j++) {
				analyzeSearchPath(paths[j]);
			}
			boolean applyOuterQueryOriginal = applyOuterQuery;
			applyOuterQuery = false;
			for (String path : paths) {
				addSearchFieldResolver(path, path);
			}
			if (!shouldApplySearch()) {
				applyOuterQuery = false;
			} else if (applyOuterQuery) {
				//applyOuterSearch = true;
			}
			applyOuterQuery = applyOuterQuery || applyOuterQueryOriginal;
			
			List<String> searchTerms = CollectionUtils.isEmpty(queryRequest.getSearchTerms())
					? (StringUtils.isBlank(queryRequest.getSearchTerm())
							? Collections.emptyList()
							: Collections.singletonList(queryRequest.getSearchTerm()))
					: queryRequest.getSearchTerms();
			for (int i = 0; i < searchTerms.size(); i++) {
				String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + Integer.toString(i);
				List<String> resolvedPathList = new ArrayList<>();
				for (int j = 0; j < paths.length; j++) {
					String resolvedSearchPath = resolveSearchPath(paths[j]);
					resolvedPathList.add(resolvedSearchPath);
				}
				SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, resolvedPathList.toArray(new String[0]));
				searchPartSuppliers.add(QuerySearchUtil.applySearch(true, searchBuilder));
			}
		});
	}

	private void addOrderFieldResolver(String path, String innerPath) {
		addOrderFieldResolver(null, path, innerPath);
	}

	private void addOrderFieldResolver(String alias, String path, String innerPath) {
		addFieldResolver(alias, path, innerPath, true, false);
	}

	private void addSelectFieldResolver(String alias, String path, String innerPath) {
		addFieldResolver(alias, path, innerPath, false, true);
	}

	private void addSearchFieldResolver(String path, String innerPath) {
		addFieldResolver(null, path, innerPath, false, false);
	}

	private void addFieldResolver(String alias, String path, String innerPath, boolean orderField, boolean selectField) {
		Pair<String, String> aliasPair = adaptAlias(alias, path);
		addFieldItem(aliasPair.getKey(), aliasPair.getValue(), path, innerPath, orderField, selectField);
	}

	private Pair<String, String> adaptAlias(String alias, String path) {
		String[] pathParts = path.split("\\.");
		String innerAlias = pathParts[pathParts.length - 1];
		if (alias == null || alias.equals(path)) {
			alias = innerAlias;
		}
		if (pathParts.length == 1 && aliasResolverMap.get(alias) == null) {
			throw new IllegalArgumentException("Unknown alias: " + alias);
		}
		return Pair.of(alias, innerAlias);
	}

	private void addFieldItem(String alias, String innerAlias, String path, String innerPath, boolean orderField, boolean selectField) {
		innerPath = resolveInnerPath(innerAlias, innerPath);
		FieldItem fieldItem = fieldMap.get(alias);
		if (fieldItem == null) {
			fieldItem = new FieldItem(alias, innerAlias, path, innerPath);
			fieldItems.add(fieldItem);
			fieldMap.put(alias, fieldItem);
			fieldPathMap.put(path, fieldItem);
		}
		if (orderField) {
			orderFieldMap.put(alias, fieldItem);
		}
		if (selectField) {
			selectFieldMap.put(alias, fieldItem);
		}
	}

	private void doAddSelect(String selectString) {
		if (StringUtils.isEmpty(selectString)) {
			return;
		}

		String[] parts = selectString.replaceAll("(?i)select", "").replaceAll("(?i)distinct", "").split(", ");
		for (String part : parts) {
			String[] subParts = part.split("(?i) as ");
			String path = subParts[0].trim();
			String alias = null;
			if (subParts.length > 1) {
				alias = subParts[1].trim();
			} else if (!path.contains(".")) {
				String tryPath = aliasResolverMap.get(path);
				if (tryPath != null) {
					addSelectFieldResolver(path, tryPath, tryPath);
					continue;
				}
			}
			addSelectFieldResolver(alias, path, path);
		}
	}

	private void doAddSelect(String alias, String path) {
		addSelectFieldResolver(alias, path, path);
	}

	private void init() {
		initPart();
		initSelectPart();
		initConditionalPart();
		initSearchPart();
		initOrderByPart();

		String fromBasicQueryStr = "";
		List<String> parts = resolveInnerFrom();
		for (String queryPart : parts) {
			fromBasicQueryStr += queryPart;
		}

		String innerSelectFields = getInnerFields(false);
		String innerGroupByFields = getInnerFields(true);
		String innerSelect = "SELECT " + innerSelectFields;
		if (applyOuterQuery) {
			//This query part returns select fields + column names, for which sorting is supported. Select and Sorting columns should exist in entity table or in tables, which have one to one correspondence
			//Therefore GROUP BY by all these columns guarantees uniquness of entity's tab.UUID and we shouldn't have duplicates when grouping by select and sorting columns (unless we return tabular native query result)
			String outerSelectFields = getOuterSelectFields();
			String outerComposerSelect = "SELECT " + outerSelectFields;

			//This query part selects column names, which will be used by outer query. 

			String fromQueryStr = null;


			fromQueryStr = fromBasicQueryStr + " " + getSearchPart(fromBasicQueryStr) + " GROUP BY " + innerGroupByFields;
			countQueryStr = "SELECT DISTINCT COUNT(1) OVER () " + fromQueryStr;
			fromQueryStr = innerSelect + " " + fromQueryStr;

			queryStr = outerComposerSelect + " FROM ( " + fromQueryStr + " ) tab" + getOuterOrderByPart() + getPagedPart();
		} else {
			String searchPart = getSearchPart(fromBasicQueryStr);
			queryStr = innerSelect + " " + fromBasicQueryStr + " " + searchPart + getBasicOrderByPart(searchPart) + getPagedPart();
			countQueryStr = "SELECT COUNT(1) " + fromBasicQueryStr + " " + getSearchPart(fromBasicQueryStr);
		}
	}


	private String getOuterSelectFields() {
		String result = OUTER_QUERY_PREFIX + "." + Constants.UUID;
		for (FieldItem fieldItem : selectFieldMap.values()) {
			if (!Constants.UUID.equalsIgnoreCase(fieldItem.getAlias())) {
				result += ", " + OUTER_QUERY_PREFIX + "." + fieldItem.getAlias();
			}
		}
		return result;
	}

	private String getInnerFields(boolean groupBy) {
		String result = rootAlias + "." + Constants.UUID;

		for (FieldItem fieldItem : selectFieldMap.values()) {
			if (!Constants.UUID.equalsIgnoreCase(fieldItem.getAlias())) {
				result += ", " + fieldItem.getInnerPath(rootAlias, groupBy);
			}
		}
		
		if (applyOuterQuery) {
			for (FieldItem fieldItem : orderFieldMap.values()) {
				if (fieldItem != null && !Constants.UUID.equalsIgnoreCase(fieldItem.getAlias()) && selectFieldMap.get(fieldItem.getAlias()) == null) {
					result += ", " + fieldItem.getInnerPath(rootAlias, groupBy);
				}
			}
		}

		return result;
	}
	
	List<SelectField> getSelectFields() {		
		List<SelectField> result = new ArrayList<>();
		SelectField selectField = new SelectField(Constants.UUID, rootAlias + "." + Constants.UUID);
		result.add(selectField);
		for (FieldItem fieldItem : selectFieldMap.values()) {
			selectField = new SelectField(fieldItem.getAlias(), fieldItem.getInnerPath(rootAlias));
			result.add(selectField);
		}
		return result;
	}

	private List<String> resolveInnerFrom() {
		return queryParts.stream().filter(s -> shouldBeAdded(s)).map(s -> s.getQueryPart()).collect(Collectors.toList());
	}

	private String getSearchPart(String fromBasicQueryStr) {
		String result = null;
		for (String searchPartSupplier : searchPartSuppliers) {
			result = QueryFunctions.and(result, searchPartSupplier);
		}
		return result == null ? "" : ((StringUtils.isNotBlank(fromBasicQueryStr) && fromBasicQueryStr.contains(" WHERE ") ? "AND " : "WHERE ") + result);
	}

	private void initPart() {
		for (Runnable initRunner : initRunners) {
			initRunner.run();
		}
	}

	private void initSelectPart() {
		for (Runnable selectBuilderRunner : selectBuilderRunners) {
			selectBuilderRunner.run();
		}
	}

	private void initConditionalPart() {
		for (Runnable conditionalBuilderRunner : queryPartBuilderRunners) {
			conditionalBuilderRunner.run();
		}
	}

	private void initSearchPart() {
		for (Runnable searchPartInitRunner : searchPartInitRunners) {
			searchPartInitRunner.run();
		}
	}

	private void initOrderByPart() {
		for (Runnable orderBuilderRunner : orderBuilderRunners) {
			orderBuilderRunner.run();
		}
		for (Runnable orderBuilderRunner : defaultOrderBuilderRunners) {
			orderBuilderRunner.run();
		}
	}

	private String getBasicOrderByPart(String searchPart) {
		return (searchPart.length() == 0 || searchPart.substring(searchPart.length() - 1).equals(" ") ? "" : " ") 
				+ "ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}
	
	private String getOuterOrderByPart() {
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getPagedPart() {
		return QueryRequestUtil.isPaged(queryRequest) ? " LIMIT ?limit OFFSET ?offset" : "";
	}

	private void analyzeSearchPath(String path) {
		String[] pathParts = path.split("\\.");
		if (pathParts.length == 1 && aliasResolverMap.get(path) == null) {
			throw new IllegalArgumentException("Unknown alias: " + path);
		}
	}

	private String resolveSearchPath(String path) {
		String result = null;
		FieldItem fieldItem = fieldPathMap.get(path);
		if (fieldItem == null) {
			fieldItem = fieldMap.get(path);
		}
		String[] pathParts = path.split("\\.");
		if (pathParts.length == 1 && aliasResolverMap.get(path) == null) {
			throw new IllegalArgumentException("Unknown alias: " + path);
		}
		if (pathParts.length == 1 && aliasResolverMap.get(path) != null) {
			result = aliasResolverMap.get(path);
		} else {
			result = path;
		}
		return result;

	}

	private String resolvePath(String alias, String path) {
		String[] pathParts = path.split("\\.");
		if (alias == null || alias.equals(path)) {
			String innerAlias = pathParts[pathParts.length - 1];
			alias = innerAlias;
			if (pathParts.length == 1 && aliasResolverMap.get(alias) == null) {
				throw new IllegalArgumentException("Unknown alias: " + alias);
			}
		}
		if (pathParts.length == 1 && aliasResolverMap.get(alias) != null) {
			path = aliasResolverMap.get(path);
		}
		return applyOuterQuery ? (OUTER_QUERY_PREFIX + "." + alias) : path;
	}
	
	private String resolveInnerPath(String alias, String path) {
		String[] pathParts = path.split("\\.");
		if (pathParts.length == 1 && aliasResolverMap.get(alias) != null) {
			path = aliasResolverMap.get(path);
		}
		return path;
	}

	private boolean shouldApplySearch() {
		return shouldApplySearch(queryRequest);
	}

	public static boolean shouldApplySearch(QueryRequestDTO queryRequest) {
		if (StringUtils.isNotBlank(queryRequest.getSearchTerm())) {
			return true;
		} else {
			for (int i = 0; i < queryRequest.getSearchTerms().size(); i++) {
				if (StringUtils.isNotBlank(queryRequest.getSearchTerms().get(i))) {
					return true;
				}
			}
		}
		return false;
	}
	
	void select(String customSelectPart) {
		this.customSelectPart = customSelectPart;
	}
	
	String buildSelectResult() {
		String selectPart = getInnerFields(false);
		if (customSelectPart == null && StringUtils.isBlank(selectPart)) {
			customSelectPart = "*";
		}
		return "SELECT " + (customSelectPart == null ? selectPart : customSelectPart);
	}
	
	void orderBy(String customOrderByPart) {
		this.customOrderByPart = customOrderByPart;
	}
	
	String buildOrderByResult() {
		if (customOrderByPart == null &&  CollectionUtils.isEmpty(orderBuilder.getOrderFields())) {
			return "";
		}
		return "ORDER BY " + (customOrderByPart == null ? QueryOrderUtil.applyOrder(orderBuilder.getOrderFields()) : customOrderByPart);
	}
	
	void groupBy(String customGroupByPart) {
		this.customGroupByPart = customGroupByPart;
	}
	
	String buildGroupByResult() {
		String groupByPart = isDistinct() ? getInnerFields(true) : null;
		if (customGroupByPart == null && StringUtils.isBlank(groupByPart)) {
			return "";
		}
		return "GROUP BY " + (customGroupByPart == null ? groupByPart : customGroupByPart);
	}

}