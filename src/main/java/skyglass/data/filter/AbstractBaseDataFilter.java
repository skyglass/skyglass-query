package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import skyglass.data.filter.request.IFilterRequest;
import skyglass.data.query.QueryResult;

public abstract class AbstractBaseDataFilter<T, F> implements IBaseDataFilter<T, F> {

    protected abstract void initBeforeResult();

    protected abstract Iterable<T> getFullResult();	
	
    protected abstract long getRowCount();

    protected abstract QueryResult<T> getPagedResult();

    protected abstract List<T> getUnpagedResult();

    protected abstract void applyOrder(List<OrderField> orderFields);

    protected abstract void resolveCustomFilter(CustomFilterResolver filterResolver);

    protected abstract PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue,
            FilterType filterType, FilterClass filterClass);
    
    private int rowsPerPage = 10;
    private int pageNumber = 1;
    private List<OrderField> orderFields = new ArrayList<OrderField>();
    private Map<String, FieldResolver> fieldResolverMap = new HashMap<String, FieldResolver>();

    private List<CustomFilterResolver> customFilterResolvers = new ArrayList<CustomFilterResolver>();

    protected PrivateFilterItemTree rootFilterItem;

    private Map<String, List<FieldResolver>> searchMap = new HashMap<String, List<FieldResolver>>();

    private IFilterRequest request;

    protected AbstractBaseDataFilter(JunctionType junctionType, IFilterRequest request) {
        this.rootFilterItem = new PrivateFilterItemTree(junctionType);
        this.request = request;
    }
    
    protected Object parseExpression(Object object, String expression) {
        ExpressionParser parser = new SpelExpressionParser();
        Expression exp = parser.parseExpression(expression);
        try {
            return exp.getValue(object);
        }
        catch (SpelEvaluationException e) {
            return null;
        }
    }

    protected boolean returnEmptyResult() {
        return false;
    }

    protected int getRowsPerPage() {
        return rowsPerPage;
    }

    protected int getPageNumber() {
        return pageNumber;
    }

    protected List<OrderField> getOrderFields() {
        return orderFields;
    }

    @SuppressWarnings("unchecked")
    public F setPaging(Integer rowsPerPage, Integer pageNumber) {
        this.rowsPerPage = rowsPerPage;
        this.pageNumber = pageNumber;
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    public F addOrder(String orderField, OrderType orderType) {
        OrderField order = new OrderField(getFieldResolver(orderField, null), orderType);
        this.orderFields.add(order);
        return (F) this;
    }

    public F setOrder(String orderField, OrderType orderType) {
        this.orderFields.clear();
        return addOrder(orderField, orderType);
    }

    protected FieldResolver getFieldResolver(String fieldName, FieldType fieldType) {
        FieldResolver fieldResolver = fieldResolverMap.get(fieldName);
        if (fieldResolver == null) {
            fieldResolver = new FieldResolver(this, fieldName, fieldType);
            fieldResolverMap.put(fieldName, fieldResolver);
        }
        if (fieldResolver.getFieldType() == null && fieldType != null) {
            fieldResolver.setFieldType(fieldType);
        }
        return fieldResolver;
    }

    @SuppressWarnings("unchecked")
    public F setDefaultOrder(String orderField, OrderType orderType) {
        if (this.orderFields.size() == 0) {
            setOrder(orderField, orderType);
        }
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    protected QueryResult<T> getEmptyResult() {
        QueryResult<T> result = new QueryResult<T>();
        result.setTotalRecords(0);
        result.setResults((List<T>) Collections.emptyList());
        return result;
    }

    @SuppressWarnings("unchecked")
    protected F addSearch(Map<String, List<FieldResolver>> searchMap, final String filterValue, FieldType fieldType,
            final String... fieldNames) {
        if (fieldNames.length > 0) {
            List<FieldResolver> searchList = searchMap.get(filterValue);
            if (searchList == null) {
                searchList = new ArrayList<FieldResolver>();
                searchMap.put(filterValue, searchList);
            }
            for (String fieldName : fieldNames) {
                searchList.add(getFieldResolver(fieldName, fieldType));
            }
        }
        return (F) this;
    }    

    protected void resolveCustomFilters() {
        for (CustomFilterResolver customFilterResolver : customFilterResolvers) {
            resolveCustomFilter(customFilterResolver);
        }
    }

    @SuppressWarnings("unchecked")
    public F addCustomFilterResolver(CustomFilterResolver customFilterResolver) {
        customFilterResolvers.add(customFilterResolver);
        return (F) this;
    }

    public QueryResult<T> getResult() {
        if (returnEmptyResult()) {
            return getEmptyResult();
        }
        initBeforeResult();
        return getPagedResult();
    }

    public List<T> getUnpagedList() {
        if (returnEmptyResult()) {
            return getEmptyResult().getResults();
        }
        initBeforeResult();
        return getUnpagedResult();
    }

    public long getResultCount() {
        if (returnEmptyResult()) {
            return 0;
        }
        initBeforeResult();
        return getRowCount();
    }

    protected boolean doApplyOrder() {
        List<OrderField> orderFields = getOrderFields();
        if (orderFields.size() > 0) {
            applyOrder(orderFields);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public F addRequestSearch(String... fieldNames) {
        String searchQuery = request.getSearchQuery();
        if (StringUtils.isNotBlank(searchQuery)) {
            addSearch(searchQuery, request.filterSearchFields(fieldNames));
        }
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    public F addSearch(final String filterValue, final String... fieldNames) {
        addSearch(searchMap, filterValue, FieldType.Path, fieldNames);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addSearch(final String filterValue, FieldType fieldType, final String... fieldNames) {
        addSearch(searchMap, filterValue, fieldType, fieldNames);
        return (F) this;
    }

    protected void initSearch() {
        if (!searchMap.isEmpty()) {
            PrivateFilterItemTree orFilter = new PrivateFilterItemTree(JunctionType.OR);
            rootFilterItem.addChild(orFilter);
            for (String fieldValue : searchMap.keySet()) {
                List<FieldResolver> fieldResolvers = searchMap.get(fieldValue);
                for (FieldResolver fieldResolver : fieldResolvers) {
                    PrivateFilterItem filterItem = createFilterItem(fieldResolver.getFieldName(),
                            fieldResolver.getFieldType(), fieldValue, FilterType.LIKE, FilterClass.STRING);
                    orFilter.addChild(filterItem);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver) {
        FieldResolver fieldResolver = getFieldResolver(fieldName, FieldType.Criteria);
        fieldResolver.setCustomFieldResolver(customFieldResolver);
        addFilter(fieldName, null);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFieldResolver(String fieldName, FieldType fieldType, String expression) {
        FieldResolver fieldResolver = getFieldResolver(fieldName, fieldType);
        fieldResolver.addResolvers(expression);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFieldResolvers(String fieldName, FieldType fieldType, String... expressions) {
        FieldResolver fieldResolver = getFieldResolver(fieldName, fieldType);
        fieldResolver.addResolvers(expressions);
        return (F) this;
    }

    @Override
    public F addFilter(String fieldName, Object filterValue) {
        return addFilter(fieldName, FieldType.Path, filterValue);
    }

    @Override
    public F addFilter(String fieldName, Object filterValue, FilterType filterType) {
        return addFilter(fieldName, FieldType.Path, filterValue, filterType);
    }

    @Override
    public F addFilter(String fieldName, Object filterValue, FilterClass filterClass) {
        return addFilter(fieldName, FieldType.Path, filterValue, filterClass);
    }

    @Override
    public F addFilter(String fieldName, Object filterValue, FilterType filterType, FilterClass filterClass) {
        return addFilter(fieldName, FieldType.Path, filterValue, filterType, filterClass);
    }

    @Override
    public F addFilters(String fieldName, Object[] filterValues) {
        return addFilters(fieldName, FieldType.Path, filterValues);
    }

    @Override
    public F addFilters(String fieldName, Object[] filterValues, FilterType filterType) {
        return addFilters(fieldName, FieldType.Path, filterValues, filterType);
    }

    @Override
    public F addFilters(String fieldName, Object[] filterValues, FilterClass filterClass) {
        return addFilters(fieldName, FieldType.Path, filterValues, filterClass);
    }

    @Override
    public F addFilters(String fieldName, Object[] filterValues, FilterType filterType, FilterClass filterClass) {
        return addFilters(fieldName, FieldType.Path, filterValues, filterType, filterClass);
    }

    @Override
    public F addFilter(String fieldName, FieldType fieldType, Object filterValue) {
        return addFilter(new FilterItem(fieldName, fieldType, filterValue));
    }

    @Override
    public F addFilter(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType) {
        return addFilter(new FilterItem(fieldName, fieldType, filterValue, filterType));
    }

    @Override
    public F addFilter(String fieldName, FieldType fieldType, Object filterValue, FilterClass filterClass) {
        return addFilter(new FilterItem(fieldName, fieldType, filterValue, filterClass));
    }

    @Override
    public F addFilter(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType,
            FilterClass filterClass) {
        return addFilter(new FilterItem(fieldName, fieldType, filterValue, filterType, filterClass));
    }

    @Override
    public F addFilter(FilterItem filterItem) {
        if (filterItem instanceof FilterItemTree) {
            return addFilter(createFilterItemTree((FilterItemTree) filterItem));
        } else {
            return addFilter(createFilterItem(filterItem));
        }
    }

    @Override
    public F addOrFilters(FilterItem... filterItems) {
        return addOrFilters(createFilterItems(filterItems));
    }

    @Override
    public F addAndFilters(FilterItem... filterItems) {
        return addAndFilters(createFilterItems(filterItems));
    }

    @Override
    public F addFilter(FilterItemTree parent, FilterItem filterItem) {
        return addFilter(createFilterItemTree(parent), createFilterItem(filterItem));
    }

    @Override
    public F addFilters(FilterItem... filterItems) {
        return addFilters(createFilterItems(filterItems));
    }

    @Override
    public F addFilters(FilterItemTree parent, FilterItem... filterItems) {
        return addFilters(createFilterItemTree(parent), createFilterItems(filterItems));
    }

    private PrivateFilterItemTree createFilterItemTree(FilterItemTree customFilterItemTree) {
        PrivateFilterItemTree filterItemTree = new PrivateFilterItemTree(customFilterItemTree.getJunctionType());
        for (FilterItem customFilterItem : customFilterItemTree.getChildren()) {
            if (customFilterItem instanceof FilterItemTree) {
                filterItemTree.addChild(createFilterItemTree((FilterItemTree) customFilterItem));
            } else {
                filterItemTree.addChild(createFilterItem(customFilterItem));
            }
        }
        return filterItemTree;
    }

    private PrivateFilterItem createFilterItem(FilterItem customFilterItem) {
        return createFilterItem(customFilterItem.getFieldName(), customFilterItem.getFieldType(),
                customFilterItem.getFilterValue(), customFilterItem.getFilterType(), customFilterItem.getFilterClass());
    }

    private PrivateFilterItem[] createFilterItems(FilterItem... filterItems) {
        PrivateFilterItem[] result = new PrivateFilterItem[filterItems.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = createFilterItem(filterItems[i]);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues) {
        addFilters(fieldName, fieldType, filterValues, FilterType.EQ, FilterClass.STRING);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType) {
        addFilters(fieldName, fieldType, filterValues, filterType, FilterClass.STRING);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterClass filterClass) {
        addFilters(fieldName, fieldType, filterValues, FilterType.EQ, filterClass);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType,
            FilterClass filterClass) {
        PrivateFilterItemTree orFilter = new PrivateFilterItemTree(JunctionType.OR);
        rootFilterItem.addChild(orFilter);
        for (Object filterValue : filterValues) {
            PrivateFilterItem filterItem = createFilterItem(fieldName, fieldType, filterValue, filterType, filterClass);
            orFilter.addChild(filterItem);
        }
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    private F addFilter(PrivateFilterItem filterItem) {
        rootFilterItem.addChild(filterItem);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    private F addFilter(PrivateFilterItemTree parent, PrivateFilterItem filterItem) {
        parent.addChild(filterItem);
        rootFilterItem.addChild(parent);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    private F addFilter(PrivateFilterItemTree parent) {
        rootFilterItem.addChild(parent);
        return (F) this;
    }

    @SuppressWarnings("unchecked")
    private F addFilters(PrivateFilterItem... filterItems) {
        for (PrivateFilterItem filterItem : filterItems) {
            addFilter(filterItem);
        }
        return (F) this;
    }

    private F addOrFilters(PrivateFilterItem... filterItems) {
        return addFilters(new PrivateFilterItemTree(JunctionType.OR), filterItems);
    }

    private F addAndFilters(PrivateFilterItem... filterItems) {
        return addFilters(new PrivateFilterItemTree(JunctionType.AND), filterItems);
    }

    @SuppressWarnings("unchecked")
    private F addFilters(PrivateFilterItemTree parent, PrivateFilterItem... filterItems) {
        for (PrivateFilterItem filterItem : filterItems) {
            parent.addChild(filterItem);
        }
        rootFilterItem.addChild(parent);
        return (F) this;
    }

    public F addFieldResolver(String fieldName, String expression) {
        return addFieldResolver(fieldName, FieldType.Path, expression);
    }

    public F addFieldResolvers(String fieldName, String... expressions) {
        return addFieldResolvers(fieldName, FieldType.Path, expressions);
    }

}
