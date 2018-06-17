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
import skyglass.data.filter.IDataFilter;
import skyglass.data.filter.FieldResolver;
import skyglass.data.filter.FieldType;
import skyglass.data.filter.FilterClass;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.IJoinResolver;
import skyglass.data.filter.DataFilterItem;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.PrivateFilterItemTree;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.data.query.QueryResult;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPath;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.ITypedQuery;

public class DataFilter<T> extends AbstractBaseDataFilter<T, IDataFilter<T>> implements IDataFilter<T> {

    protected Class<T> clazz;

    private IJoinType joinType;

    private IQueryBuilder<T, T> queryBuilder;

    public DataFilter(Class<T> clazz, JunctionType junctionType, IQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        super(junctionType, request);
        this.clazz = clazz;
        this.queryBuilder = queryBuilder;
        this.joinType = IJoinType.INNER;
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue) {
        return new DataFilterItem<T>(queryBuilder, clazz, getFieldResolver(fieldName, fieldType), filterValue);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue,
            FilterType filterType) {
        return new DataFilterItem<T>(queryBuilder, clazz, getFieldResolver(fieldName, fieldType), filterValue,
                filterType);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue,
            FilterClass filterClass) {
        return new DataFilterItem<T>(queryBuilder, clazz, getFieldResolver(fieldName, fieldType), filterValue,
                filterClass);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue,
            FilterType filterType, FilterClass filterClass) {
        return new DataFilterItem<T>(queryBuilder, clazz, getFieldResolver(fieldName, fieldType), filterValue, filterType,
                filterClass);
    }

    @Override
    public IQueryBuilder<T, T> getQueryBuilder() {
        return queryBuilder;
    }

    @Override
    public IPredicate createAtomicFilter(String fieldName, FilterType filterType,
            Supplier<Object> filterValueResolver) {
        return createAtomicFilter(fieldName, filterType, filterValueResolver, false);
    }

    private IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver,
            boolean resolvePropertyPath) {
        if (resolvePropertyPath) {
            fieldName = resolvePropertyPath(fieldName, joinType);
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
        return queryBuilder.createQuery(queryBuilder.createQuery(clazz));
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

    @Override
    public String resolvePropertyPath(String associationPath) {
        return queryBuilder.resolvePropertyPath(associationPath, joinType);
    }

    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType) {
        return queryBuilder.resolvePropertyPath(associationPath, joinType);
    }

    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return queryBuilder.resolvePropertyPath(associationPath, joinType, onClause);
    }

    @Override
    public String resolveAliasPath(String associationPath) {
        return queryBuilder.resolveAliasPath(associationPath);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType) {
        return queryBuilder.resolveAliasPath(associationPath, joinType);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return queryBuilder.resolveAliasPath(associationPath, joinType, onClause);
    }

    protected void doApplyFilter() {
        IPredicate result = applyFilters(rootFilterItem);
        if (result != null) {
            queryBuilder.getQuery().where(result);
        }
    }

    private IPredicate applyFilters(PrivateFilterItemTree parent) {
        IPredicate totalResult = null;
        for (PrivateFilterItem filterItem : parent.getChildren()) {
            IPredicate result = null;
            if (filterItem instanceof PrivateFilterItemTree) {
                result = applyFilters((PrivateFilterItemTree) filterItem);
            } else {
                result = applyFilter(filterItem.getFieldResolver(), filterItem.getFilterType(),
                        filterItem.getFilterValueResolver());
            }
            if (result == null) {
                continue;
            }
            if (totalResult == null) {
                totalResult = result;
            } else if (parent.getJunctionType() == JunctionType.AND) {
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
    public void setJoinType(IJoinType joinType) {
        this.joinType = joinType;
    }

    @Override
    public IJoinResolver<T, IDataFilter<T>> addLeftJoin(String alias) {
        return new CustomJoin<T, T, IDataFilter<T>>(IJoinType.LEFT, this, alias);
    }

    @Override
    public IJoinResolver<T, IDataFilter<T>> addJoin(String alias) {
        return new CustomJoin<T, T, IDataFilter<T>>(IJoinType.INNER, this, alias);
    }

}
