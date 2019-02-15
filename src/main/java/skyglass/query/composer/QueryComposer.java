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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import skyglass.query.QueryFunctions;
import skyglass.query.QueryOrderUtil;
import skyglass.query.QueryRequestUtil;
import skyglass.query.QuerySearchUtil;
import skyglass.query.builder.FieldType;
import skyglass.query.builder.OrderBuilder;
import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.SearchBuilder;
import skyglass.query.builder.config.Constants;

public class QueryComposer {

	private final String COMPOSER_PREFIX = "tab";

	private QueryRequestDTO queryRequest;

	private boolean applyComposer;

	private Map<String, Set<String>> queryMap = new LinkedHashMap<>();

	private Collection<String> queryParts = new ArrayList<>();

	private List<FieldItem> fieldItems = new ArrayList<>();

	private Map<String, FieldItem> fieldMap = new LinkedHashMap<>();

	private Map<String, FieldItem> fieldPathMap = new LinkedHashMap<>();

	private Map<String, Set<String>> aliasResolverMap = new HashMap<>();

	private Map<String, FieldItem> orderFieldMap = new LinkedHashMap<>();

	private List<Supplier<String>> searchPartSuppliers = new ArrayList<>();

	private List<Runnable> orderBuilderRunners = new ArrayList<>();

	private OrderBuilder orderBuilder;

	private String rootAlias;

	private String rootTable;

	private String queryStr = null;

	private String countQueryStr = null;

	private Map<String, String> queryParameters = new HashMap<>();

	public QueryComposer(QueryRequestDTO queryRequest, String rootAlias, String rootTable) {
		this.queryRequest = queryRequest;
		this.orderBuilder = new OrderBuilder(queryRequest);
		this.rootAlias = rootAlias;
		this.rootTable = rootTable;
	}

	public void setDistinct() {
		this.applyComposer = true;
	}

	public void add(String queryPart) {
		queryParts.add(queryPart);
		queryMap.computeIfAbsent(queryPart, a -> new HashSet<>()).add(Constants.UUID);
	}

	public void addConditional(String queryPart, String... aliases) {
		queryParts.add(queryPart);
		for (String alias : aliases) {
			queryMap.computeIfAbsent(queryPart, a -> new HashSet<>()).add(alias);
		}
	}

