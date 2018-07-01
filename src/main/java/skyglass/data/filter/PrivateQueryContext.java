package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IValueResolver;
import skyglass.query.model.query.FilterGroup;
import skyglass.query.model.query.InternalUtil;
import skyglass.query.model.query.QueryFilter;
import skyglass.query.model.query.QueryUtil;
import skyglass.query.model.query.SelectField;
import skyglass.query.model.query.Sort;

public class PrivateQueryContext {
	
	private JunctionType junctionType;
	
    private PrivateFilterItemTree rootFilterItem;
    
    private IValueResolver valueResolver;
    
    private IJoinBuilder joinBuilder;
    
    private PrivatePathResolver pathResolver;
    
    private Class<?> rootClazz;
    
    private PrivateFieldResolverContext fieldResolverContext = new PrivateFieldResolverContext();
    
    private Map<String, PrivateQueryContext> subQueryContextMap = new HashMap<String, PrivateQueryContext>();
    
    private Collection<PrivateExpression> groupByList = new ArrayList<>();
    
    private Collection<SelectField> selectFields = new ArrayList<>();
    
    private Collection<QueryFilter> queryFilters = new ArrayList<>();
    
    private Collection<Sort> sorts = new ArrayList<>();
    
    private boolean distinct = false;
    
    public PrivateQueryContext(JunctionType junctionType, IValueResolver valueResolver,
    		IJoinBuilder joinBuilder, Class<?> rootClazz, IJoinType joinType) {
    	this.junctionType = junctionType;
        this.rootFilterItem = new PrivateFilterItemTree(JunctionType.toFilterType(junctionType));
        this.valueResolver = valueResolver;
        this.joinBuilder = joinBuilder;
        this.rootClazz = rootClazz;
        this.pathResolver = new PrivatePathResolver(joinBuilder, joinType);
    }
    
    public PrivateQueryContext(PrivateQueryContext parent, boolean isAnd) {
        this(isAnd ? JunctionType.AND : JunctionType.OR, parent.valueResolver, 
        		parent.joinBuilder, parent.rootClazz, parent.pathResolver.getJoinType());
    }
    
    public void addRootChild(PrivateFilterItem filterItem) {
    	rootFilterItem.addChild(filterItem);
    }
    
    public PrivateFilterItemTree getRootFilterItem() {
    	return rootFilterItem;
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, Object filterValue) {
        return createFilterItem(fieldName, FieldType.Path, filterValue);
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue) {
        return createFilterItem(fieldName, FilterType.Equals, filterValue);
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, FilterType filterType, Object filterValue) {
        return createFilterItem(fieldName, FieldType.Path, filterType, filterValue);
    }
    
    private PrivateFilterItem addFilter(String fieldName, FilterType filterType) {
        return addFilter(fieldName, filterType, null);
    }
    
    private PrivateFilterItem addFilter(String fieldName, FilterType filterType, Object filterValue) {
        return createFilterItem(fieldName, filterType, filterValue);
    }
    
    private PrivateFilterItem addFilters(FilterType filterType, PrivateFilterItem... filterItems) {
        PrivateFilterItemTree parent = createFilterItemTree(filterType);
        addRootChild(parent);
        for (PrivateFilterItem filterItem: filterItems) {
            parent.addChild(filterItem);
        }
        return parent;
    }
    
    private PrivateFilterItem addFilters(String fieldName, FilterType filterType, PrivateFilterItem... filterItems) {
        PrivateFilterItemTree parent = createFilterItemTree(fieldName, filterType);
        addRootChild(parent);
        for (PrivateFilterItem filterItem: filterItems) {
            parent.addChild(filterItem);
        }
        return parent;
    }
    
