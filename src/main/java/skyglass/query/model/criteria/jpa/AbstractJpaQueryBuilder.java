package skyglass.query.model.criteria.jpa;

import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;

import skyglass.data.filter.Dialect;
import skyglass.query.api.AbstractQueryBuilder;
import skyglass.query.metadata.jpa.JpaMetadataHelper;
import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISubQueryBuilder;
import skyglass.query.model.criteria.ISubquery;
import skyglass.query.model.criteria.ITypedQuery;
import skyglass.query.model.query.IQuery;

public abstract class AbstractJpaQueryBuilder<E, S> extends AbstractQueryBuilder<E, S> {

    private CriteriaBuilder criteriaBuilder;
    
    private EntityManager entityManager;
    
    private JpaMetadataHelper jpaMetadataHelper;    

    private final ICriteriaQuery<S> query;

    private final IRoot<E> root;

    public AbstractJpaQueryBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
        this.entityManager = entityManager;
        this.jpaMetadataHelper = JpaMetadataHelper.getInstanceForMetamodel(entityManager.getMetamodel());
        this.query = createQuery(selectClass);
        this.root = query.from(entityClass);
    }
    
	@Override
	public IQuery createQuery(String queryString) {
        return new JpaQuery(entityManager.createQuery(queryString));
	}
    
    @Override
    public ICriteriaQuery<S> getQuery() {
        return query;
    }

    @Override
    public IRoot<E> getRoot() {
        return root;
    }

	@Override
	public Long getCurrentUserId() {
		// TODO Auto-generated method stub
		return null;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> ICriteriaQuery<T> createQuery(Class<T> clazz) {
        return new JpaCriteriaQuery(criteriaBuilder.createQuery(clazz));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IExpression<Long> count(IExpression<?> expression) {
        return new JpaExpression(criteriaBuilder.count(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate isNull(IExpression<T> expression) {
        return new JpaPredicate(criteriaBuilder.isNull(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate isNotNull(IExpression<T> expression) {
        return new JpaPredicate(criteriaBuilder.isNotNull(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate equal(IExpression<T> expression, Supplier<Object> valueResolver) {
        return new JpaPredicate(
                criteriaBuilder.equal(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate equalProperty(IExpression<?> expression1, IExpression<?> expression2) {
        return new JpaPredicate(criteriaBuilder.equal(((JpaExpression) expression1).getExpression(),
                ((JpaExpression) expression2).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate notEqual(IExpression<T> expression, Supplier<Object> valueResolver) {
        return new JpaPredicate(
                criteriaBuilder.notEqual(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T> IPredicate notEqualProperty(IExpression<?> expression1, IExpression<?> expression2) {
        return new JpaPredicate(criteriaBuilder.notEqual(((JpaExpression) expression1).getExpression(),
                ((JpaExpression) expression2).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate gt(IExpression<Number> expression, Supplier<Number> valueResolver) {
        return new JpaPredicate(criteriaBuilder.gt(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate ge(IExpression<Number> expression, Supplier<Number> valueResolver) {
        return new JpaPredicate(criteriaBuilder.ge(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate lt(IExpression<Number> expression, Supplier<Number> valueResolver) {
        return new JpaPredicate(criteriaBuilder.lt(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate le(IExpression<Number> expression, Supplier<Number> valueResolver) {
        return new JpaPredicate(criteriaBuilder.le(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IExpression<String> lower(IExpression<String> expression) {
        return new JpaExpression(criteriaBuilder.lower(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate like(IExpression<String> expression, Supplier<String> valueResolver) {
        return new JpaPredicate(
                criteriaBuilder.like(((JpaExpression) expression).getExpression(), valueResolver.get()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IOrder desc(IExpression<?> expression) {
        return new JpaOrder(criteriaBuilder.desc(((JpaExpression) (expression)).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IOrder asc(IExpression<?> expression) {
        return new JpaOrder(criteriaBuilder.asc(((JpaExpression) (expression)).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <Y> IExpression<String> coalesce(IExpression<? extends Y> expression, Supplier<Y> replacementResolver) {
        return new JpaExpression(
                criteriaBuilder.coalesce(((JpaExpression) expression).getExpression(), replacementResolver.get()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IExpression<String> concat(IExpression<String> expression1, IExpression<String> expression2) {
        return new JpaExpression(criteriaBuilder.concat(((JpaExpression) expression1).getExpression(),
                ((JpaExpression) expression2).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate and(IExpression<Boolean> expression1, IExpression<Boolean> expression2) {
        return new JpaPredicate(criteriaBuilder.and(((JpaExpression) expression1).getExpression(),
                ((JpaExpression) expression2).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate or(IExpression<Boolean> expression1, IExpression<Boolean> expression2) {
        return new JpaPredicate(criteriaBuilder.or(((JpaExpression) expression1).getExpression(),
                ((JpaExpression) expression2).getExpression()));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public IPredicate exists(ISubquery<?> subquery) {
        return new JpaPredicate(criteriaBuilder.exists(((JpaSubquery) subquery).getSubquery()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IPredicate not(IExpression<Boolean> expression) {
        return new JpaPredicate(criteriaBuilder.not(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public IExpression<Number> max(IExpression<Number> expression) {
        return new JpaExpression(criteriaBuilder.max(((JpaExpression) expression).getExpression()));
    }

    @Override
    public ICriteriaQuery<Long> createCountCriteria() {
        ICriteriaQuery<Long> countCriteria = createQuery(Long.class);
        IRoot<S> entityRoot = countCriteria.from(getQuery().getResultType());
        countCriteria.select(count(entityRoot));
        countCriteria.where(getQuery().getRestriction());
        return countCriteria;
    }
    
    public boolean isMatchesSearchQuery(String result, String searchQuery) {
        return searchQuery != null && result.toLowerCase().matches(searchQuery.toLowerCase());
    }

    public boolean areMatchSearchQuery(String searchQuery, String... results) {
        if (searchQuery == null) {
            return false;
        }
        for (String result : results) {
            if (result != null && result.toLowerCase().matches(searchQuery.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public <E0, S0> ISubQueryBuilder<E0, S0> createSubCriteriaBuilder(ICriteriaQuery<S> parentQuery, 
    		Class<E0> subEntityClass, Class<S0> subSelectClass) {
        return new JpaSubCriteriaBuilder<E0, S, S0>(entityManager, parentQuery, subEntityClass, subSelectClass);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> ITypedQuery<T> createQuery(ICriteriaQuery<T> criteriaQuery) {
        return new JpaTypedQuery(entityManager.createQuery(((JpaCriteriaQuery) criteriaQuery).getCriteriaQuery()));
    }

    @Override
    public Dialect getDialect() {
        return Dialect.HANADB;
    }

    @Override
    public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName) {
        return () -> jpaMetadataHelper.isNumericField(rootClass, propertyName);
    }

    @Override
    public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection) {
        return () -> jpaMetadataHelper.convertObject(rootClass, property, value, isCollection);
    }

}
