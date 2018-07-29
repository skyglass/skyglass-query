package skyglass.data.filter;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import skyglass.data.query.QueryResult;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.query.Sort;

public interface IBaseDataFilter<T, F>{
	
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

    public F addFilter(CompositeFilterItem parent, FilterItem filterItem);

    public F addFilters(FilterItem... filterItems);

    public F addFilters(CompositeFilterItem parent, FilterItem... filterItems);

    public F addFieldResolver(String fieldName, String expression);

    public F addFieldResolver(String fieldName, FieldType fieldType, String expression);

    public F addFieldResolvers(String fieldName, String... expressions);

    public F addFieldResolvers(String fieldName, FieldType fieldType, String... expressions);

    public F addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver);

    public F addCustomFilterResolver(CustomFilterResolver customFilterResolver);

    public F addAll(String property, FilterItem... filterItems);
    
    public F addAnd(FilterItem... filterItems);
    
    public F addEmpty(String property);
    
    public F addEqual(String property, Object value);
    
    public F addGreaterOrEqual(String property, Object value);
    
    public F addGreater(String property, Object value);
    
    public F addLike(String property, String value);
    
    public F addIn(String property, Collection<?> values);
    
    public F addIn(String property, Object... values);
    
    public F addLessOrEqual(String property, Object value);
    
    public F addLess(String property, Object value);

    public F addNone(String property, FilterItem... filterItems);
    
    public F addNot(FilterItem... filterItems);
    
    public F addNotEqual(String property, Object value);
    
    public F addNotIn(String property, Collection<?> values);
    
    public F addNotIn(String property, Object... values);
    
    public F addNotEmpty(String property);
    
    public F addNotNull(String property);
    
    public F addNull(String property);
    
    public F addOr(FilterItem... filterItems);
    
    public F addSome(String property, FilterItem... filterItems);
    
    public F addRange(String property, Object minValue, Object maxValue);
    
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

    public IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver);

    public IQueryBuilder<T, T> getQueryBuilder();
    
    public IJoinResolver<F, T> addJoin(String fieldName, String alias);

    public IJoinResolver<F, T> addLeftJoin(String fieldName, String alias);

}
