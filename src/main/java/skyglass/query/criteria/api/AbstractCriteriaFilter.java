package skyglass.query.criteria.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import skyglass.data.filter.AbstractBaseDataFilter;
import skyglass.data.filter.CustomFilterResolver;
import skyglass.data.filter.CustomJpaFilterResolver;
import skyglass.data.filter.FieldResolver;
import skyglass.data.filter.FieldType;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.ICriteriaFilter;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.model.criteria.ICriteriaQueryBuilder;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPath;
import skyglass.query.model.criteria.IPredicate;

public abstract class AbstractCriteriaFilter<T, F> extends AbstractBaseDataFilter<T, F> 
	implements ICriteriaFilter<T, F> {
	
	private ICriteriaQueryBuilder<T, T> queryBuilder;

    public AbstractCriteriaFilter(Class<T> rootClazz, JunctionType junctionType, ICriteriaQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

    public AbstractCriteriaFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType, ICriteriaQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        super(rootClazz, junctionType, joinType, request, 
        		queryBuilder);
        this.queryBuilder = queryBuilder;
    }

    @Override
    public IPredicate createAtomicFilter(String fieldName, FilterType filterType,
            Supplier<Object> filterValueResolver) {
        return createAtomicFilter(fieldName, filterType, filterValueResolver, false);
    }
    
    @Override
    public ICriteriaQueryBuilder<T, T> getQueryBuilder() {
    	return queryBuilder;
    }

    private IPredicate createAtomicFilter(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver,
            boolean resolvePropertyPath) {
        if (resolvePropertyPath) {
            fieldName = queryContext.resolvePropertyPath(fieldName);
        }
        return queryBuilder.getPredicate(fieldName, filterType, filterValueResolver);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IExpression<?> coalesce(String concat) {
        return queryBuilder.coalesce(queryBuilder.lower((IPath) queryBuilder.getExpression(concat)), () -> "");
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IExpression<?> concat(IExpression<?> concat1, IExpression<?> concat2) {
        return queryBuilder.concat((IExpression) concat1, (IExpression) concat2);
    }

    @Override
    protected void applyFilter(PrivateCompositeFilterItem rootFilterItem) {
        IPredicate result = applyFilters(rootFilterItem);
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

}
