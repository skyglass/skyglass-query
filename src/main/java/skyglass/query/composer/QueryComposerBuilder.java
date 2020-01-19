package skyglass.query.composer;

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

import skyglass.query.composer.config.Constants;
import skyglass.query.composer.search.Combination;
import skyglass.query.composer.search.SearchPath;
import skyglass.query.composer.search.SearchProcessor;
import skyglass.query.composer.search.SearchTerm;
import skyglass.query.composer.search.SearchType;
import skyglass.query.composer.util.QueryFunctions;
import skyglass.query.composer.util.QueryOrderUtil;
import skyglass.query.composer.util.QueryRequestUtil;
import skyglass.query.composer.util.QuerySearchUtil;

public class QueryComposerBuilder {

	private static final Pattern ALIAS_REGEX_PATTERN = Pattern.compile("\\$\\{(.*?)\\}");

	private QueryRequestDTO queryRequest;

	private QueryComposer root;

	private boolean applyOuterQuery;

	private Map<QueryPartString, Set<String>> queryMap;

	private Collection<QueryPart> queryParts;

	private List<FieldItem> fieldItems;

	private Map<String, FieldItem> fieldMap = new HashMap<>();

	private Map<String, FieldItem> fieldPathMap;

	private Map<String, String> aliasResolverMap = new HashMap<>();

	private Map<String, FieldItem> selectFieldMap;

	private Map<String, FieldItem> groupByFieldMap;

	private Map<String, FieldItem> orderFieldMap;

	private List<String> searchPartAndSuppliers;

	private List<String> searchPartOrSuppliers;

	private List<Runnable> searchPartInitRunners = new ArrayList<>();

	private List<Runnable> orderBuilderRunners = new ArrayList<>();

	private List<Runnable> defaultOrderBuilderRunners = new ArrayList<>();

	private List<Runnable> selectBuilderRunners = new ArrayList<>();

	private List<Runnable> groupByBuilderRunners = new ArrayList<>();

	private List<Runnable> queryPartBuilderRunners = new ArrayList<>();

	private List<Runnable> initRunners = new ArrayList<>();

	private OrderBuilder orderBuilder;

	private String rootAlias;

	private String customOrderByPart;

	private boolean forceDistinct;

	QueryComposerBuilder(QueryComposer root, QueryRequestDTO queryRequest, String rootAlias) {
		this.root = root;
		this.queryRequest = queryRequest;
		this.orderBuilder = new OrderBuilder(queryRequest);
		this.rootAlias = rootAlias;
	}

	public boolean isDistinct() {
		return this.applyOuterQuery;
	}

	public void setDistinct() {
		initRunners.add(() -> {
			_forceDistinct(true);
		});
	}

	public void setDistinct(boolean distinct) {
		initRunners.add(() -> {
			_forceDistinct(distinct);
		});
	}

	void _setDistinct(boolean distinct) {
		if (!forceDistinct) {
			this.applyOuterQuery = distinct;
		}
	}

	void _forceDistinct(boolean distinct) {
		this.applyOuterQuery = distinct;
		if (distinct) {
			this.forceDistinct = true;
		}
	}

	public void add(String queryPart) {
		add(queryPart, false);
	}

	public void addDistinct(String queryPart) {
		add(queryPart, true);
	}

	public void addWhere(String queryPart) {
		addWhere(queryPart, null, false);
	}

	public void addOrWhere(String queryPart) {
		addWhere(queryPart, "OR", false);
	}

	public void addDistinctWhere(String queryPart) {
		addWhere(queryPart, null, true);
	}

	public void addDistinctOrWhere(String queryPart) {
		addWhere(queryPart, "OR", true);
	}

	public void addConditionalWhere(String queryPart, String... aliases) {
		addConditionalWhere(queryPart, null, aliases);
	}

	public void addDistinctConditionalWhere(String queryPart, String... aliases) {
		addDistinctConditionalWhere(queryPart, null, aliases);
	}

