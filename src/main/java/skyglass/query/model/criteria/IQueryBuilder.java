package skyglass.query.model.criteria;

import java.util.function.Supplier;

import skyglass.data.filter.Dialect;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.model.query.IQuery;

public interface IQueryBuilder<E, S> extends IValueResolver, IJoinBuilder {
	
    public IQuery createQuery(String queryString);

    public IRoot<E> getRoot();

    public ICriteriaQuery<S> getQuery();

    public <T> IExpression<T> getExpression(String expression);

    public <T> ICriteriaQuery<T> createQuery(Class<T> clazz);

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

    public <T> ITypedQuery<T> createQuery(ICriteriaQuery<T> criteriaQuery);

    public <E0, S0> ISubQueryBuilder<E0, S0> createSubCriteriaBuilder(ICriteriaQuery<S> parentQuery, Class<E0> subEntityClass, Class<S0> subSelectClass);

    public Dialect getDialect();

    public Long getCurrentUserId();
    
    public PrivateQueryContext setPrivateQueryContext(
    		JunctionType junctionType, Class<?> rootClazz, IJoinType joinType);
    
    public PrivateQueryContext getPrivateQueryContext();
    
    public static String convertToRegexp(String filterString) {
        String result = filterString.replace("*", "\\*");
        result = result.replace("%", ".*");
        if (!result.endsWith(".*")) {
            result += ".*";
        }
        if (!result.startsWith(".*")) {
            result = ".*" + result;
        }

        return result;
    }

    public static String processFilterString(Object filterString) {
        String result = filterString.toString().replace("\\*", "*");
        result = result.replace('*', '%');
        if (!result.endsWith("%")) {
            result += "%";
        }
        if (!result.startsWith("%")) {
            result = "%" + result;
        }

        return result.toLowerCase();
    }

    public static String normalizeFieldName(String expression, boolean forceLast) {
        String[] values1 = expression.split("\\.");
        if (values1.length == 1) {
            return expression;
        }
        String propertyName = values1[values1.length - 1];
        int aliasCount = values1.length - 1;
        // check if it's embedded id.
        // be sure that embedded id has name "id", otherwise this logic won't
        // work
        if (values1[values1.length - 2].equals("id")) {
            propertyName = "id:" + propertyName;
            aliasCount = values1.length - 2;
        }
        if (aliasCount == 0) {
            return propertyName;
        }
        String[] values = new String[aliasCount];
        for (int i = 0; i < aliasCount; i++) {
            values[i] = values1[i];
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append("_");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (forceLast) {
            sb.append("_").append(propertyName);
        } else {
            sb.append(".").append(propertyName);
        }
        return sb.toString();
    }

    // embedded id in the result has the following format: id:propertyName
    // this method replaces ":" to "."
    public static String denormalizePropertyName(String expression) {
        String[] values = expression.split(":");
        if (values.length == 1) {
            return expression;
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String resolvePropertyName(String expression) {
        String original = expression;
        expression = normalizeFieldName(expression, false);
        String[] values1 = expression.split("\\.");
        if (values1.length == 1) {
            return original;
        }
        String propertyName = denormalizePropertyName(values1[1]);
        return values1[0] + "." + propertyName;
    }

    public static boolean hasAlias(String property) {
        String[] values1 = property.split("\\.");
        if (values1.length == 1) {
            return false;
        }
        return true;
    }

}
