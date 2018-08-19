package skyglass.query.api.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;

import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.metadata.jpa.JpaMetadataHelper;
import skyglass.query.model.criteria.IQueryProcessor;
import skyglass.query.model.criteria.ITypedQuery;
import skyglass.query.model.criteria.jpa.JpaTypedQuery;

public class JpaQueryBuilder<E, S> extends AbstractJpaQueryBuilder<E, S> {

    protected Class<E> entityClass;
    
    protected Class<S> selectClass;
    
    protected List<Object> paramList = new ArrayList<Object>();
    
    private EntityManager entityManager;
    
	public JpaQueryBuilder(EntityManager entityManager, Class<E> entityClass, Class<S> selectClass) {
		super(JpaMetadataHelper.getInstanceForMetamodel(entityManager.getMetamodel()));
		this.entityManager = entityManager;
	}
	
	@Override
	public ITypedQuery<E> createQuery(String queryString) {
       return new JpaTypedQuery<E>(entityManager.createQuery(queryString, rootClazz));
	}
	
	
	@Override
	public <T> ITypedQuery<T> createQuery(Class<T> clazz) {
	   return new JpaTypedQuery<T>(entityManager.createQuery(generateQueryString(), clazz));
	}

	@Override
	public ITypedQuery<Long> createCountQuery() {
		   return new JpaTypedQuery<Long>(entityManager.createQuery(generateCountQueryString(), Long.class));
	}

}
