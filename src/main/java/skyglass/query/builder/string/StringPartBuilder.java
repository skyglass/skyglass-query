package skyglass.query.builder.string;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import skyglass.query.builder.SearchBuilder;
import skyglass.query.builder.SearchType;

public class StringPartBuilder extends QueryParamBuilder {

	public static final String SEARCH_TERM_FIELD = "searchTerm";

	private static final String OR_SUFFIX = " )";

	QueryComposer root;

	private StringPartBuilder parent;

	private StringBuilder sb = new StringBuilder();

	private boolean condition = true;
	
	private boolean distinct;

	private boolean alreadyBuilt;

	private String delimiter;

	private boolean delimiterStart;

	private int orCount;

	public StringPartBuilder(QueryComposer root, StringPartBuilder parent) {
		this(root, parent, null);
	}

	public StringPartBuilder(QueryComposer root, StringPartBuilder parent, String delimiter) {
		this.root = root;
		this.parent = parent;
		this.delimiter = delimiter;
	}

	protected StringPartBuilder build(StringBuilder part) {
		return build(part, null, params, false);
	}

	protected StringPartBuilder build(StringBuilder part, StringBuilder alternativePart) {
		return build(part, alternativePart, params, false);
	}

	protected StringPartBuilder buildAll(StringBuilder part, StringBuilder alternativePart,
			Collection<QueryParam> childParams) {
		return build(part, alternativePart, combine(params, childParams), true);
	}

	protected StringPartBuilder parentBuildAll(StringBuilder part, StringBuilder alternativePart,
			Collection<QueryParam> childParams) {
		if (parent != null) {
			return parent.buildAll(part, alternativePart, childParams);
		}
		return this;
	}

	protected StringPartBuilder build(StringBuilder part, StringBuilder alternativePart, Collection<QueryParam> params,
			boolean buildAll) {
		if (!alreadyBuilt) {
			if (isFalseCondition(params)) {
				if (buildAll) {
					return parentBuildAll(null, alternativePart, null);
				}
				return parentAppendStringBuilder(alternativePart, null);
			}
			doAppend(part);
			alreadyBuilt = true;
			return parent(sb, params, buildAll);
		}
		return parent(buildAll);
	}

	private StringPartBuilder parent(boolean buildAll) {
		return parent(null, null, buildAll);
	}

	private StringPartBuilder parent(StringBuilder sb, Collection<QueryParam> params, boolean buildAll) {
		if (parent == null) {
			if (params != null) {
				for (QueryParam queryParam : params) {
					root.setParam(queryParam);
				}
			}
			return this;
		}
		if (buildAll) {
			endAll();
			return parentBuildAll(sb, null, params);
		}
		return parentAppendStringBuilder(sb, params);
	}

	public StringPartBuilder start() {
		return start(null);
	}

	public StringPartBuilder start(String part) {
		return start(part, false);
	}

	public StringPartBuilder start(String part, boolean parseParams) {
		if (parseParams) {
			part = QueryParamProcessor.parseParams(root, this, part);
		}
		doAppendWithoutDelimiter(stringBuilder(part));
		return this;
	}

	//does nothing, only for indentation
	public StringPartBuilder __() {
		return this;
	}

	//does nothing, only for indentation
	public StringPartBuilder ____() {
		return this;
	}

	//does nothing, only for indentation
	public StringPartBuilder ______() {
		return this;
	}

	//does nothing, only for indentation
	public StringPartBuilder ________() {
		return this;
	}

	public QueryComposer end() {
		return end(null);
	}

	public QueryComposer end(StringBuilder part) {
		buildAll(part, null, null);
		return root;
	}

	public StringPartBuilder append(String part) {
		doAppend(stringBuilder(part));
		return this;
	}

	public StringPartBuilder appendBuilder(QueryComposer builder) {
		String result = builder.buildPart();
		Collection<QueryParam> params = builder.getParams();
		if (isTrueCondition(params)) {
			addParams(params);
			doAppend(stringBuilder(result));
		}
		return this;
	}

	public StringPartBuilder addAliases(String... aliases) {
		return addAliases(false, aliases);
	}
	
	public StringPartBuilder addAliases(boolean distinct, String... aliases) {
		this.distinct = distinct;
		for (String alias: aliases) {
			this.params.add(QueryParam.create(alias, AliasType.Alias));
		}
		return this;
	}
	
	public StringPartBuilder setCondition(boolean condition) {
		this.condition = condition;
		return this;
	}
	
	public StringPartBuilder setDistinct(boolean distinct) {
		this.distinct = distinct;
		return this;
	}

	public StringPartBuilder addCondition(boolean condition) {
		return addCondition(condition, false);
	}
	
