package skyglass.query.model.criteria;

import java.util.function.Supplier;

import skyglass.data.filter.Dialect;
import skyglass.data.filter.FilterType;

public interface ICriteriaQueryBuilder<E, S> extends IQueryBuilder<E>, IJoinBuilder {
	
    public IRoot<E> getRoot();

    public ICriteriaQuery<S> getQuery();

    public <T> IExpression<T> getExpression(String expression);

    public <T> ICriteriaQuery<T> createCriteriaQuery(Class<T> clazz);

    public ICriteriaQuery<Long> createCountCriteria();

    public IExpression<Long> count(IExpression<?> expression);

    public <T> IPredicate isNull(IExpression<T> expression);

    public <T> IPredicate isNotNull(IExpression<T> expression);

    public <T> IPredicate equal(IExpression<T> expression, Supplier<Object> valueResolver);

    public <T> IPredicate equalProperty(IExpression<?> expression1, IExpression<?> expression2);

    public <T> IPredicate notEqual(IExpression<T> expression, Supplier<Object> valueResolver);

    public <T> IPredicate notEqualProperty(IExpression<?> expression1, IExpression<?> expression2);

    public IPredicate gt(IExpression<Number> expression, Supplier<Number> valueResolver);

    public IPredicate ge(IExpression<Number> expression, Supplier<Number> valueResolver);

    public IPredicate lt(IExpression<Number> expression, Supplier<Number> valueResolver);

    public IPredicate le(IExpression<Number> expression, Supplier<Number> valueResolver);

    public IExpression<String> lower(IExpression<String> expression);

    public IPredicate like(IExpression<String> expression, Supplier<String> valueResolver);

    public IOrder desc(IExpression<?> expression);

    public IOrder asc(IExpression<?> expression);

    public <Y> IExpression<String> coalesce(IExpression<? extends Y> expression, Supplier<Y> repacementResolver);

    public IExpression<String> concat(IExpression<String> concat1, IExpression<String> concat2);

    public IPredicate and(IExpression<Boolean> expression1, IExpression<Boolean> expression2);

    public IPredicate or(IExpression<Boolean> expression1, IExpression<Boolean> expression2);

    public IPredicate exists(ISubquery<?> subquery);

    public IPredicate not(IExpression<Boolean> expression);

    public IExpression<Number> max(IExpression<Number> expression);

    public IPredicate getPredicate(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver);

    public <T> ITypedQuery<T> createTypedQuery(ICriteriaQuery<T> criteriaQuery);

    public ISubQueryBuilder<E, S> createSubCriteriaBuilder(ICriteriaQuery<S> parentQuery, Class<E> subEntityClass, Class<S> subSelectClass);

    public Dialect getDialect();

    public Long getCurrentUserId();
    
}
