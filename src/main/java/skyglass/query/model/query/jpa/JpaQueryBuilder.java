package skyglass.query.model.query.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

public class JpaQueryBuilder<E, S> extends AbstractJpaQueryBuilder<E, S> {

    protected Class<E> entityClass;
    
    protected Class<S> selectClass;
    
    protected List<Object> paramList = new ArrayList<Object>();
    
	public JpaQueryBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
		super(entityManager);
	}
	
}
