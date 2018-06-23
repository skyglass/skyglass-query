package skyglass.data.filter;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import skyglass.data.query.QueryResult;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.query.QueryFilter;
import skyglass.query.model.query.Sort;

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
    
    public F addFilter(QueryFilter filter);

    public F addFilters(QueryFilter... filters);

    public F addFilterAll(String property, QueryFilter filter);
    
    public F addFilterAnd(QueryFilter... filters);
    
    public F addFilterEmpty(String property);
    
    public F addFilterEqual(String property, Object value);
    
    public F addFilterGreaterOrEqual(String property, Object value);
    
    public F addFilterGreaterThan(String property, Object value);
    
    public F addFilterILike(String property, String value);
    
    public F addFilterIn(String property, Collection<?> value);
    
    public F addFilterIn(String property, Object... value);
    
    public F addFilterLessOrEqual(String property, Object value);
    
    public F addFilterLessThan(String property, Object value);
    
    public F addFilterLike(String property, String value);

    public F addFilterNone(String property, QueryFilter filter);
    
    public F addFilterNot(QueryFilter filter);
    
    public F addFilterNotEqual(String property, Object value);
    
    public F addFilterNotIn(String property, Collection<?> value);
    
    public F addFilterNotIn(String property, Object... value);
    
    public F addFilterNotEmpty(String property);
    
    public F addFilterNotNull(String property);
    
    public F addFilterNull(String property);
    
    public F addFilterOr(QueryFilter... filters);
    
    public F addFilterSome(String property, QueryFilter filter);
    
    public F addSort(Sort sort);
    
    public F addSorts(Sort... sorts);
    
    public F addSort(String property, boolean desc);
    
    public F addSortAsc(String property);
    
    public F addSortDesc(String property);
    
    public F addSelectFields(String... fieldNames);
    
    public F addSelectFields(SelectType operator, String... fieldNames);

    public F addSelectField(String fieldName);

    public F addSelectField(String fieldName, SelectType operator);

    public F setJoinType(IJoinType joinType);

    public IJoinResolver<T, F> addJoin(String alias);

    public IJoinResolver<T, F> addLeftJoin(String alias);

    public IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver);

    public IQueryBuilder<T, T> getQueryBuilder();

}