	public void addConditionalOrWhere(String queryPart, String... aliases) {
		addConditionalWhere(queryPart, "OR", aliases);
	}

	public void addDistinctConditionalOrWhere(String queryPart, String... aliases) {
		addDistinctConditionalWhere(queryPart, "OR", aliases);
	}

	private void addWhere(String queryPart, String delimiter, boolean distinct) {
		queryPartBuilderRunners.add(() -> {
			doAddWhere(queryPart, delimiter, distinct);
		});
	}

	private void addConditionalWhere(String queryPart, String delimiter, String... aliases) {
		queryPartBuilderRunners.add(() -> {
			doAddWhereConditional(queryPart, delimiter, false, aliases);
		});
	}

	private void addDistinctConditionalWhere(String queryPart, String delimiter, String... aliases) {
		queryPartBuilderRunners.add(() -> {
			doAddWhereConditional(queryPart, delimiter, true, aliases);
		});
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

	public void addSearch(String paramName, String paramValue, SearchType searchType, String... paths) {
		addSearch(paramName, paramValue, searchType, false, paths);
	}

	public void addSearch(String... paths) {
		addSearch(SearchBuilder.SEARCH_TERM_PARAM_NAME, null, SearchType.IgnoreCase, false, paths);
	}

	public void addTranslatableSearch(String paramName, String paramValue, SearchType searchType, String... paths) {
		addSearch(paramName, paramValue, searchType, true, paths);
	}

	public void addTranslatableSearch(String... paths) {
		addSearch(SearchBuilder.SEARCH_TERM_PARAM_NAME, null, SearchType.IgnoreCase, true, paths);
	}

	public void addSearch(String paramName, String paramValue, SearchType searchType, boolean translatable, String... paths) {
		addSearchRunner(paramName, paramValue, searchType, translatable, paths);
	}

	public void addAliasResolver(String alias, String path) {
		initRunners.add(() -> {
			aliasResolverMap.put(alias, resolveAliasPath(path));
		});
	}

	private void tryAliasResolver(String alias, String... paths) {
		if (paths.length != 1) {
			return;
		}
		if (alias == null) {
			return;
		}
		String path = paths[0];
		boolean test1 = (rootAlias + "." + alias).equals(path);
		boolean test2 = (alias).equals(path);
		if (!test1 && !test2) {
			addAliasResolver(alias, path);
		}
	}

	public void addDefaultOrder(OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, true, path);
	}

