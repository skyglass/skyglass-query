package skyglass.query.api;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import skyglass.data.filter.FilterType;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoin;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;

public abstract class AbstractQueryBuilder<E, S> implements IQueryBuilder<E, S> {

    private Map<String, IJoin<?, ?>> joins = new HashMap<>();

    @Override
    public IPredicate getPredicate(String fieldName, FilterType filterType, Supplier<Object> filterValueResolver) {
        if (filterType == FilterType.LIKE) {
            return applyLikeFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.EQ) {
            return applyEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.GE) {
            return applyGreaterOrEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.GT) {
            return applyGreaterFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.LE) {
            return applyLessOrEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.LT) {
            return applyLessFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.NE) {
            return applyNotEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.EQPR) {
            return applyEqualPropertyFilter(fieldName, filterValueResolver);
        } else {
            return applyEqualsFilter(fieldName, filterValueResolver);
        }
    }

    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType) {
        return resolvePropertyPath(associationPath, joinType, null);
    }

    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return createAliases(associationPath, joinType, onClause, false);
    }

    @Override
    public String resolveAliasPath(String associationPath) {
        return resolveAliasPath(associationPath, IJoinType.INNER);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType) {
        return resolveAliasPath(associationPath, joinType, null);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return createAliases(associationPath, joinType, onClause, true);
    }

    public <T> IExpression<T> getExpression(String expression) {
        String original = expression;
        expression = IQueryBuilder.normalizeFieldName(expression, false);
        String[] values1 = expression.split("\\.");
        String[] values = values1[0].split("_");
        if (values1.length == 1 && values.length == 1) {
            return getRoot().get(original);
        }
        int i = 0;
        String currentPath = values[0];
        for (String value : values) {
            if (i > 0) {
                currentPath = currentPath + "." + value;
            }
            i++;
        }
        if (values1.length == 2) {
            String propertyName = IQueryBuilder.denormalizePropertyName(values1[1]);
            return joins.get(currentPath).get(propertyName);
        }
        return null;
    }

    private String createAliases(String expression, IJoinType joinType, IPredicate onClause, boolean forceLast) {
        String original = expression;
        expression = IQueryBuilder.normalizeFieldName(expression, forceLast);
        String[] values1 = expression.split("\\.");
        String[] values = values1[0].split("_");
        if (values1.length == 1 && values.length == 1) {
            if (forceLast) {
                createAlias(values1[0], null, values1[0], joinType, onClause);
            }
            return original;
        }
        int i = 0;
        String currentPath = values[0];
        String currentAlias = values[0];
        createAlias(currentPath, null, values[0], joinType);
        for (String value : values) {
            if (i > 0) {
                if (i == values.length - 1 && values1.length == 1) {
                    createAlias(currentPath + "." + value, currentPath, currentAlias + "_" + value, joinType, onClause);
                } else {
                    createAlias(currentPath + "." + value, currentPath, currentAlias + "_" + value, joinType);
                }
                currentAlias = currentAlias + "_" + value;
                currentPath = currentPath + "." + value;
            }
            i++;
        }
        if (values1.length == 2) {
            if (forceLast) {
                createAlias(currentPath + "." + values1[1], currentPath, currentAlias + "_" + values1[1], joinType,
                        onClause);
                currentAlias = currentAlias + "_" + values1[1];
                return currentAlias;
            }
            String propertyName = IQueryBuilder.denormalizePropertyName(values1[1]);
            return currentAlias + "." + propertyName;
        }
        return currentAlias;
    }

    private void createAlias(String path, String parentPath, String alias, IJoinType joinType) {
        createAlias(path, parentPath, alias, joinType, null);
    }

    private void createAlias(String path, String parentPath, String alias, IJoinType joinType, IPredicate onClause) {
        if (joins.containsKey(path)) {
            return;
        }
        IJoin<?, ?> join = null;
        if (parentPath != null) {
            IJoin<?, ?> parentJoin = joins.get(parentPath);
            join = parentJoin.join(alias, joinType);
        } else {
            join = getRoot().join(alias, joinType);
        }
        joins.put(path, join);
        if (onClause != null) {
            join.on(onClause);
        }
    }

    private IPredicate applyEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return equal(getExpression(fieldName), filterValueResolver);
    }

    private IPredicate applyNotEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return notEqual(getExpression(fieldName), filterValueResolver);
    }

    private IPredicate applyEqualPropertyFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return equalProperty(getExpression(fieldName), getExpression((String) filterValueResolver.get()));
    }

    private IPredicate applyGreaterFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return gt(getExpression(fieldName), getNumberValue(filterValueResolver));
    }

    private IPredicate applyGreaterOrEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return ge(getExpression(fieldName), getNumberValue(filterValueResolver));
    }

    private IPredicate applyLessFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return lt(getExpression(fieldName), getNumberValue(filterValueResolver));
    }

    private IPredicate applyLessOrEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return le(getExpression(fieldName), getNumberValue(filterValueResolver));
    }

    private IPredicate applyLikeFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return like(lower(getExpression(fieldName)), () -> filterValueResolver.get().toString());
    }

    private Supplier<Number> getNumberValue(Supplier<Object> filterValueResolver) {
        return () -> {
            Object filterValue = filterValueResolver;
            if (filterValue instanceof Number) {
                return (Number) filterValue;
            }
            if (filterValue instanceof Date) {
                return ((Date) filterValue).getTime();
            }
            throw new UnsupportedOperationException("Unsupported Number filter value: " + filterValue);
        };
    }

}
