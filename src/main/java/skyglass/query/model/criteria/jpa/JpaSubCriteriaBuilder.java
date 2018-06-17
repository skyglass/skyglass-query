package skyglass.query.model.criteria.jpa;

import javax.persistence.EntityManager;

import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISubQueryBuilder;
import skyglass.query.model.criteria.ISubquery;

public class JpaSubCriteriaBuilder<E0, S, S0> extends AbstractJpaQueryBuilder<E0, S0> implements ISubQueryBuilder<E0, S0> {

    private final ISubquery<S0> subQuery;

    private final IRoot<E0> root;

    public JpaSubCriteriaBuilder(EntityManager entityManager, ICriteriaQuery<S> parentQuery, 
    		Class<E0> entityClass, Class<S0> selectClass) {
        super(entityManager, entityClass, selectClass);
        this.subQuery = parentQuery.subquery(selectClass);
        this.root = subQuery.from(entityClass);
    }

    @Override
    public ISubquery<S0> getSubQuery() {
        return subQuery;
    }

    @Override
    public IRoot<E0> getRoot() {
        return root;
    }

    @Override
    public ICriteriaQuery<S0> getQuery() {
        throw new UnsupportedOperationException("cannot get parent query, please use other API");
    }

}
