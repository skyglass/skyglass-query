package skyglass.query.model.criteria.jpa;

import javax.persistence.EntityManager;

import skyglass.query.metadata.jpa.JpaMetadataHelper;
import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.ISubQueryBuilder;
import skyglass.query.model.criteria.ITypedQuery;

public class JpaCriteriaBuilder<E, S> extends AbstractJpaCriteriaQueryBuilder<E, S> {
	
	private EntityManager entityManager;

	public JpaCriteriaBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
		super(entityManager.getCriteriaBuilder(), 
				entityClass, selectClass, 
				JpaMetadataHelper.getInstanceForMetamodel(entityManager.getMetamodel()));
		this.entityManager = entityManager;
	}

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> ITypedQuery<T> createTypedQuery(ICriteriaQuery<T> criteriaQuery) {
        return new JpaTypedQuery(entityManager.createQuery(((JpaCriteriaQuery) criteriaQuery).getCriteriaQuery()));
    }
    
	@Override
	public ITypedQuery<E> createQuery(String queryString) {
       return new JpaTypedQuery<E>(entityManager.createQuery(queryString, rootClazz));
	}

	@Override
	public ISubQueryBuilder<E, S> createSubCriteriaBuilder(ICriteriaQuery<S> parentQuery, Class<E> subEntityClass,
			Class<S> subSelectClass) {
        return new JpaSubCriteriaBuilder<E, S, S>(entityManager, parentQuery, subEntityClass, subSelectClass);
	}

}
