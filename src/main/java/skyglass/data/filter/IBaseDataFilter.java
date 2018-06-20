package skyglass.data.filter;

import java.util.List;
import java.util.function.Supplier;

import skyglass.data.query.QueryResult;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;

public interface IBaseDataFilter<T, F> {
	
    public QueryResult<T> getResult();

    public long getResultCount();

    public List<T> getUnpagedList();

    public F addOrder(String orderField, OrderType orderType);

    public F setOrder(String orderField, OrderType orderType);

    public F setDefaultOrder(String orderField, OrderType orderType);

    public F setPaging(Integer rowsPerPage, Integer pageNumber);

    public F addRequestSearch(String... fieldNames);

    public F addSearch(String filterValue, FieldType fieldType, String... fieldNames);

    public F addSearch(String filterValue, String... fieldNames);

    public F addFilter(String fieldName, Object filterValue);

    public F addFilter(String fieldName, Object filterValue, FilterType filterType);

    public F addFilters(String fieldName, Object[] filterValues);

    public F addFilters(String fieldName, Object[] filterValues, FilterType filterType);

    public F addFilter(String fieldName, FieldType fieldType, Object filterValue);

    public F addFilter(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType);

    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues);

    public F addFilters(String fieldName, FieldType fieldType, Object[] filterValues, FilterType filterType);

    public F addFilter(FilterItem filterItem);

    public F addFilter(FilterItemTree parent, FilterItem filterItem);

    public F addFilters(FilterItem... filterItems);

    public F addOrFilters(FilterItem... filterItems);

    public F addAndFilters(FilterItem... filterItems);

    public F addFilters(FilterItemTree parent, FilterItem... filterItems);

    public F addFieldResolver(String fieldName, String expression);

    public F addFieldResolver(String fieldName, FieldType fieldType, String expression);

    public F addFieldResolvers(String fieldName, String... expressions);

    public F addFieldResolvers(String fieldName, FieldType fieldType, String... expressions);

    public F addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver);

    public F addCustomFilterResolver(CustomFilterResolver customFilterResolver);

    public void setJoinType(IJoinType joinType);

    public IJoinResolver<T, F> addJoin(String alias);

    public IJoinResolver<T, F> addLeftJoin(String alias);

    public IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver);

    public IQueryBuilder<T, T> getQueryBuilder();

}
