package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IValueResolver;
import skyglass.query.model.query.QueryUtil;
import skyglass.query.model.query.SelectField;
import skyglass.query.model.query.Sort;

public class PrivateQueryContext {
	
	private JunctionType junctionType;
	
    private PrivateCompositeFilterItem rootFilterItem;
    
    private IValueResolver valueResolver;
    
    private IJoinBuilder joinBuilder;
    
    private PrivatePathResolver pathResolver;
    
    private Class<?> rootClazz;
    
    private PrivateFieldResolverContext fieldResolverContext = new PrivateFieldResolverContext();
    
    private Map<String, PrivateQueryContext> subQueryContextMap = new HashMap<String, PrivateQueryContext>();
    
    private Collection<PrivateExpression> groupByList = new ArrayList<>();
    
    private Collection<SelectField> selectFields = new ArrayList<>();
    
    private Collection<Sort> sorts = new ArrayList<>();
    
    private boolean distinct = false;
    
    public PrivateQueryContext(JunctionType junctionType, IValueResolver valueResolver,
    		IJoinBuilder joinBuilder, Class<?> rootClazz, IJoinType joinType) {
    	this.junctionType = junctionType;
        this.rootFilterItem = new PrivateCompositeFilterItem(JunctionType.toFilterType(junctionType));
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

    public void all(String fieldName, FilterItem... filterItems) {
        addFilters(fieldName, FilterType.All, createPrivateFilterItems(filterItems));
    }

    public void and(FilterItem... filterItems) {
        addFilters(FilterType.And, createPrivateFilterItems(filterItems));
    }
    
    public void empty(String fieldName) {
       addFilter(fieldName, FilterType.Empty);
    }

    public void equal(String fieldName, Object value) {
        addFilter(fieldName, FilterType.Equals, value);
    }

    public void greaterOrEqual(String fieldName, Object value) {
        addFilter(fieldName, FilterType.GreaterOrEquals, value);
    }

    public void greater(String fieldName, Object value) {
        addFilter(fieldName, FilterType.Greater, value);
    }

    public void like(String fieldName, String value) {
        addFilter(fieldName, FilterType.Like, value);
    }

    public void in(String fieldName, Collection<?> values) {
        addFilter(fieldName, FilterType.In, values);
    }

    public void in(String fieldName, Object... values) {
        addFilter(fieldName, FilterType.In, values);
    }

    public void lessOrEqual(String fieldName, Object value) {
        addFilter(fieldName, FilterType.LessOrEquals, value);
    }

    public void less(String fieldName, Object value) {
        addFilter(fieldName, FilterType.Less, value);
    }

    public void none(String fieldName, FilterItem... filterItems) {
        addFilters(fieldName, FilterType.None, createPrivateFilterItems(filterItems));
    }

    public void not(FilterItem... filterItems) {
        addFilters(FilterType.Not, createPrivateFilterItems(filterItems));
    }

    public void notEqual(String fieldName, Object value) {
        addFilter(fieldName, FilterType.NotEquals, value);
    }

    public void notIn(String fieldName, Collection<?> values) {
        addFilter(fieldName, FilterType.NotIn, values);
    }

    public void notIn(String fieldName, Object... values) {
        addFilter(fieldName, FilterType.NotIn, values);
    }

    public void notEmpty(String fieldName) {
        addFilter(fieldName, FilterType.NotEmpty);
    }

    public void notNull(String fieldName) {
        addFilter(fieldName, FilterType.IsNotNull);
    }

    public void isNull(String fieldName) {
        addFilter(fieldName, FilterType.IsNull);
    }

    public void or(FilterItem... filterItems) {
        addFilters(FilterType.Or, createPrivateFilterItems(filterItems));
    }

    public void some(String fieldName, FilterItem... filterItems) {
        addFilters(fieldName, FilterType.Some, createPrivateFilterItems(filterItems));
    }
    
    public void range(String fieldName, Object minValue, Object maxValue) {
    	addFilters(FilterType.And, addFilter(fieldName, FilterType.GreaterOrEquals, minValue),
    			addFilter(fieldName, FilterType.LessOrEquals, maxValue));
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
    
    public String registerParam(Supplier<Object> valueResolver) {
    	return pathResolver.registerParam(valueResolver);
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
    
}
