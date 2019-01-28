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

	private Map<String, Set<String>> aliasResolverMap = new HashMap<>();

	private Map<String, FieldItem> orderFieldMap = new LinkedHashMap<>();

	private List<String> searchParts = new ArrayList<>();

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
			SearchBuilder searchBuilder = new SearchBuilder(queryRequest, searchTermField, false, normalizePaths(paths));
			searchParts.add(QuerySearchUtil.applySearch(true, searchBuilder));
		}
		for (String path : paths) {
			addFieldResolver(path);
		}
	}

	private String[] normalizePaths(String... paths) {
		String[] result = new String[paths.length];
		int i = 0;
		for (String path : paths) {
			String toAdd = null;
			if (path.split("\\.").length == 1) {
				toAdd = COMPOSER_PREFIX + "." + path;
			} else {
				toAdd = path;
			}
			result[i] = toAdd;
			i++;
		}
		return result;
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
		orderBuilder.setDefaultOrder(orderType, fieldType, path);
		addOrderFieldResolver(path);
	}

	public void setDefaultOrder(OrderType orderType, String path) {
		orderBuilder.setDefaultOrder(orderType, path);
		addOrderFieldResolver(path);
	}

	public void setDefaultOrder(OrderType orderType, FieldType fieldType, String alias, String path) {
		orderBuilder.setDefaultOrder(orderType, fieldType, path);
		addOrderFieldResolver(alias, path);
	}

	public void setDefaultOrder(OrderType orderType, String alias, String path) {
		orderBuilder.setDefaultOrder(orderType, path);
		addOrderFieldResolver(alias, path);
	}

	public void bindOrder(String name, FieldType fieldType, String path) {
		orderBuilder.bindOrder(name, fieldType, path);
		addOrderFieldResolver(name, path);
	}

	public void bindOrder(String name, String path) {
		orderBuilder.bindOrder(name, path);
		addOrderFieldResolver(name, path);
	}

	private void addOrderFieldResolver(String path) {
		addFieldResolver(null, path, true);
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
		for (String searchPart : searchParts) {
			result = QueryFunctions.and(result, searchPart);
		}
		return result == null ? "" : " WHERE " + result;
	}

	private String getOrderByPart() {
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getBasicOrderByPart() {
		return " ORDER BY " + QueryOrderUtil.applyOrder(orderBuilder.getOrderFields());
	}

	private String getPagedPart() {
		return QueryRequestUtil.isPaged(queryRequest) ? " LIMIT ?limit OFFSET ?offset" : "";
	}

}
