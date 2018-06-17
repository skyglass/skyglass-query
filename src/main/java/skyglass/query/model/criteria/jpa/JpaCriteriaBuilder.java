package skyglass.query.model.criteria.jpa;

import javax.persistence.EntityManager;

public class JpaCriteriaBuilder<E, S> extends AbstractJpaQueryBuilder<E, S> {

	public JpaCriteriaBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
		super(entityManager, entityClass, selectClass);
	}

}
