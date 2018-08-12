package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import skyglass.data.query.QueryFilter;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.ITypeResolver;
import skyglass.query.model.query.QueryUtil;
import skyglass.query.model.query.SelectField;

public class PrivateQueryContext {
	
	private JunctionType junctionType;
	
    private PrivateCompositeFilterItem rootFilterItem;
    
    private ITypeResolver typeResolver;
    
    private PrivatePathResolver pathResolver;
    
    private Class<?> rootClazz;
    
    private PrivateFieldResolverContext fieldResolverContext = new PrivateFieldResolverContext();
    
    private Map<String, PrivateQueryContext> subQueryContextMap = new HashMap<String, PrivateQueryContext>();
    
    private Collection<PrivateExpression> groupByList = new ArrayList<>();
    
    private List<OrderField> orderFields = new ArrayList<OrderField>();
    
    private List<SelectField> selectFields = new ArrayList<>();
    
    private Collection<String> fetches = new ArrayList<>();
    
    private boolean distinct = false;
    
    private PrivateQueryContext parentContext;
    
    public PrivateQueryContext(JunctionType junctionType, ITypeResolver typeResolver,
    		Class<?> rootClazz, IJoinType joinType) {
    	this.junctionType = junctionType;
        this.rootFilterItem = new PrivateCompositeFilterItem(JunctionType.toFilterType(junctionType));
        this.typeResolver = typeResolver;
        this.rootClazz = rootClazz;
        this.pathResolver = new PrivatePathResolver(rootClazz, typeResolver, joinType);
    }
    
    public PrivateQueryContext(PrivateQueryContext parentContext, 
    		PrivateCompositeFilterItem rootFilterItem) {
    	this.parentContext = parentContext;
    	this.junctionType = JunctionType.fromFilterType(rootFilterItem.getFilterType());
        this.rootFilterItem = rootFilterItem;
        this.typeResolver = parentContext.typeResolver;
        this.rootClazz = rootFilterItem.getRootClass();
        this.pathResolver = new PrivatePathResolver(rootClazz, parentContext.pathResolver);
    }
    
    public PrivateQueryContext(PrivateQueryContext parent, boolean isAnd) {
        this(isAnd ? JunctionType.AND : JunctionType.OR, parent.typeResolver, 
        		parent.rootClazz, parent.pathResolver.getJoinType());
    }
    
    public void addRootChild(PrivateFilterItem filterItem) {
    	rootFilterItem.addChild(filterItem);
    }
    
    public PrivateCompositeFilterItem getRootFilterItem() {
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
        PrivateCompositeFilterItem parent = createFilterItemTree(filterType);
        addRootChild(parent);
        for (PrivateFilterItem filterItem: filterItems) {
            parent.addChild(filterItem);
        }
        return parent;
    }
    
    private PrivateFilterItem addFilters(String fieldName, FilterType filterType, PrivateFilterItem... filterItems) {
        PrivateCompositeFilterItem parent = createFilterItemTree(fieldName, filterType);
        addRootChild(parent);
        for (PrivateFilterItem filterItem: filterItems) {
            parent.addChild(filterItem);
        }
        return parent;
    }
    
