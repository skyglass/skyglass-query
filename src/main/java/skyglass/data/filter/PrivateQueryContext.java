package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IValueResolver;
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
        this.rootFilterItem = new PrivateFilterItemTree(junctionType);
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

    public void addFilterAll(String property, QueryFilter filter) {
        addFilter(QueryFilter.all(property, filter));
    }

    public void addFilterAnd(QueryFilter... filters) {
        addFilter(QueryFilter.and(filters));
    }

    public void addFilterEmpty(String property) {
        addFilter(QueryFilter.isEmpty(property));
    }

    public void addFilterEqual(String property, Object value) {
        addFilter(QueryFilter.equal(property, value));
    }

    public void addFilterGreaterOrEqual(String property, Object value) {
        addFilter(QueryFilter.greaterOrEqual(property, value));
    }

    public void addFilterGreaterThan(String property, Object value) {
        addFilter(QueryFilter.greaterThan(property, value));
    }

    public void addFilterILike(String property, String value) {
        addFilter(QueryFilter.ilike(property, value));
    }

    public void addFilterIn(String property, Collection<?> value) {
        addFilter(QueryFilter.in(property, value));
    }

    public void addFilterIn(String property, Object... value) {
        addFilter(QueryFilter.in(property, value));
    }

    public void addFilterLessOrEqual(String property, Object value) {
        addFilter(QueryFilter.lessOrEqual(property, value));
    }

    public void addFilterLessThan(String property, Object value) {
        addFilter(QueryFilter.lessThan(property, value));
    }

    public void addFilterLike(String property, String value) {
        addFilter(QueryFilter.like(property, value));
    }

    public void addFilterNone(String property, QueryFilter filter) {
        addFilter(QueryFilter.none(property, filter));
    }

    public void addFilterNot(QueryFilter filter) {
        addFilter(QueryFilter.not(filter));
    }

    public void addFilterNotEqual(String property, Object value) {
        addFilter(QueryFilter.notEqual(property, value));
    }

    public void addFilterNotIn(String property, Collection<?> value) {
        addFilter(QueryFilter.notIn(property, value));
    }

    public void addFilterNotIn(String property, Object... value) {
        addFilter(QueryFilter.notIn(property, value));
    }

    public void addFilterNotEmpty(String property) {
        addFilter(QueryFilter.isNotEmpty(property));
    }

    public void addFilterNotNull(String property) {
        addFilter(QueryFilter.isNotNull(property));
    }

    public void addFilterNull(String property) {
        addFilter(QueryFilter.isNull(property));
    }

    public void addFilterOr(QueryFilter... filters) {
        addFilter(QueryFilter.or(filters));
    }

    public void addFilterSome(String property, QueryFilter filter) {
        addFilter(QueryFilter.some(property, filter));
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
