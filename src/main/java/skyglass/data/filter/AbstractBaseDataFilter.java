package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
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
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.query.Sort;

public abstract class AbstractBaseDataFilter<T, F> implements IBaseDataFilter<T, F> {
	
	protected abstract F self();	

    protected abstract void initBeforeResult();

    protected abstract Iterable<T> getFullResult();	
	
    protected abstract long getRowCount();

    protected abstract QueryResult<T> getPagedResult();

    protected abstract List<T> getUnpagedResult();

    protected abstract void applyOrder(List<OrderField> orderFields);

    protected abstract void resolveCustomFilter(CustomFilterResolver filterResolver);
    
    private int rowsPerPage = 10;
    private int pageNumber = 1;
    private List<OrderField> orderFields = new ArrayList<OrderField>();
    private List<CustomFilterResolver> customFilterResolvers = new ArrayList<CustomFilterResolver>();
    
    protected IQueryBuilder<T, T> queryBuilder;
    
    protected PrivateQueryContext queryContext;

    private Map<String, List<FieldResolver>> searchMap = new HashMap<String, List<FieldResolver>>();

    private IFilterRequest request;
    
    protected Class<T> rootClazz;

    protected AbstractBaseDataFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType, 
    		IFilterRequest request, IQueryBuilder<T, T> queryBuilder) {
        this.rootClazz = rootClazz;
        this.queryBuilder = queryBuilder;
        this.queryContext = this.queryBuilder.setPrivateQueryContext(junctionType, rootClazz, joinType);
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

    @Override
    public F setPaging(Integer rowsPerPage, Integer pageNumber) {
        this.rowsPerPage = rowsPerPage;
        this.pageNumber = pageNumber;
        return self();
    }

    @Override
    public F addOrder(String orderField, OrderType orderType) {
        OrderField order = new OrderField(queryContext.addFieldResolver(orderField, null), orderType);
        this.orderFields.add(order);
        return self();
    }

    @Override
    public F setOrder(String orderField, OrderType orderType) {
        this.orderFields.clear();
        return addOrder(orderField, orderType);
    }

    @Override
    public F setDefaultOrder(String orderField, OrderType orderType) {
        if (this.orderFields.size() == 0) {
            setOrder(orderField, orderType);
        }
        return self();
    }

    @SuppressWarnings("unchecked")
    protected QueryResult<T> getEmptyResult() {
        QueryResult<T> result = new QueryResult<T>();
        result.setTotalRecords(0);
        result.setResults((List<T>) Collections.emptyList());
        return result;
    }

    protected F addSearch(Map<String, List<FieldResolver>> searchMap, final String filterValue, FieldType fieldType,
            final String... fieldNames) {
        if (fieldNames.length > 0) {
            List<FieldResolver> searchList = searchMap.get(filterValue);
            if (searchList == null) {
                searchList = new ArrayList<FieldResolver>();
                searchMap.put(filterValue, searchList);
            }
            for (String fieldName : fieldNames) {
                searchList.add(queryContext.addFieldResolver(fieldName, fieldType));
            }
        }
        return self();
    }    

    protected void resolveCustomFilters() {
        for (CustomFilterResolver customFilterResolver : customFilterResolvers) {
            resolveCustomFilter(customFilterResolver);
        }
    }

    @Override
    public F addCustomFilterResolver(CustomFilterResolver customFilterResolver) {
        customFilterResolvers.add(customFilterResolver);
        return self();
    }

    @Override
    public QueryResult<T> getResult() {
        if (returnEmptyResult()) {
            return getEmptyResult();
        }
        initBeforeResult();
        return getPagedResult();
    }

    @Override
    public List<T> getUnpagedList() {
        if (returnEmptyResult()) {
            return getEmptyResult().getResults();
        }
        initBeforeResult();
        return getUnpagedResult();
    }

    @Override
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

    @Override
    public F addRequestSearch(String... fieldNames) {
        String searchQuery = request.getSearchQuery();
        if (StringUtils.isNotBlank(searchQuery)) {
            addSearch(searchQuery, request.filterSearchFields(fieldNames));
        }
        return self();
    }

    public F addSearch(final String filterValue, final String... fieldNames) {
        addSearch(searchMap, filterValue, FieldType.Path, fieldNames);
        return self();
    }

    @Override
    public F addSearch(final String filterValue, FieldType fieldType, final String... fieldNames) {
        addSearch(searchMap, filterValue, fieldType, fieldNames);
        return self();
    }

    protected void initSearch() {
        if (!searchMap.isEmpty()) {
            PrivateCompositeFilterItem orFilter = new PrivateCompositeFilterItem(FilterType.Or);
            queryContext.addRootChild(orFilter);
            for (String fieldValue : searchMap.keySet()) {
                List<FieldResolver> fieldResolvers = searchMap.get(fieldValue);
                for (FieldResolver fieldResolver : fieldResolvers) {
                    PrivateFilterItem filterItem = queryContext.createFilterItem(fieldResolver.getFieldName(),
                            fieldResolver.getFieldType(), FilterType.Like, fieldValue);
                    orFilter.addChild(filterItem);
                }
            }
        }
    }

    @Override
    public F addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver) {
    	queryContext.addFieldResolver(fieldName, FieldType.Criteria);
        addFilter(fieldName, null);
        return self();
    }

    @Override
    public F addFieldResolver(String fieldName, FieldType fieldType, String expression) {
    	queryContext.addFieldResolver(fieldName, fieldType);
        return self();
    }

    @Override
    public F addFieldResolvers(String fieldName, FieldType fieldType, String... expressions) {
    	queryContext.addFieldResolvers(fieldName, fieldType, expressions);
        return self();
    }
    
    @Override
    public IQueryBuilder<T, T> getQueryBuilder() {
        return queryBuilder;
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
    public F addFilters(String fieldName, Object[] filterValues) {
        return addFilters(fieldName, FieldType.Path, filterValues);
    }

    @Override
    public F addFilters(String fieldName, Object[] filterValues, FilterType filterType) {
        return addFilters(fieldName, FieldType.Path, filterValues, filterType);
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
    public F addFilter(FilterItem filterItem) {
    	return addFilter(filterItem);
    }

    @Override
    public F addFilter(CompositeFilterItem parent, FilterItem filterItem) {
        queryContext.addFilter(parent, filterItem);
        return self();
    }

    @Override
    public F addFilters(FilterItem... filterItems) {
        queryContext.addFilters(filterItems);
        return self();
    }

    @Override
    public F addFilters(CompositeFilterItem parent, FilterItem... filterItems) {
        queryContext.addFilters(parent, filterItems);
        return self();
    }
    
    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues) {
        addFilters(fieldName, fieldType, filterValues, FilterType.Equals);
        return self();
    }

    @Override
    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType) {
        queryContext.addFilters(fieldName, fieldType, filterValues, filterType);
        return self();
    }
    
	@Override
    public F addFieldResolver(String fieldName, String expression) {
        return addFieldResolver(fieldName, FieldType.Path, expression);
    }

	@Override
    public F addFieldResolvers(String fieldName, String... expressions) {
        return addFieldResolvers(fieldName, FieldType.Path, expressions);
    }

	@Override
    public F addAll(String property, FilterItem... filterItems) {
		queryContext.all(property, filterItems);
        return self();
    }

	@Override
    public F addAnd(FilterItem... filterItems) {
		queryContext.and(filterItems);
        return self();
    }

	@Override
    public F addEmpty(String property) {
		queryContext.empty(property);
        return self();
    }

	@Override
    public F addEqual(String property, Object value) {
		queryContext.equal(property, value);
        return self();
    }

	@Override
    public F addGreaterOrEqual(String property, Object value) {
		queryContext.greaterOrEqual(property, value);
        return self();
    }

	@Override
    public F addGreater(String property, Object value) {
		queryContext.greater(property, value);
        return self();
    }

	@Override
    public F addLike(String property, String value) {
		queryContext.like(property, value);
        return self();
    }

	@Override
    public F addIn(String property, Collection<?> values) {
		queryContext.in(property, values);
        return self();
    }

	@Override
    public F addIn(String property, Object... values) {
		queryContext.in(property, values);
        return self();
    }

	@Override
    public F addLessOrEqual(String property, Object value) {
		queryContext.lessOrEqual(property, value);
        return self();
    }

	@Override
    public F addLess(String property, Object value) {
		queryContext.less(property, value);
        return self();
    }

	@Override
    public F addNone(String property, FilterItem... filterItems) {
		queryContext.none(property, filterItems);
        return self();
    }

	@Override
    public F addNot(FilterItem... filterItems) {
		queryContext.not(filterItems);
        return self();
    }

	@Override
    public F addNotEqual(String property, Object value) {
		queryContext.notEqual(property, value);
        return self();
    }

	@Override
    public F addNotIn(String property, Collection<?> values) {
		queryContext.notIn(property, values);
        return self();
    }

	@Override
    public F addNotIn(String property, Object... values) {
		queryContext.notIn(property, values);
        return self();
    }

	@Override
    public F addNotEmpty(String property) {
		queryContext.notEmpty(property);
        return self();
    }

	@Override
    public F addNotNull(String property) {
		queryContext.notNull(property);
        return self();
    }

	@Override
    public F addNull(String property) {
		queryContext.isNull(property);
        return self();
    }

	@Override
    public F addOr(FilterItem... filterItems) {
		queryContext.or(filterItems);
        return self();
    }

	@Override
    public F addSome(String property, FilterItem... filterItems) {
		queryContext.some(property, filterItems);
        return self();
    }
	
	@Override
    public F addRange(String property, Object minValue, Object maxValue) {
		queryContext.range(property, minValue, maxValue);
        return self();   	
    }

	@Override
    public F addSort(Sort sort) {
		queryContext.addSort(sort);
        return self();
    }

	@Override
    public F addSorts(Sort... sorts) {
		queryContext.addSorts(sorts);
        return self();
    }

	@Override
    public F addSort(String property, boolean desc) {
		queryContext.addSort(property, desc);
        return self();
    }

	@Override
    public F addSortAsc(String property) {
		queryContext.addSortAsc(property);
        return self();
    }

	@Override
    public F addSortDesc(String property) {
		queryContext.addSortDesc(property);
        return self();
    }
    
	@Override
	public F addSelectFields(String... fieldNames) {
		queryContext.addSelectFields(fieldNames);
        return self();
	}

	@Override
	public F addSelectFields(SelectType operator, String... fieldNames) {
		queryContext.addSelectFields(operator, fieldNames);	
        return self();
	}

	@Override
	public F addSelectField(String fieldName) {
		queryContext.addSelectField(fieldName);	
        return self();
	}

	@Override
	public F addSelectField(String fieldName, SelectType operator) {
		queryContext.addSelectField(fieldName, operator);
        return self();
	}
	
    @Override
    public F setJoinType(IJoinType joinType) {
        queryContext.setJoinType(joinType);
        return self();
    }

}