    public PrivateFilterItem addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType) {
        PrivateCompositeFilterItem orFilter = new PrivateCompositeFilterItem(FilterType.Or);
        addRootChild(orFilter);
        for (Object filterValue : filterValues) {
            PrivateFilterItem filterItem = createFilterItem(fieldName, fieldType, filterType, filterValue);
            orFilter.addChild(filterItem);
        }
        return orFilter;
    }
    
    private PrivateCompositeFilterItem createFilterItemTree(FilterType filterType) {
        return new PrivateCompositeFilterItem(filterType);
    }
    
    private PrivateCompositeFilterItem createFilterItemTree(String fieldName, 
    		FilterType filterType) {
        return new PrivateCompositeFilterItem(fieldResolverContext.addFieldResolver(
        		this, fieldName, FieldType.Path), filterType);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType,
    		FilterType filterType, Object filterValue) {
        return new DataFilterItem(typeResolver, rootClazz, 
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
    
    public void addFilter(CompositeFilterItem parent, FilterItem filterItem) {
        addFilter((PrivateCompositeFilterItem)createPrivateFilterItem(parent), 
        		createPrivateFilterItem(filterItem));
    }
    
    public void addFilters(FilterItem... filterItems) {
        addFilters(createPrivateFilterItems(filterItems));
    }
    
    public void addFilters(CompositeFilterItem parent, FilterItem... filterItems) {
        addFilters((PrivateCompositeFilterItem)createPrivateFilterItem(parent), 
        		createPrivateFilterItems(filterItems));
    }

    public PrivateFilterItem all(String fieldName, FilterItem... filterItems) {
        return addFilters(fieldName, FilterType.All, createPrivateFilterItems(filterItems));
    }

    public PrivateFilterItem and(FilterItem... filterItems) {
        return addFilters(FilterType.And, createPrivateFilterItems(filterItems));
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

    public PrivateFilterItem none(String fieldName, FilterItem... filterItems) {
        return addFilters(fieldName, FilterType.None, createPrivateFilterItems(filterItems));
    }

    public PrivateFilterItem not(FilterItem... filterItems) {
        return addFilters(FilterType.Not, createPrivateFilterItems(filterItems));
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

    public PrivateFilterItem or(FilterItem... filterItems) {
        return addFilters(FilterType.Or, createPrivateFilterItems(filterItems));
    }

    public PrivateFilterItem some(String fieldName, FilterItem... filterItems) {
        return addFilters(fieldName, FilterType.Some, createPrivateFilterItems(filterItems));
    }
    
    public PrivateFilterItem range(String fieldName, Object minValue, Object maxValue) {
    	return addFilters(FilterType.And, addFilter(fieldName, FilterType.GreaterOrEquals, minValue),
    			addFilter(fieldName, FilterType.LessOrEquals, maxValue));
    }
    
    public PrivateFilterItem negate(FilterItem filterItem) {
        return addFilters(FilterType.Not, addExplicitNullChecks(createPrivateFilterItem(filterItem)));
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
    
    public void addFetches(String... fieldNames) {
    	for (String fieldName: fieldNames) {
    		addFetch(fieldName);
    	}
    }
    
    public void addFetch(String fieldName) {
    	fieldResolverContext.addFieldResolver(this, fieldName);
    	fetches.add(fieldName);
    }
    
    public String getRootAlias() {
    	return pathResolver.getRootAlias();
    }
    
    public AliasNode getRootNode() {
    	return pathResolver.getRootNode();
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
    
    public List<SelectField> getSelectFields() {
    	return selectFields;
    }
    
    public Collection<String> getFetches() {
    	return fetches;
    }
    
    public String registerParam(String path, Supplier<Object> valueResolver) {
    	return pathResolver.registerParam(path, valueResolver);
    }
    
    public String getPathRef(String path) {
    	return pathResolver.getPathRef(path);
    }
    
    public List<Object> getParams() {
    	return pathResolver.getParams();
    }
    
    public List<OrderField> getOrderFields() {
        return orderFields;
    }
    
    public void addOrder(String orderField, OrderType orderType) {
        OrderField order = new OrderField(addFieldResolver(orderField, null), orderType);
        this.orderFields.add(order);
    }

    public void setOrder(String orderField, OrderType orderType) {
        this.orderFields.clear();
        addOrder(orderField, orderType);
    }

    public void setDefaultOrder(String orderField, OrderType orderType) {
        if (this.orderFields.size() == 0) {
            setOrder(orderField, orderType);
        }
    }
    
    private PrivateFilterItem createPrivateFilterItem(FilterItem customFilterItem) {
    	if (customFilterItem instanceof CompositeFilterItem) {
    		CompositeFilterItem customCompositeFilterItem = (CompositeFilterItem)customFilterItem;
            PrivateCompositeFilterItem compositeFilterItem = new PrivateCompositeFilterItem(
            		customCompositeFilterItem.getFilterType());
            for (FilterItem customFilterItemChild : customCompositeFilterItem.getChildren()) {
                compositeFilterItem.addChild(createPrivateFilterItem(customFilterItemChild));
            }
            return compositeFilterItem;    		
    	}
        return createFilterItem(
        		customFilterItem.getFieldName(), customFilterItem.getFieldType(),
        		customFilterItem.getFilterType(), customFilterItem.getFilterValue());
    }

    private PrivateFilterItem[] createPrivateFilterItems(FilterItem... filterItems) {
        PrivateFilterItem[] result = new PrivateFilterItem[filterItems.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createPrivateFilterItem(filterItems[i]);
        }
        return result;
    }
    
    private void addFilter(PrivateCompositeFilterItem parent, PrivateFilterItem filterItem) {
        parent.addChild(filterItem);
        addRootChild(parent);
    }

    private void addFilters(PrivateFilterItem... filterItems) {
        for (PrivateFilterItem filterItem : filterItems) {
            addFilter(filterItem);
        }
    }
    
    private void addFilter(PrivateFilterItem filterItem) {
    	addRootChild(filterItem);
    }

    private void addFilters(PrivateCompositeFilterItem parent, PrivateFilterItem... filterItems) {
        for (PrivateFilterItem filterItem : filterItems) {
            parent.addChild(filterItem);
        }
        addRootChild(parent);
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
    public PrivateCompositeFilterItem checkAndCleanFilters() {
        QueryUtil.walkFilters(rootFilterItem.getChildren(), new QueryUtil.FilterVisitor() {
            @Override
            public PrivateFilterItem visitBefore(PrivateFilterItem filterItem) {
                if (filterItem != null && filterItem.hasNotNullValue()) {
                    if (filterItem.isTakesListOfSubFilters()) {
                    	PrivateCompositeFilterItem compositeFilterItem = (PrivateCompositeFilterItem)filterItem;
                        // make sure that filters that take lists of filters
                        // actually have lists of filters for their values
                        if (compositeFilterItem.getChildren().size() == 0) {
                            throw new IllegalArgumentException("The query has a composite filter (" + filterItem
                                    + ") which should have a List of Filters but the list is empty");
                        }
                    } else if (filterItem.isTakesListOfValues()) {
                        // make sure filters that take collections or arrays
                        // actually have collections or arrays for their values
                        if (!filterItem.hasCollectionValue()) {
                            throw new IllegalArgumentException("The query has a filter (" + filterItem
                                    + ") for which the value should be a collection or array but is of type: "
                                    + filterItem.getValueClass());
                        }
                    }
                }

                return filterItem;
            }

            @Override
            public PrivateFilterItem visitAfter(PrivateFilterItem filterItem) {
                if (filterItem == null)
                    return null;

                // Remove operators that take sub filters but have none
                // assigned.
                if (filterItem.isTakesListOfSubFilters()) {
                	PrivateCompositeFilterItem compositeFilterItem = (PrivateCompositeFilterItem)filterItem;
                    if (compositeFilterItem.getChildren().size() == 0) {
                        return null; 
                    }
                }

                return filterItem;
            }
        }, true);
        
        return rootFilterItem;
    }
    
    /**
     * Used by {@link #negate(QueryFilter)}. There's a complication with null
     * values in the database so that !(x == 1) is not the opposite of (x == 1).
     * Rather !(x == 1 and x != null) is the same as (x == 1). This method
     * applies the null check explicitly to all filters included in the given
     * filter tree.
     */
    protected PrivateFilterItem addExplicitNullChecks(PrivateFilterItem filterItem) {
        return QueryUtil.walkFilter(filterItem, new QueryUtil.FilterVisitor() {
            @Override
            public PrivateFilterItem visitAfter(PrivateFilterItem filterItem) {
                if (filterItem.isTakesSingleValue() || filterItem.isTakesListOfValues()) {
                    return addFilters(FilterType.And, filterItem, notNull(filterItem.getFieldResolver().getFieldName()));
                } else {
                    return filterItem;
                }
            }
        }, false);

    }
    
}