    public PrivateFilterItem addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType) {
        PrivateFilterItemTree orFilter = new PrivateFilterItemTree(FilterType.Or);
        addRootChild(orFilter);
        for (Object filterValue : filterValues) {
            PrivateFilterItem filterItem = createFilterItem(fieldName, fieldType, filterType, filterValue);
            orFilter.addChild(filterItem);
        }
        return orFilter;
    }
    
    private PrivateFilterItemTree createFilterItemTree(FilterType filterType) {
        return new PrivateFilterItemTree(filterType);
    }
    
    private PrivateFilterItemTree createFilterItemTree(String fieldName, 
    		FilterType filterType) {
        return new PrivateFilterItemTree(fieldResolverContext.addFieldResolver(
        		this, fieldName, FieldType.Path), filterType);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType,
    		FilterType filterType, Object filterValue) {
        return new DataFilterItem(valueResolver, rootClazz, 
        		fieldResolverContext.addFieldResolver(this, fieldName, fieldType), filterValue,
                filterType);
    }
    
    public String resolveAliasPath(String path, IJoinType joinType) {
    	return pathResolver.resolveAliasPath(path, joinType);
    }
    
    public String resolvePropertyPath(String path) {
    	return pathResolver.resolvePropertyPath(path);
    }
    
    public String resolvePropertyPath(String path, IJoinType joinType) {
    	return pathResolver.resolvePropertyPath(path, joinType);
    }
    
    public void setJoinType(IJoinType joinType) {
    	pathResolver.setJoinType(joinType);
    }
    
    public void addSubQueryContext(String path, PrivateQueryContext subQueryContext) {
    	subQueryContextMap.put(path, subQueryContext);
    }
    
    public void addGroupBy(String path) {
    	groupByList.add(new PrivateExpression(path));
    }
    
    public FieldResolver addFieldResolver(String fieldName) {
    	return fieldResolverContext.addFieldResolver(this, fieldName);
    }
    
    public FieldResolver addFieldResolver(String fieldName, FieldType fieldType) {
        return fieldResolverContext.addFieldResolver(this, fieldName, fieldType);
    }
    
    public FieldResolver addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver) {
        return fieldResolverContext.addCustomFieldResolver(this, fieldName, customFieldResolver);
    }

    public FieldResolver addFieldResolver(String fieldName, FieldType fieldType, String expression) {
        return fieldResolverContext.addFieldResolver(this, fieldName, fieldType, expression);
    }

    public FieldResolver addFieldResolvers(String fieldName, FieldType fieldType, String... expressions) {
        return fieldResolverContext.addFieldResolvers(this, fieldName, fieldType, expressions);
    }
    
    public void addFilter(QueryFilter filter) {
        queryFilters.add(filter);
    }

    public void addFilters(QueryFilter... filters) {
        if (filters != null) {
            for (QueryFilter filter : filters) {
                addFilter(filter);
            }
        }
    }
    
    private void addFilters(PrivateFilterItemTree parent, PrivateFilterItem... filterItems) {
        for (PrivateFilterItem filterItem : filterItems) {
            parent.addChild(filterItem);
        }
        addRootChild(parent);
    }

    public PrivateFilterItem all(String fieldName, PrivateFilterItem... filterItems) {
        return addFilters(fieldName, FilterType.All, filterItems);
    }

    public PrivateFilterItem and(PrivateFilterItem... filterItems) {
        return addFilters(FilterType.And, filterItems);
    }

    public PrivateFilterItem empty(String fieldName) {
        return addFilter(fieldName, FilterType.Empty);
    }

    public PrivateFilterItem equal(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.Equals, value);
    }

    public PrivateFilterItem greaterOrEqual(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.GreaterOrEquals, value);
    }

    public PrivateFilterItem greater(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.Greater, value);
    }

    public PrivateFilterItem like(String fieldName, String value) {
        return addFilter(fieldName, FilterType.Like, value);
    }

    public PrivateFilterItem in(String fieldName, Collection<?> values) {
        return addFilter(fieldName, FilterType.In, values);
    }

    public PrivateFilterItem in(String fieldName, Object... values) {
        return addFilter(fieldName, FilterType.In, values);
    }

    public PrivateFilterItem lessOrEqual(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.LessOrEquals, value);
    }

    public PrivateFilterItem less(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.Less, value);
    }

    public PrivateFilterItem none(String fieldName, PrivateFilterItem... filterItems) {
        return addFilters(fieldName, FilterType.None, filterItems);
    }

    public PrivateFilterItem not(PrivateFilterItem... filterItems) {
        return addFilters(FilterType.Not, filterItems);
    }

    public PrivateFilterItem notEqual(String fieldName, Object value) {
        return addFilter(fieldName, FilterType.NotEquals, value);
    }

    public PrivateFilterItem notIn(String fieldName, Collection<?> values) {
        return addFilter(fieldName, FilterType.NotIn, values);
    }

    public PrivateFilterItem notIn(String fieldName, Object... values) {
        return addFilter(fieldName, FilterType.NotIn, values);
    }

    public PrivateFilterItem notEmpty(String fieldName) {
        return addFilter(fieldName, FilterType.NotEmpty);
    }

    public PrivateFilterItem notNull(String fieldName) {
        return addFilter(fieldName, FilterType.IsNotNull);
    }

    public PrivateFilterItem isNull(String fieldName) {
        return addFilter(fieldName, FilterType.IsNull);
    }

    public PrivateFilterItem or(PrivateFilterItem... filterItems) {
        return addFilters(FilterType.Or, filterItems);
    }

    public PrivateFilterItem some(String fieldName, PrivateFilterItem... filterItems) {
        return addFilters(fieldName, FilterType.Some, filterItems);
    }
    
    public PrivateFilterItem range(String fieldName, Object minValue, Object maxValue) {
    	return and(greaterOrEqual(fieldName, minValue),
    			lessOrEqual(fieldName, maxValue));
    }

 

    public void setHavingType() {
        this.filterType = FilterGroup.Having;
    }

    public boolean isHavingType() {
        return this.filterType == FilterGroup.Having;
    }

    public void addSort(Sort sort) {
        if (sort == null)
            return;
        sorts.add(sort);
    }

    public void addSorts(Sort... sorts) {
        if (sorts != null) {
            for (Sort sort : sorts) {
                addSort(sort);
            }
        }
    }

    public void addSort(String property, boolean desc) {
        addSort(property, desc, false);
    }

    public void addSort(String property, boolean desc, boolean ignoreCase) {
        if (property == null)
            return; // null properties do nothing, don't bother to add them.
        addSort(new Sort(property, desc, ignoreCase));
    }

    public void addSortAsc(String property) {
        addSort(property, false, false);
    }

    public void addSortAsc(String property, boolean ignoreCase) {
        addSort(property, false, ignoreCase);
    }

    public void addSortDesc(String property) {
        addSort(property, true, false);
    }

    public void addSortDesc(String property, boolean ignoreCase) {
        addSort(property, true, ignoreCase);
    }
    
    public void addSelectFields(String... fieldNames) {
    	addSelectFields(SelectType.Property, fieldNames);
    }
    
    public void addSelectFields(SelectType selectOperator, String... fieldNames) {
    	for (String fieldName: fieldNames) {
    		addSelectField(fieldName, selectOperator);
    	}
    }
    
    public void addSelectField(String fieldName) {
    	addSelectField(fieldName, SelectType.Property);
    }
    
    public void addSelectField(String fieldName, SelectType selectOperator) {
    	fieldResolverContext.addFieldResolver(this, fieldName);
    	selectFields.add(new SelectField(fieldName, selectOperator));
    }
    
    public void addSubQueryExpression(String path, String subQueryPath, FilterType filterType,
    		PrivateQueryContext subQueryContext) {
    	createFilterItem(path, filterType, subQueryPath);
    	subQueryContextMap.put(path, subQueryContext);
    }
    
    public void setDistinct(boolean distinct) {
    	this.distinct = distinct;
    }
    
    public Class<?> getRootClazz() {
    	return rootClazz;
    }
    
    public boolean isDistinct() {
    	return distinct;
    }
    
    public boolean isDisjunction() {
    	return junctionType == JunctionType.AND;
    }
    
    public Collection<SelectField> getSelectFields() {
    	return selectFields;
    }
    
    public Collection<QueryFilter> getQueryFilters() {
    	return queryFilters;
    }
    
    /**
     * <ol>
     * <li>Check for injection attack in property strings.
     * <li>Check for values that are incongruent with the operator.
     * <li>Remove null filters from the list.
     * <li>Simplify out junctions (and/or) that have only one sub-filter.
     * <li>Remove filters that require sub-filters but have none
     * (and/or/not/some/all/none)
     * </ol>
     */
    private Collection<QueryFilter> checkAndCleanFilters(Collection<QueryFilter> filters) {
        return QueryUtil.walkFilters(filters, new QueryUtil.FilterVisitor() {
            @SuppressWarnings({ "rawtypes" })
			@Override
            public QueryFilter visitBefore(QueryFilter filter) {
                if (filter != null && filter.getValue() != null) {
                    if (filter.isTakesListOfSubFilters()) {
                        // make sure that filters that take lists of filters
                        // actually have lists of filters for their values
                        if (filter.getValue() instanceof List) {
                            for (Object o : (List) filter.getValue()) {
                                if (!(o instanceof QueryFilter)) {
                                    throw new IllegalArgumentException("The search has a filter (" + filter
                                            + ") for which the value should be a List of Filters but there is an element in the list that is of type: "
                                            + o.getClass());
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be a List of Filters but is not a list. The actual type is "
                                    + filter.getValue().getClass());
                        }
                    } else if (filter.isTakesSingleSubFilter()) {
                        // make sure filters that take filters actually have
                        // filters for their values
                        if (!(filter.getValue() instanceof QueryFilter)) {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be of type Filter but is of type: "
                                    + filter.getValue().getClass());
                        }
                    } else if (filter.isTakesListOfValues()) {
                        // make sure filters that take collections or arrays
                        // actually have collections or arrays for their values
                        if (!(filter.getValue() instanceof Collection) && !(filter.getValue() instanceof Object[])) {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be a collection or array but is of type: "
                                    + filter.getValue().getClass());
                        }
                    }
                }

                return filter;
            }

            @SuppressWarnings("unchecked")
            @Override
            public QueryFilter visitAfter(QueryFilter filter) {
                if (filter == null)
                    return null;

                if (!filter.isTakesNoProperty()) {
                    securityCheckProperty(filter.getProperty());
                }

                // Remove operators that take sub filters but have none
                // assigned. Replace conjunctions that only have a single
                // sub-filter with that sub-filter.
                if (filter.isTakesSingleSubFilter()) {
                    if (filter.getValue() == null) {
                        return null;
                    }
                } else if (filter.isTakesListOfSubFilters()) {
                    if (filter.getValue() == null) {
                        return null;
                    } else {
                        List<QueryFilter> list = (List<QueryFilter>) filter.getValue();
                        if (list.size() == 0) {
                            return null;
                        } else if (list.size() == 1) {
                            return list.get(0);
                        }
                    }
                }

                return filter;
            }
        }, true);
    }

}
