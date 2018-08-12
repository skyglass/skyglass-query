package skyglass.query.model.criteria.jpa;

import java.util.function.Supplier;

import javax.persistence.EntityManager;

import skyglass.query.model.criteria.ITypedQuery;

public class JpaCriteriaBuilder<E, S> extends AbstractJpaCriteriaQueryBuilder<E, S> {

	public JpaCriteriaBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
		super(entityManager, entityClass, selectClass);
	}

}