	public void addDefaultOrder(OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, true, path);
	}

	public void addDefaultOrder(String alias, OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, alias, true, path);
	}

	public void addDefaultOrder(String alias, OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, null, alias, true, path);
	}

	public void setDefaultOrder(OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, false, path);
	}

	public void setDefaultOrder(OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, false, path);
	}

	public void setDefaultOrder(String alias, OrderType orderType, FieldType fieldType, String... path) {
		addDefaultOrderRunner(orderType, fieldType, alias, false, path);
	}

	public void setDefaultOrder(String alias, OrderType orderType, String... path) {
		addDefaultOrderRunner(orderType, null, alias, false, path);
	}

	public void bindOrder(String name, FieldType fieldType) {
		bindOrder(name, fieldType, name);
	}

	public void bindOrder(String name, FieldType fieldType, String... path) {
		addBindOrderRunner(name, fieldType, path);
	}

	public void bindOrder(String name, String... path) {
		addBindOrderRunner(name, null, path);
	}

	public void select(String selectString) {
		selectBuilderRunners.add(() -> {
			doAddSelect(selectString);
		});
	}

	public void addSelect(String alias, String path) {
		tryAliasResolver(alias, path);
		selectBuilderRunners.add(() -> {
			doAddSelect(alias, path);
		});
	}

	void resetAndInit() {
		reset();
		init();
	}

	public List<OrderField> getOrderFields() {
		return orderBuilder.getOrderFields();
	}

	private void reset() {
		applyOuterQuery = false;
		forceDistinct = false;
		root.setCustomWherePart(false);
		queryMap = new LinkedHashMap<>();
		queryParts = new ArrayList<>();
		fieldItems = new ArrayList<>();
		fieldMap = new LinkedHashMap<>();
		fieldPathMap = new LinkedHashMap<>();
		selectFieldMap = new LinkedHashMap<>();
		groupByFieldMap = new LinkedHashMap<>();
		orderFieldMap = new LinkedHashMap<>();
		searchPartAndSuppliers = new ArrayList<>();
		searchPartOrSuppliers = new ArrayList<>();

	}

	private void doAdd(String queryPart, boolean distinct) {
		addQueryPart(queryPart, distinct, null, false);
	}

	private void doAddWhere(String queryPart, String delimiter, boolean distinct) {
		addQueryPart(queryPart, distinct, delimiter, true);
	}

	private void doAddWhereConditional(String queryPart, String delimiter, boolean distinct, String... aliases) {
		addQueryPart(queryPart, distinct, delimiter, true, aliases);
	}

	private void doAddConditional(String queryPart, String... aliases) {
		doAddConditional(queryPart, false, aliases);
	}

	private void doAddDistinctConditional(String queryPart, String... aliases) {
		doAddConditional(queryPart, true, aliases);
	}

	private void doAddConditional(String queryPart, boolean distinct, String... aliases) {
		addQueryPart(queryPart, distinct, null, false, aliases);
	}

	private void addQueryPart(String queryPart, boolean distinct, String delimiter, boolean wherePart, String... aliases) {
		QueryPart result = new QueryPart(root, queryPart, distinct, delimiter, wherePart);
		queryParts.add(result);
		for (String alias : aliases) {
			queryMap.computeIfAbsent(result.getQueryPart(), a -> new HashSet<>()).add(alias);
		}
		if (aliases.length == 0) {
			queryMap.computeIfAbsent(result.getQueryPart(), a -> new HashSet<>()).add(getUuid());
		}
	}

	private boolean shouldBeAdded(QueryPart queryPart) {
		Collection<String> aliases = queryMap.get(queryPart.getQueryPart());
		boolean result1 = shouldBeAdded(isDistinct(), aliases);
		boolean result2 = queryPart.hasNoEmptyParamValues();
		return result1 && result2;
	}

	public boolean shouldBeAdded(Collection<String> aliases) {
		if (CollectionUtils.isEmpty(aliases)) {
			return false;
		}
		boolean result = aliases.contains(getUuid());
		if (!result) {
			for (String alias : fieldMap.keySet()) {
				result = aliases.contains(alias);
				if (result) {
					break;
				}
				String aliasResolver = resolveAlias(alias);
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

	FieldItem getFieldItem(String alias) {
		if (StringUtils.isBlank(alias)) {
			return null;
		}
		FieldItem result = fieldMap.get(alias);
		if (result == null) {
			String aliasResolver = resolveAlias(alias);
			if (aliasResolver != null) {
				result = fieldPathMap.get(aliasResolver);
			}
		}
		return result;
	}

	public boolean shouldBeAdded(boolean isDistinct, Collection<String> aliases) {
		boolean result = shouldBeAdded(aliases);
		if (result && isDistinct) {
			_setDistinct(true);
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
			String replacement = resolveAlias(varName);
			if (StringUtils.isBlank(replacement)) {
				replacement = "";
			}
			output.append(subString).append(replacement);
			lastStart = matcher.end();
		}
		output.append(path.substring(lastStart));
		return output.toString();
	}

	private void addDefaultOrderRunner(OrderType orderType, boolean add, String... path) {
		addDefaultOrderRunner(orderType, null, null, add, path);
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, boolean add, String... path) {
		addDefaultOrderRunner(orderType, fieldType, null, add, path);
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, String alias, boolean add, String... paths) {
		tryAliasResolver(alias, paths);
		if (alias == null) {
			alias = paths.length > 0 ? adaptAlias(alias, paths[0]).getKey() : null;
		}
		final String finalAlias = alias;
		final boolean useAlias = alias != null;
		defaultOrderBuilderRunners.add(() -> {
			if (add || orderBuilder.shouldSetDefaultOrder()) {
				String[] resolvedPaths = new String[paths.length];
				for (int i = 0; i < paths.length; i++) {
					resolvedPaths[i] = resolvePath(finalAlias, paths[i]);
				}
				if (fieldType == null) {
					if (add) {
						orderBuilder.addDefaultOrder(orderType, resolvedPaths);
					} else {
						orderBuilder.setDefaultOrder(orderType, resolvedPaths);
					}
				} else {
					if (add) {
						orderBuilder.addDefaultOrder(orderType, fieldType, resolvedPaths);
					} else {
						orderBuilder.setDefaultOrder(orderType, fieldType, resolvedPaths);
					}
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
		tryAliasResolver(name, paths);
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

	private void addSearchRunner(String paramName, String paramValue, SearchType searchType, boolean translatable, String... paths) {
		searchPartInitRunners.add(() -> {
			List<SearchPath> resolvedPathList = new ArrayList<>();
			for (int j = 0; j < paths.length; j++) {
				Pair<String, String> resolvedSearchPath = resolveSearchPath(paths[j]);
				resolvedPathList.add(new SearchPath(resolvedSearchPath.getLeft(), resolvedSearchPath.getRight()));
			}
			boolean applyOuterQueryOriginal = applyOuterQuery;
			_setDistinct(false);
			for (String path : paths) {
				addSearchFieldResolver(path, path);
			}
			if (!shouldApplySearch()) {
				_setDistinct(false);
			} else if (applyOuterQuery) {
				//applyOuterSearch = true;
			}
			_setDistinct(applyOuterQuery || applyOuterQueryOriginal);

			List<String> searchTerms = null;
			if (StringUtils.isNotBlank(paramValue)) {
				searchTerms = Collections.singletonList(paramValue);
			} else {
				searchTerms = CollectionUtils.isEmpty(queryRequest.getSearchTerms())
						? (StringUtils.isBlank(queryRequest.getSearchTerm())
								? Collections.emptyList()
								: Collections.singletonList(queryRequest.getSearchTerm()))
						: queryRequest.getSearchTerms();
			}
			List<SearchTerm> result = SearchProcessor.parseSearch(searchTerms);
			List<SearchTerm> andResult = andSearch(result);
			List<SearchTerm> orResult = orSearch(result);
			int i = 0;
			for (SearchTerm searchTerm : andResult) {
				if (StringUtils.isNotBlank(searchTerm.getStringValue())) {
					String p = searchTerm.hasField() ? searchTerm.getField() : (paramName + Integer.toString(i));
					i++;
					SearchBuilder searchBuilder = new SearchBuilder(root, queryRequest, searchTerm, searchType, p,
							translatable, resolvedPathList.toArray(new SearchPath[0]));
					searchPartAndSuppliers.add(QuerySearchUtil.applySearch(root.isNativeQuery(), searchBuilder));
				}
			}
			for (SearchTerm searchTerm : orResult) {
				if (StringUtils.isNotBlank(searchTerm.getStringValue())) {
					String p = searchTerm.hasField() ? searchTerm.getField() : (paramName + Integer.toString(i));
					i++;
					SearchBuilder searchBuilder = new SearchBuilder(root, queryRequest, searchTerm, searchType, p,
							translatable, resolvedPathList.toArray(new SearchPath[0]));
					searchPartOrSuppliers.add(QuerySearchUtil.applySearch(root.isNativeQuery(), searchBuilder));
				}
			}
		});
	}

	private List<SearchTerm> andSearch(List<SearchTerm> searchTerms) {
		return searchTerms.stream().filter(s -> s.getCombination() == Combination.And).collect(Collectors.toList());
	}

	private List<SearchTerm> orSearch(List<SearchTerm> searchTerms) {
		return searchTerms.stream().filter(s -> s.getCombination() == Combination.Or).collect(Collectors.toList());
	}

	private void addOrderFieldResolver(String path, String innerPath) {
		addOrderFieldResolver(null, path, innerPath);
	}

	private void addOrderFieldResolver(String alias, String path, String innerPath) {
		addFieldResolver(alias, path, innerPath, true, false, false);
	}

	private void addSelectFieldResolver(String alias, String path, String innerPath) {
		addFieldResolver(alias, path, innerPath, false, true, false);
	}

	private void addGroupByFieldResolver(String alias, String path, String innerPath) {
		addFieldResolver(alias, path, innerPath, false, false, true);
	}

	private void addSearchFieldResolver(String path, String innerPath) {
		addFieldResolver(null, path, innerPath, false, false, false);
	}

	private void addFieldResolver(String alias, String path, String innerPath, boolean orderField, boolean selectField, boolean groupByField) {
		Pair<String, String> aliasPair = adaptAlias(alias, path);
		addFieldItem(aliasPair.getKey(), aliasPair.getValue(), path, innerPath, orderField, selectField, groupByField);
	}

	private Pair<String, String> adaptAlias(String alias, String path) {
		String[] pathParts = path.split("\\.");
		String innerAlias = pathParts[pathParts.length - 1];
		if (alias == null || alias.equals(path)) {
			alias = innerAlias;
		}
		if (pathParts.length == 1 && resolveAlias(alias) == null) {
			//throw new IllegalArgumentException("Unknown alias: " + alias);
		}
		return Pair.of(alias, innerAlias);
	}

	private void addFieldItem(String alias, String innerAlias, String path, String innerPath, boolean orderField, boolean selectField, boolean groupByField) {
		innerPath = resolveInnerPath(innerAlias, innerPath);
		FieldItem fieldItem = fieldMap.get(alias);
		if (fieldItem == null) {
			fieldItem = new FieldItem(getFieldItemType(orderField, selectField, groupByField), alias, innerAlias, path, innerPath);
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
		if (groupByField) {
			groupByFieldMap.put(alias, fieldItem);
		}
	}

	private FieldItemType getFieldItemType(boolean orderField, boolean selectField, boolean groupByField) {
		if (orderField) {
			return FieldItemType.OrderBy;
		}
		if (selectField) {
			return FieldItemType.Select;
		}
		if (groupByField) {
			return FieldItemType.GroupBy;
		}
		return FieldItemType.Search;
	}

	private void doAddSelect(String selectString) {
		if (selectString.equals("*") || selectString.equals(rootAlias)) {
			selectString = rootAlias + "." + getUuid();
		}
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
				String tryPath = resolveAlias(path);
				if (tryPath != null) {
					addSelectFieldResolver(path, tryPath, tryPath);
					continue;
				}
			}
			addSelectFieldResolver(alias, path, path);
		}
	}

	private void doAddGroupBy(String groupByString) {
		if (StringUtils.isEmpty(groupByString)) {
			return;
		}

		String[] parts = groupByString.replaceAll("(?i)group by", "").split(", ");
		for (String part : parts) {
			String[] subParts = part.split("(?i) as ");
			String path = subParts[0].trim();
			String alias = null;
			if (subParts.length > 1) {
				alias = subParts[1].trim();
			} else if (!path.contains(".")) {
				String tryPath = resolveAlias(path);
				if (tryPath != null) {
					addGroupByFieldResolver(path, tryPath, tryPath);
					continue;
				}
			}
			addGroupByFieldResolver(alias, path, path);
		}
	}

	private void doAddSelect(String alias, String path) {
		addSelectFieldResolver(alias, path, path);
	}

	private void doAddGroupBy(String alias, String path) {
		addGroupByFieldResolver(alias, path, path);
	}

	void init() {
		initPart();
		initSelectPart();
		initGroupByPart();
		initConditionalPart();
		initSearchPart();
		initOrderByPart();
	}

	String getOuterSelectFields() {
		StringBuilder sb = new StringBuilder();
		sb.append(Constants.OUTER_QUERY_PREFIX + "." + getUuid());
		for (FieldItem fieldItem : selectFieldMap.values()) {
			if (!getUuid().equalsIgnoreCase(fieldItem.getAlias())) {
				sb.append(", ");
				sb.append(Constants.OUTER_QUERY_PREFIX + "." + fieldItem.getAlias());
			}
		}

		return sb.toString();
	}

	String getInnerFields(boolean groupBy) {
		StringBuilder sb = new StringBuilder();
		sb.append(getDistinctGroupByFields(groupBy));

		if (root.applyOuterQuery()) {
			for (FieldItem fieldItem : orderFieldMap.values()) {
				if (fieldItem != null && !getUuid().equalsIgnoreCase(fieldItem.getAlias()) && selectFieldMap.get(fieldItem.getAlias()) == null
						&& groupByFieldMap.get(fieldItem.getAlias()) == null) {
					sb.append(", ");
					sb.append(fieldItem.getInnerPath(rootAlias, groupBy));
				}
			}
		}

		return sb.toString();
	}

	String getDistinctGroupByFields(boolean groupBy) {
		StringBuilder sb = new StringBuilder();
		String rootSelect = getRootSelect(groupBy);

		for (FieldItem fieldItem : selectFieldMap.values()) {
			if (!getUuid().equalsIgnoreCase(fieldItem.getAlias())) {
				sb.append(", ");
				sb.append(fieldItem.getInnerPath(rootAlias, groupBy));
			}
		}

		String groupByFields = getGroupByFields(groupBy);
		if (StringUtils.isNotBlank(groupByFields)) {
			sb.append(groupByFields);
		}

		if (root.isSkipUuid() && sb.length() > 0) {
			sb.deleteCharAt(0);
			sb.deleteCharAt(0);
			return sb.toString();
		}

		return rootSelect + sb.toString();
	}

	String getDistinctUuidFields(boolean groupBy) {
		StringBuilder sb = new StringBuilder();
		sb.append(getRootSelect(groupBy));

		String groupByFields = getGroupByFields(groupBy);
		if (StringUtils.isNotBlank(groupByFields)) {
			sb.append(groupByFields);
		}

		return sb.toString();
	}

	String getGroupByFields(boolean groupBy) {
		StringBuilder sb = new StringBuilder();

		for (FieldItem fieldItem : groupByFieldMap.values()) {
			if (!getUuid().equalsIgnoreCase(fieldItem.getAlias()) && selectFieldMap.get(fieldItem.getAlias()) == null) {
				sb.append(", ");
				sb.append(fieldItem.getInnerPath(rootAlias, groupBy));
			}
		}

		return sb.toString();
	}

	List<SelectField> getSelectFields() {
		List<SelectField> result = new ArrayList<>();
		addRootSelect(result);
		for (FieldItem fieldItem : selectFieldMap.values()) {
			SelectField selectField = new SelectField(fieldItem.getAlias(), fieldItem.getInnerPath(rootAlias));
			result.add(selectField);
		}
		return result;
	}

	List<QueryPartString> resolveInnerFrom() {
		return queryParts.stream().filter(s -> shouldBeAdded(s)).map(s -> s.getQueryPart()).collect(Collectors.toList());
	}

	String getAndSearchPart() {
		String result = null;
		for (String searchPartSupplier : searchPartAndSuppliers) {
			result = QueryFunctions.and(result, searchPartSupplier);
		}
		if (StringUtils.isBlank(result)) {
			return "";
		}
		return result;
	}

	String getOrSearchPart() {
		String result = null;
		for (String searchPartSupplier : searchPartOrSuppliers) {
			result = QueryFunctions.or(result, searchPartSupplier);
		}
		if (StringUtils.isBlank(result)) {
			return "";
		}
		if (searchPartOrSuppliers.size() > 1) {
			result = "( " + result + " )";
		}
		return result;
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

	private void initGroupByPart() {
		for (Runnable groupByBuilderRunner : groupByBuilderRunners) {
			groupByBuilderRunner.run();
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

	String getOrderByPart() {
		return QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	String getBasicOrderByPart(String searchPart) {
		return (searchPart.length() == 0 || searchPart.substring(searchPart.length() - 1).equals(" ") ? "" : " ")
				+ "ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	String getOuterOrderByPart() {
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	String getPagedPart() {
		return QueryRequestUtil.isPaged(queryRequest) ? " LIMIT ?limit OFFSET ?offset" : "";
	}

	private Pair<String, String> resolveSearchPath(String path) {
		String resultPath = null;
		String resultAlias = null;
		String[] pathParts = path.split("\\.");
		if (pathParts.length == 1) {
			resultAlias = path;
			String test = resolveAlias(path);
			if (test == null) {
				resultPath = getRootPath(path);
			} else {
				resultPath = test;
			}
		} else {
			resultPath = path;
			resultAlias = pathParts[pathParts.length - 1];
		}
		return Pair.of(resultAlias, resultPath);
	}

	private String resolvePath(String alias, String path) {
		String[] pathParts = path.split("\\.");
		String test = resolveAlias(alias);
		if (alias == null || alias.equals(path)) {
			String innerAlias = pathParts[pathParts.length - 1];
			alias = innerAlias;
		}
		if (pathParts.length == 1 && test == null) {
			path = getRootPath(alias);
		} else if (pathParts.length == 1 && test != null) {
			path = test;
		}
		return root.applyOuterQuery() ? (Constants.OUTER_QUERY_PREFIX + "." + alias) : path;
	}

	private String resolveInnerPath(String alias, String path) {
		String[] pathParts = path.split("\\.");
		String test = resolveAlias(alias);
		if (pathParts.length == 1 && test != null) {
			path = test;
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

	void orderBy(String customOrderByPart) {
		this.customOrderByPart = customOrderByPart;
	}

	String buildOrderByResult() {
		if (customOrderByPart == null && CollectionUtils.isEmpty(orderBuilder.getOrderFields())) {
			return "";
		}
		return " ORDER BY " + (customOrderByPart == null ? QueryOrderUtil.applyOrder(orderBuilder.getOrderFields()) : customOrderByPart);
	}

	public void groupBy(String groupByString) {
		setDistinct();
		groupByBuilderRunners.add(() -> {
			doAddGroupBy(groupByString);
		});
	}

	public void addGroupBy(String alias, String path) {
		setDistinct();
		tryAliasResolver(alias, path);
		groupByBuilderRunners.add(() -> {
			doAddGroupBy(alias, path);
		});
	}

	String buildGroupByResult() {
		String groupByPart = isDistinct() ? getInnerFields(true) : getGroupByFields(true);
		if (StringUtils.isBlank(groupByPart)) {
			return "";
		}
		return " GROUP BY " + groupByPart;
	}

	boolean isApplyOuterQuery(boolean isNative) {
		return applyOuterQuery && isNative;
	}

	private String getRootSelect(boolean groupBy) {
		String result = "";
		if (root.isNativeQuery()) {
			result = rootAlias + "." + getUuid();
			if (!groupBy && root.isShowUuidAlias()) {
				result += " AS " + root.getUuidAlias();
			}
		} else {
			result = rootAlias;
		}
		return result;
	}

	private void addRootSelect(List<SelectField> selectFields) {
		if (root.isNativeQuery() && !root.isSkipUuid()) {
			SelectField selectField = new SelectField(getUuid(), rootAlias + "." + getUuid());
			selectFields.add(selectField);
		}
	}

	private String getUuid() {
		return root.getUuidField();
	}

	private String resolveAlias(String alias) {
		return aliasResolverMap.get(alias);
	}

	private String getRootPath(String alias) {
		return rootAlias + "." + alias;
	}

}
