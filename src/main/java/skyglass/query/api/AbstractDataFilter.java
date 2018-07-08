package skyglass.query.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import skyglass.data.filter.AbstractBaseDataFilter;
import skyglass.data.filter.CustomFilterResolver;
import skyglass.data.filter.CustomJoin;
import skyglass.data.filter.CustomJpaFilterResolver;
import skyglass.data.filter.FieldResolver;
import skyglass.data.filter.FieldType;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.IDataFilter;
import skyglass.data.filter.IJoinResolver;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.data.query.QueryResult;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPath;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.ITypedQuery;

public abstract class AbstractDataFilter<T, F> extends AbstractBaseDataFilter<T, F> implements IDataFilter<T, F> {

    public AbstractDataFilter(Class<T> rootClazz, JunctionType junctionType, IQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

    public AbstractDataFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType, IQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        super(rootClazz, junctionType, joinType, request, queryBuilder);
    }

    @Override
    public IPredicate createAtomicFilter(String fieldName, FilterType filterType,
            Supplier<Object> filterValueResolver) {
        return createAtomicFilter(fieldName, filterType, filterValueResolver, false);
    }

    private IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver,
            boolean resolvePropertyPath) {
        if (resolvePropertyPath) {
            fieldName = queryContext.resolvePropertyPath(fieldName);
        }
        return queryBuilder.getPredicate(fieldName, filterType, filterValueResolver);
    }

    @Override
    public List<T> getFullResult() {
        doApplyFilter();
        resolveCustomFilters();
        doApplyOrder();
        List<T> result = createResultQuery().getResultList();
        return result;
    }

    @Override
    protected void initBeforeResult() {
        initSearch();
    }

    @Override
    protected long getRowCount() {
        doApplyFilter();
        resolveCustomFilters();
        long rowCount = createCountResultQuery().getSingleResult();
        return rowCount;
    }

    @Override
    protected QueryResult<T> getPagedResult() {
        long rowCount = getRowCount();
        QueryResult<T> result = new QueryResult<T>();
        result.setTotalRecords(rowCount);
        if (rowCount == 0) {
            result.setResults(Collections.emptyList());
            return result;
        }
        ITypedQuery<T> query = setRootResult();
        result.setResults(query.getResultList());
        return result;
    }

    private ITypedQuery<T> setRootResult() {
        int rowsPerPage = getRowsPerPage();
        doApplyOrder();
        ITypedQuery<T> result = createResultQuery();
        result.setFirstResult((getPageNumber() - 1) * rowsPerPage);
        result.setMaxResults(rowsPerPage);
        return result;
    }

    private ITypedQuery<T> createResultQuery() {
        return queryBuilder.createQuery(queryBuilder.createQuery(rootClazz));
    }

    private ITypedQuery<Long> createCountResultQuery() {
        return queryBuilder.createQuery(queryBuilder.createCountCriteria());
    }

    @Override
    protected List<T> getUnpagedResult() {
        doApplyFilter();
        resolveCustomFilters();
        doApplyOrder();
        return createResultQuery().getResultList();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void resolveCustomFilter(CustomFilterResolver filterResolver) {
        ((CustomJpaFilterResolver<T, T>) filterResolver).addCustomFilter(queryBuilder);
    }

    @Override
    protected void applyOrder(List<OrderField> orderFields) {
        for (OrderField orderField : orderFields) {
            FieldResolver fieldResolver = orderField.getOrderField();
            if (fieldResolver.isMultiple()) {
                applyMultipleOrder(orderField);
            } else {
                applySingleOrder(orderField);
            }
        }

    }

    private void applyMultipleOrder(OrderField orderField) {
        IExpression<?> orderExpression = getOrderExpression(orderField);
        queryBuilder.getQuery().orderBy(getOrder(orderField, orderExpression));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void applySingleOrder(OrderField orderField) {
        List<IOrder> orderList = new ArrayList<>();
        for (String fieldResolver : orderField.getOrderField().getResolvers()) {
            if (orderField.isDescending()) {
                orderList.add(queryBuilder
                        .desc(queryBuilder.lower((IExpression) queryBuilder.getExpression(fieldResolver))));
            } else {
                orderList.add(queryBuilder
                        .asc(queryBuilder.lower((IExpression) queryBuilder.getExpression(fieldResolver))));
            }
        }
        queryBuilder.getQuery().orderBy(orderList);
    }

    private IExpression<?> getOrderExpression(OrderField orderField) {
        return concat(0, orderField.getOrderField().getResolvers());
    }

    private IOrder getOrder(OrderField orderField, IExpression<?> orderExpression) {
        if (orderField.isDescending()) {
            return queryBuilder.desc(orderExpression);
        } else {
            return queryBuilder.asc(orderExpression);
        }
    }

    private IExpression<?> concat(int i, Collection<String> fieldResolvers) {
        IExpression<?> expression = coalesce(getFieldResolver(i, fieldResolvers));
        if (i < fieldResolvers.size() - 1) {
            return concat(expression, concat(i + 1, fieldResolvers));
        } else {
            return expression;
        }
    }

    private String getFieldResolver(int i, Collection<String> fieldResolvers) {
        int j = 0;
        for (String fieldResolver : fieldResolvers) {
            if (j == i) {
                return fieldResolver;
            }
            j++;
        }
        return null;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IExpression<?> coalesce(String concat) {
        return queryBuilder.coalesce(queryBuilder.lower((IPath) queryBuilder.getExpression(concat)), () -> "");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IExpression<?> concat(IExpression<?> concat1, IExpression<?> concat2) {
        return queryBuilder.concat((IExpression) concat1, (IExpression) concat2);
    }

    protected void doApplyFilter() {
        IPredicate result = applyFilters(queryContext.getRootFilterItem());
        if (result != null) {
            queryBuilder.getQuery().where(result);
        }
    }

    private IPredicate applyFilters(PrivateCompositeFilterItem parent) {
        IPredicate totalResult = null;
        for (PrivateFilterItem filterItem : parent.getChildren()) {
            IPredicate result = null;
            if (filterItem instanceof PrivateCompositeFilterItem) {
                result = applyFilters((PrivateCompositeFilterItem) filterItem);
            } else {
                result = applyFilter(filterItem.getFieldResolver(), filterItem.getFilterType(),
                        filterItem.getFilterValueResolver());
            }
            if (result == null) {
                continue;
            }
            if (totalResult == null) {
                totalResult = result;
            } else if (parent.getFilterType() == FilterType.And) {
                totalResult = queryBuilder.and(result, totalResult);
            } else {
                totalResult = queryBuilder.or(result, totalResult);
            }
        }
        return totalResult;
    }

    private IPredicate applyFilter(FieldResolver fieldResolver, FilterType filterType,
            Supplier<Object> filterValueResolver) {
        IPredicate totalResult = null;
        for (String field : fieldResolver.getResolvers()) {
            IPredicate result = applyAtomicFilter(fieldResolver, field, filterType, filterValueResolver);
            if (totalResult == null) {
                totalResult = result;
            } else {
                totalResult = queryBuilder.or(result, totalResult);
            }
        }
        return totalResult;
    }

    private IPredicate applyAtomicFilter(FieldResolver fieldResolver, String fieldName, FilterType filterType,
            Supplier<Object> filterValueResolver) {
        if (fieldResolver.getFieldType() == FieldType.Criteria) {
            return fieldResolver.getCustomFieldResolver().getPredicate();
        }
        return createAtomicFilter(fieldName, filterType, filterValueResolver, true);
    }

    @Override
    public IJoinResolver<T, F> addLeftJoin(String alias) {
        return new CustomJoin<T, T, F>(self(), queryContext, alias, IJoinType.LEFT);
    }    
        
    @Override
    public IJoinResolver<T, F> addJoin(String alias) {
        return new CustomJoin<T, T, F>(self(), queryContext, alias, IJoinType.INNER);
    }

}