	public StringPartBuilder addCondition(boolean condition, boolean distinct) {
		this.condition = this.condition && condition;
		this.distinct = distinct;
		return this;
	}

	public StringPartBuilder setParameter(String name, Object value) {
		_doSetParameter(name, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> StringPartBuilder setParameters(String name, T... values) {
		return doSetParameters(name, Arrays.asList(values));
	}

	@SuppressWarnings({ "rawtypes" })
	public StringPartBuilder setParameterList(String name, Collection values) {
		return doSetParameters(name, values);
	}

	@SuppressWarnings("rawtypes")
	private StringPartBuilder doSetParameters(String name, Collection values) {
		_doSetParameters(root, name, values);
		return this;
	}

	private StringPartBuilder addParams(Collection<QueryParam> newParams) {
		if (CollectionUtils.isNotEmpty(newParams)) {
			for (QueryParam param : newParams) {
				params.add(param);
			}
		}
		return this;
	}

	public StringPartBuilder appendNullableList(String propertyName, Collection<?> list) {
		return appendNullableList(propertyName, QueryParamProcessor.parseParamName(propertyName), list);
	}

	public StringPartBuilder appendNullableListOrElseFalse(String propertyName, Collection<?> list) {
		return appendNullableListOrElseFalse(propertyName, QueryParamProcessor.parseParamName(propertyName), list);
	}

	public StringPartBuilder appendNullableList(String propertyName, String paramName, Collection<?> list) {
		return appendNullableList(propertyName, paramName, list, false);
	}

	public StringPartBuilder appendNullableListOrElseFalse(String propertyName, String paramName, Collection<?> list) {
		return appendNullableList(propertyName, paramName, list, true);
	}

	private StringPartBuilder appendNullableList(String propertyName, String paramName, Collection<?> list, boolean orElseFalse) {
		StringPartBuilder result = startNullablePart(QueryParamProcessor.getInString(root, propertyName, paramName, list)).setParameterList(paramName, list);
		return orElseFalse ? result.orElse("1 = 0") : result.skipIfFalse();
	}

	public StringPartBuilder appendNullableValues(String part, String paramName, Object... values) {
		return appendNullableValue(part, paramName, Arrays.asList(values));
	}

	public StringPartBuilder appendNullableValuesOrElseFalse(String part, String paramName, Object... values) {
		return appendNullableValue(part, paramName, Arrays.asList(values));
	}

	public StringPartBuilder appendNullableValues(String part, String paramName, Collection<?> list) {
		return appendNullable(part, paramName, list, false);
	}

	public StringPartBuilder appendNullableValuesOrElseFalse(String part, String paramName, Collection<?> list) {
		return appendNullable(part, paramName, list, true);
	}

	public StringPartBuilder appendNullableValue(String part, String paramName, Object value) {
		return appendNullable(part, paramName, value, false);
	}

	public StringPartBuilder appendNullableValueOrElseFalse(String part, String paramName, Object value) {
		return appendNullable(part, paramName, value, true);
	}
	
	public StringPartBuilder appendNullable(String part, String... aliasNames) {
		StringPartBuilder result = null;
		for (String aliasName: aliasNames) {
			result = appendNullable(part, aliasName, AliasType.Alias, false);
		}
		return result;
	}

	public StringPartBuilder appendNullableOrElseFalse(String part, String... aliasNames) {
		StringPartBuilder result = null;
		for (String aliasName: aliasNames) {
			result = appendNullable(part, aliasName, AliasType.Alias, true);
		}
		return result;
	}

	private StringPartBuilder appendNullable(String part, String paramName, Object value, boolean orElseFalse) {
		StringPartBuilder result = startNullablePart(QueryParamProcessor.processPart(root, this, paramName, part, value));
		result = isCollection(value) ? result.setParameterList(paramName, (Collection<?>) value) : result.setParameter(paramName, value);
		result = orElseFalse ? result.orElse("1 = 0") : result.skipIfFalse();
		return result;
	}

	public StringPartBuilder appendNullable(String part) {
		return appendNullable(part, false);
	}

	public StringPartBuilder appendNullableOrElseFalse(String part) {
		return appendNullable(part, true);
	}

	private StringPartBuilder appendNullable(String part, boolean orElseFalse) {
		StringPartBuilder result = startNullablePart(part);
		result = orElseFalse ? result.orElse("1 = 0") : result.skipIfFalse();
		return result;
	}

	public StringPartBuilder startNullable() {
		return startNullablePart(null);
	}

	public StringPartBuilder startNullablePart(String part) {
		return new StringPartBuilder(root, this).start(part, true);
	}

	public StringPartBuilder startNullableValue(String part, String paramName, Object value) {
		return startNullablePart(part).setParameter(paramName, value);
	}
	
	public StringPartBuilder startNullable(String part, String... aliasNames) {
		StringPartBuilder result = null;
		for (String aliasName: aliasNames) {
			result = startNullablePart(part).setParameter(aliasName, AliasType.Alias);
		}
		return result;
	}

	@SuppressWarnings({ "rawtypes", "unused" })
	private StringPartBuilder forEach(String paramName, String prefix, String suffix, String part, String delimiter,
			Collection list) {
		doAppendWithoutDelimiter(stringBuilder(prefix));
		for (int i = 1; i <= list.size(); i++) {
			String index = Integer.toString(i);
			if (i > 1) {
				doAppendWithoutDelimiter(stringBuilder(delimiter));
			}
			doAppendWithoutDelimiter(stringBuilder(part.replace("{index}", index)));
		}
		doAppendWithoutDelimiter(stringBuilder(suffix));
		setParameterList(paramName, list);
		return this;
	}

	public StringPartBuilder ifTrue(boolean condition) {
		if (condition) {
			return new StringPartBuilder(root, this);
		}
		return this;
	}

	private StringPartBuilder skipIfFalse() {
		return skipIfFalse(null);
	}

	private StringPartBuilder skipIfFalse(String suffix) {
		doAppendWithoutDelimiter(stringBuilder(suffix));
		return build(null, null);
	}

	public StringPartBuilder orElse(String part) {
		return build(null, stringBuilder(part));
	}

	public StringBuilder getResult() {
		if (!alreadyBuilt) {
			build(null, null);
			alreadyBuilt = true;
		}
		return sb;
	}

	protected boolean hasResult() {
		return sb != null && sb.length() > 0;
	}

	public StringPartBuilder startIf() {
		return startIf(true);
	}

	public StringPartBuilder startIf(boolean condition) {
		return startIf(condition, false, null);
	}

	public StringPartBuilder startIf(String part) {
		return startIf(true, false, part);
	}

	public StringPartBuilder startIf(boolean condition, String part) {
		return startIf(condition, false, part);
	}
	
	public StringPartBuilder startIf(boolean condition, boolean distinct, String part) {
		return new StringPartBuilder(root, this).start(part).addCondition(condition, distinct);
	}

	public StringPartBuilder startIf(String part, String... aliases) {
		return startIf(false, part, aliases);
	}
	
	public StringPartBuilder startIf(boolean distinct, String part, String... aliases) {
		return new StringPartBuilder(root, this).start(part).addAliases(distinct, aliases);
	}

	public StringPartBuilder startElse() {
		return startElse(null);
	}

	public StringPartBuilder startElse(String part) {
		return stopIf().startIf(true, part);
	}

	public StringPartBuilder appendIfTrue(boolean condition, String part) {
		return startIf(condition, part).skipIfFalse();
	}
	
	public StringPartBuilder appendIfAliases(String part, String... aliases) {
		return appendIfAliases(false, part, aliases);
	}
	
	public StringPartBuilder appendIfAliases(boolean distinct, String part, String... aliases) {
		return startIf(distinct, part, aliases);
	}

	public StringPartBuilder startAnd() {
		return startJoining(" AND ");
	}

	public StringPartBuilder startOr() {
		return startJoining(" OR ", "( ").startOrCount();
	}

	public StringPartBuilder stopAnd() {
		return endJoining();
	}

	public StringPartBuilder stopOr() {
		orCount--;
		return endJoining(OR_SUFFIX);
	}

	public StringPartBuilder stopIf() {
		return skipIfFalse();
	}

	public StringPartBuilder stopElse() {
		return skipIfFalse();
	}

	public StringPartBuilder addTranslatableSearch(String... searchFields) {
		return addSearch(SEARCH_TERM_FIELD, QueryParamProcessor.parseSearchTerm(root), SearchType.IgnoreCase, true, searchFields);
	}

	public StringPartBuilder addTranslatableSearch(SearchType searchType, String... searchFields) {
		return addSearch(SEARCH_TERM_FIELD, QueryParamProcessor.parseSearchTerm(root), searchType, true, searchFields);
	}

	public StringPartBuilder addSearch(SearchType searchType, String... searchFields) {
		return addSearch(SEARCH_TERM_FIELD, QueryParamProcessor.parseSearchTerm(root), searchType, false, searchFields);
	}

	public StringPartBuilder addSearch(String... searchFields) {
		return addSearch(SEARCH_TERM_FIELD, QueryParamProcessor.parseSearchTerm(root), SearchType.IgnoreCase, false, searchFields);
	}

	public StringPartBuilder addTranslatableSearch(String paramName, String paramValue, SearchType searchType, String... searchFields) {
		return addSearch(paramName, paramValue, searchType, true, searchFields);
	}

	public StringPartBuilder addSearch(String paramName, String paramValue, SearchType searchType, String... searchFields) {
		return addSearch(paramName, paramValue, searchType, false, searchFields);
	}

	private StringPartBuilder endOr() {
		while (orCount > 0) {
			doAppendWithoutDelimiter(stringBuilder(OR_SUFFIX));
			orCount--;
		}
		return this;
	}

	public StringPartBuilder stopNullable() {
		return skipIfFalse();
	}

	private StringPartBuilder startJoining(String delimiter) {
		return startJoining(delimiter, null);
	}

	private StringPartBuilder startJoining(String delimiter, String prefix) {
		return new StringPartBuilder(root, this, delimiter).start(prefix);
	}

	private StringPartBuilder endJoining() {
		return skipIfFalse();
	}

	private StringPartBuilder endJoining(String suffix) {
		return skipIfFalse(suffix);
	}

	private StringPartBuilder appendStringBuilder(StringBuilder sb, Collection<QueryParam> queryParams) {
		if (isFalseCondition(params)) {
			return this;
		}
		addParams(queryParams);
		doAppend(sb);
		return this;
	}

	private StringPartBuilder parentAppendStringBuilder(StringBuilder sb, Collection<QueryParam> queryParams) {
		if (parent != null) {
			return parent.appendStringBuilder(sb, queryParams);
		}
		return this;
	}

	private void doAppend(StringBuilder part) {
		if (StringUtils.isNotBlank(delimiter) && sb.length() > 0 && part != null && part.length() > 0
				&& delimiterStart) {
			sb.append(delimiter);
		}
		doAppendWithoutDelimiter(part);
		if (StringUtils.isNotBlank(delimiter) && sb.length() > 0 && part != null && part.length() > 0) {
			delimiterStart = true;
		}
	}

	private void doAppendWithoutDelimiter(StringBuilder part) {
		if (part != null && part.length() > 0) {
			sb.append(part);
		}
	}

	private boolean isTrueCondition(Collection<QueryParam> params) {
		return !isFalseCondition(params);
	}

	private boolean isFalseCondition(Collection<QueryParam> params) {
		if (!condition) {
			return true;
		}
		if (params == null) {
			return false;
		}
		for (QueryParam queryParam : params) {
			if (queryParam instanceof AliasParam
					&& !root.shouldBeAdded(distinct, Collections.singletonList(queryParam.getName()))) {
				return true;
			} else if (isEmpty(queryParam.getValue())) {
				return true;
			}
		}
		return false;
	}
	
	private Collection<QueryParam> combine(Collection<QueryParam> params, Collection<QueryParam> childParams) {
		if (CollectionUtils.isEmpty(params) && CollectionUtils.isEmpty(childParams)) {
			return null;
		}
		if (CollectionUtils.isNotEmpty(params) && CollectionUtils.isEmpty(childParams)) {
			return params;
		}
		if (CollectionUtils.isEmpty(params) && CollectionUtils.isNotEmpty(childParams)) {
			return childParams;
		}
		params.addAll(childParams);
		return params;
	}

	private StringBuilder stringBuilder(String part) {
		return StringUtils.isBlank(part) ? null : new StringBuilder(part);
	}

	private void endAll() {
		endOr();
	}

	private StringPartBuilder startOrCount() {
		orCount++;
		return this;
	}

	private StringPartBuilder addSearch(String paramName, String paramValue, SearchType searchType, boolean translatable, String... searchFields) {
		setSearchParameter(paramName, paramValue, searchType);
		append(QueryProcessor.applySearch(root.getQueryRequest(), searchType, paramName, translatable, root.isNativeQuery(), searchFields));
		return this;
	}

	StringPartBuilder buildSearch(List<SearchParameter> parameters, SearchBuilder searchBuilder) {
		for (SearchParameter searchParameter : parameters) {
			setSearchParameter(searchParameter.getParamName(), searchParameter.getValue(), searchParameter.getSearchType());
		}
		append(QueryProcessor.applySearch(root.isNativeQuery(), searchBuilder));
		return this;
	}

	private StringPartBuilder setSearchParameter(String name, String value, SearchType searchType) {
		if (!isStringEmpty(value)) {
			value = SearchType.getExpression(searchType, value);
		}
		return setParameter(name, value);
	}

	protected boolean isAlreadyBuilt() {
		return alreadyBuilt;
	}

}
