package skyglass.query.criteria.api;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import skyglass.data.filter.FilterType;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.api.AbstractQueryBuilder;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.ICriteriaQueryBuilder;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoin;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.IQueryProcessor;

public abstract class AbstractCriteriaQueryBuilder<E, S> extends AbstractQueryBuilder<E, S>
	implements ICriteriaQueryBuilder<E, S> {
	
    public AbstractCriteriaQueryBuilder(MetadataHelper metadataHelper) {
		super(metadataHelper);
	}

	private Map<String, IJoin<?, ?>> joins = new HashMap<>();
	
	@Override
	public IQueryProcessor setQueryProcessor(MetadataHelper metadataHelper, PrivateQueryContext privateQueryContext) {
		return null;
	}

    @Override
    public IPredicate getPredicate(String fieldName, FilterType filterType, 
    		Supplier<Object> filterValueResolver) {
        if (filterType == FilterType.Like) {
            return applyLikeFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.Equals) {
            return applyEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.GreaterOrEquals) {
            return applyGreaterOrEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.Greater) {
            return applyGreaterFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.LessOrEquals) {
            return applyLessOrEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.Less) {
            return applyLessFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.NotEquals) {
            return applyNotEqualsFilter(fieldName, filterValueResolver);
        } else if (filterType == FilterType.EqualsProp) {
            return applyEqualPropertyFilter(fieldName, filterValueResolver);
        } else {
            return applyEqualsFilter(fieldName, filterValueResolver);
        }
    }
    
   @Override
   public void addJoin(String path, String parentPath, String alias, IJoinType joinType, IPredicate onClause) {
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
        return gt(getExpression(fieldName), IQueryBuilder.getNumberValue(filterValueResolver));
    }

    private IPredicate applyGreaterOrEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return ge(getExpression(fieldName), IQueryBuilder.getNumberValue(filterValueResolver));
    }

    private IPredicate applyLessFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return lt(getExpression(fieldName), IQueryBuilder.getNumberValue(filterValueResolver));
    }

    private IPredicate applyLessOrEqualsFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return le(getExpression(fieldName), IQueryBuilder.getNumberValue(filterValueResolver));
    }

    private IPredicate applyLikeFilter(String fieldName, Supplier<Object> filterValueResolver) {
        return like(lower(getExpression(fieldName)), () -> filterValueResolver.get().toString());
    }

}
