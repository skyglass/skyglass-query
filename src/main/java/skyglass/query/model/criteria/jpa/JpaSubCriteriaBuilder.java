package skyglass.query.model.criteria.jpa;

import javax.persistence.EntityManager;

import skyglass.query.metadata.jpa.JpaMetadataHelper;
import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISubQueryBuilder;
import skyglass.query.model.criteria.ISubquery;
import skyglass.query.model.criteria.ITypedQuery;

public class JpaSubCriteriaBuilder<E0, S, S0> extends AbstractJpaCriteriaQueryBuilder<E0, S0> implements ISubQueryBuilder<E0, S0> {

    private final ISubquery<S0> subQuery;

    private final IRoot<E0> root;

    public JpaSubCriteriaBuilder(EntityManager entityManager, ICriteriaQuery<S> parentQuery, 
    		Class<E0> entityClass, Class<S0> selectClass) {
        super(entityManager.getCriteriaBuilder(), entityClass, selectClass,
        		JpaMetadataHelper.getInstanceForMetamodel(entityManager.getMetamodel()));
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

	@Override
	public <T> ITypedQuery<T> createTypedQuery(ICriteriaQuery<T> criteriaQuery) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypedQuery<E0> createQuery(String queryString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ISubQueryBuilder<E0, S0> createSubCriteriaBuilder(ICriteriaQuery<S0> parentQuery, Class<E0> subEntityClass,
			Class<S0> subSelectClass) {
		// TODO Auto-generated method stub
		return null;
	}

}