	private boolean shouldBeAdded(String queryPart) {
		Collection<String> aliases = queryMap.get(queryPart);
		if (CollectionUtils.isEmpty(aliases)) {
			return false;
		}
		boolean result = aliases.contains(Constants.UUID);
		if (result) {
			return true;
		}
		for (String alias : fieldMap.keySet()) {
			result = aliases.contains(alias);
			if (result) {
				return true;
			}
			Set<String> aliasResolvers = aliasResolverMap.get(alias);
			if (!CollectionUtils.isEmpty(aliasResolvers)) {
				for (String aliasResolver : aliasResolvers) {
					result = aliases.contains(aliasResolver);
					if (result) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public void addSearch(String... paths) {
		List<String> searchTerms = CollectionUtils.isEmpty(queryRequest.getSearchTerms())
				? (StringUtils.isBlank(queryRequest.getSearchTerm())
						? Collections.emptyList()
						: Collections.singletonList(queryRequest.getSearchTerm()))
				: queryRequest.getSearchTerms();
		for (int i = 0; i < searchTerms.size(); i++) {
			String searchTermField = SearchBuilder.SEARCH_TERM_FIELD + Integer.toString(i);
			searchPartSuppliers.add(() -> {
				String[] resolvedPaths = new String[paths.length];
				for (int j = 0; j < paths.length; j++) {
					resolvedPaths[j] = resolvePath(paths[j]);
				}
				SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, resolvedPaths);
				return QuerySearchUtil.applySearch(true, searchBuilder);
			});
		}
		for (String path : paths) {
			addFieldResolver(path);
		}
	}

	public void addAliasResolver(String alias, String... paths) {
		for (String path : paths) {
			String[] pathParts = path.split("\\.");
			if (pathParts.length == 1) {
				applyComposer = true;
			} else if (pathParts[0].equals(COMPOSER_PREFIX)) {
				applyComposer = true;
			}
			aliasResolverMap.computeIfAbsent(alias, a -> new HashSet<>()).add(path);
		}
	}

	public void addParameter(String name, String value) {
		queryParameters.put(name, value);
	}

	public void setDefaultOrder(OrderType orderType, FieldType fieldType, String path) {
		addDefaultOrderRunner(orderType, fieldType, path);
	}

	public void setDefaultOrder(OrderType orderType, String path) {
		addDefaultOrderRunner(orderType, path);
	}

	public void setDefaultOrder(OrderType orderType, FieldType fieldType, String alias, String path) {
		addDefaultOrderRunner(orderType, fieldType, alias, path);
	}

	public void setDefaultOrder(OrderType orderType, String alias, String path) {
		addDefaultOrderRunner(orderType, alias, path);
	}

	public void bindOrder(String name, FieldType fieldType, String path) {
		addBindOrderRunner(name, fieldType, path);
	}

	public void bindOrder(String name, String path) {
		addBindOrderRunner(name, path);
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.setDefaultOrder(orderType, fieldType, resolvedPath);
			addOrderFieldResolver(resolvedPath);
		});
	}

	private void addDefaultOrderRunner(OrderType orderType, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.setDefaultOrder(orderType, resolvedPath);
			addOrderFieldResolver(resolvedPath);
		});
	}

	private void addDefaultOrderRunner(OrderType orderType, FieldType fieldType, String alias, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.setDefaultOrder(orderType, fieldType, resolvedPath);
			addOrderFieldResolver(alias, resolvedPath);
		});
	}

	private void addDefaultOrderRunner(OrderType orderType, String alias, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.setDefaultOrder(orderType, resolvedPath);
			addOrderFieldResolver(alias, resolvedPath);
		});
	}

	private void addBindOrderRunner(String name, FieldType fieldType, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.bindOrder(name, fieldType, resolvedPath);
			addOrderFieldResolver(name, resolvedPath);
		});
	}

	private void addBindOrderRunner(String name, String path) {
		orderBuilderRunners.add(() -> {
			String resolvedPath = resolvePath(path);
			orderBuilder.bindOrder(name, resolvedPath);
			addOrderFieldResolver(name, resolvedPath);
		});
	}

	private void addOrderFieldResolver(String path) {
		addOrderFieldResolver(null, path);
	}

	private void addOrderFieldResolver(String alias, String path) {
		addFieldResolver(alias, path, true);
	}

	private void addFieldResolver(String path) {
		addFieldResolver(null, path, false);
	}

	private void addFieldResolver(String alias, String path, boolean orderField) {
		String[] pathParts = path.split("\\.");
		String innerAlias = pathParts[pathParts.length - 1];
		if (pathParts.length == 1 || pathParts[0].equals(COMPOSER_PREFIX)) {
			applyComposer = true;
		}
		if (alias == null) {
			alias = innerAlias;
		}
		addFieldItem(alias, innerAlias, path, orderField);
	}

	private void addFieldItem(String alias, String innerAlias, String path, boolean orderField) {
		FieldItem fieldItem = fieldMap.get(alias);
		if (fieldItem == null) {
			fieldItem = new FieldItem(alias, innerAlias, path);
			fieldItems.add(fieldItem);
			fieldMap.put(alias, fieldItem);
			fieldPathMap.put(path, fieldItem);
		}
		if (orderField) {
			orderFieldMap.put(alias, fieldItem);
		}
	}

	public void addSelect(String selectString) {
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
			}
			addFieldResolver(alias, path, false);
		}
	}

	public void addSelect(String alias, String path) {
		addFieldResolver(alias, path, false);
	}

	public void init() {
		String fromBasicQueryStr = "";
		List<String> parts = resolveInnerFrom();
		for (String queryPart : parts) {
			fromBasicQueryStr += queryPart;
		}

		if (applyComposer) {
			//This query part selects column names, for which sorting is supported. All these columns should exist in entity table or in tables, which have one to one correspondence
			//Therefore GROUP BY by all these columns guarantees uniquness of entity's tab.UUID and we won't have duplicates
			String outerComposerSelect = "SELECT " + getOuterComposerFields();

			//This query part selects column names, for which search is supported. Some of these columns might have one to many correspondence with entity table, when JOIN is applied
			//Therefore this query might return duplicates. That's why we need to wrap this inner query, and apply GROUP BY, which will eliminate duplicates (see previous comment)
			String innerComposerSelect = "SELECT " + getInnerComposerFields();

			//This query part selects column names, which will be used by outer query. 
			String innerSelect = "SELECT " + getInnerFields();

			String fromQueryStr = " FROM ( "
					+ innerComposerSelect
					+ " FROM ( "
					+ innerSelect + " " + fromBasicQueryStr;

			fromQueryStr += " ) tab"
					+ getSearchPart()
					+ " ) tab "
					+ " GROUP BY " + getOuterComposerFields() + " ";

			queryStr = outerComposerSelect + fromQueryStr + getOrderByPart() + getPagedPart();
			countQueryStr = "SELECT DISTINCT COUNT(*) OVER () " + fromQueryStr;
		} else {
			String selectQueryStr = "SELECT " + rootAlias + "." + Constants.UUID;
			queryStr = selectQueryStr + fromBasicQueryStr + getBasicOrderByPart() + getPagedPart();
			countQueryStr = "SELECT COUNT(*) " + fromBasicQueryStr;
		}
	}

	public String getCountQueryStr() {
		return countQueryStr;
	}

	public String getQueryStr() {
		init();
		return queryStr;
	}

	private String getOuterComposerFields() {
		String result = COMPOSER_PREFIX + "." + Constants.UUID;
		for (FieldItem fieldItem : orderFieldMap.values()) {
			if (!Constants.UUID.equals(fieldItem.getAlias())) {
				result += ", " + COMPOSER_PREFIX + "." + fieldItem.getAlias();
			}
		}
		return result;
	}

	private String getInnerComposerFields() {
		String result = COMPOSER_PREFIX + "." + Constants.UUID;
		for (FieldItem fieldItem : fieldMap.values()) {
			if (!Constants.UUID.equals(fieldItem.getAlias())) {
				result += ", " + COMPOSER_PREFIX + "." + fieldItem.getAlias();
			}
		}
		return result;
	}

	private String getInnerFields() {
		String result = rootAlias + "." + Constants.UUID;
		for (FieldItem fieldItem : fieldMap.values()) {
			if (!Constants.UUID.equals(fieldItem.getAlias())) {
				result += ", " + fieldItem.getInnerSelect(rootAlias);
			}
		}
		return result;
	}

	private List<String> resolveInnerFrom() {
		return queryParts.stream().filter(s -> shouldBeAdded(s)).collect(Collectors.toList());
	}

	private String getSearchPart() {
		String result = null;
		for (Supplier<String> searchPartSupplier : searchPartSuppliers) {
			result = QueryFunctions.and(result, searchPartSupplier.get());
		}
		return result == null ? "" : " WHERE " + result;
	}

	private String getOrderByPart() {
		for (Runnable orderBuilderRunner : orderBuilderRunners) {
			orderBuilderRunner.run();
		}
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getBasicOrderByPart() {
		for (Runnable orderBuilderRunner : orderBuilderRunners) {
			orderBuilderRunner.run();
		}
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getPagedPart() {
		return QueryRequestUtil.isPaged(queryRequest) ? " LIMIT ?limit OFFSET ?offset" : "";
	}

	private String resolvePath(String path) {
		return applyComposer ? (COMPOSER_PREFIX + "." + fieldPathMap.get(path).getAlias()) : path;
	}

}
