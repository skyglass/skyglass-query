package skyglass.query.model.query.jpa;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import skyglass.data.filter.Dialect;
import skyglass.query.api.AbstractQueryBuilder;
import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISubQueryBuilder;
import skyglass.query.model.criteria.ISubquery;
import skyglass.query.model.criteria.ITypedQuery;
import skyglass.query.model.query.IQuery;
import skyglass.query.model.query.QueryProcessor;

public class JpaQueryBuilder<E, S> extends AbstractQueryBuilder<E, S> {

    private QueryProcessor<E, S> queryProcessor;
    
    protected Class<E> entityClass;
    
    protected Class<S> selectClass;
    
    protected List<Object> paramList = new ArrayList<Object>();
    
    public JpaQueryBuilder(Class<E> entityClass, Class<S> selectClass) {
		this.entityClass = entityClass;
		this.selectClass = selectClass;
	}

    @Override
    public IRoot<E> getRoot() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICriteriaQuery<S> getQuery() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> ICriteriaQuery<T> createQuery(Class<T> clazz) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ICriteriaQuery<Long> createCountCriteria() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IExpression<Long> count(IExpression<?> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate isNull(IExpression<T> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate isNotNull(IExpression<T> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate equal(IExpression<T> expression, Supplier<Object> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate equalProperty(IExpression<?> expression1, IExpression<?> expression2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate notEqual(IExpression<T> expression, Supplier<Object> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <T> IPredicate notEqualProperty(IExpression<?> expression1, IExpression<?> expression2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate gt(IExpression<Number> expression, Supplier<Number> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate ge(IExpression<Number> expression, Supplier<Number> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate lt(IExpression<Number> expression, Supplier<Number> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate le(IExpression<Number> expression, Supplier<Number> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IExpression<String> lower(IExpression<String> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate like(IExpression<String> expression, Supplier<String> valueResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOrder desc(IExpression<?> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IOrder asc(IExpression<?> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public <Y> IExpression<String> coalesce(IExpression<? extends Y> expression, Supplier<Y> repacementResolver) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IExpression<String> concat(IExpression<String> concat1, IExpression<String> concat2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate and(IExpression<Boolean> expression1, IExpression<Boolean> expression2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate or(IExpression<Boolean> expression1, IExpression<Boolean> expression2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate exists(ISubquery<?> subquery) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IPredicate not(IExpression<Boolean> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IExpression<Number> max(IExpression<Number> expression) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IQuery createQuery(String queryString) {
        // TODO Auto-generated method stub
        return null;
    }

	@Override
	public <T> ITypedQuery<T> createQuery(ICriteriaQuery<T> criteriaQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <E0, S0> ISubQueryBuilder<E0, S0> createSubCriteriaBuilder(ICriteriaQuery<S> parentQuery,
			Class<E0> subEntityClass, Class<S0> subSelectClass) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Dialect getDialect() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Long getCurrentUserId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCollection(Class<?> rootClass, String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEntity(Class<?> rootClass, String path) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isId(Class<?> rootClass, String path) {
		// TODO Auto-generated method stub
		return false;
	}

}
