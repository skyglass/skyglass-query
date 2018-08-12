package skyglass.query.model.query.jpa;

import java.util.function.Supplier;

import javax.persistence.EntityManager;

import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.api.AbstractQueryBuilder;
import skyglass.query.metadata.jpa.JpaMetadataHelper;
import skyglass.query.model.criteria.ITypedQuery;
import skyglass.query.model.criteria.jpa.JpaTypedQuery;
import skyglass.query.model.query.JpaQueryProcessor;

public abstract class AbstractJpaQueryBuilder<E, S> extends AbstractQueryBuilder<E, S> {
	
   private JpaQueryProcessor queryProcessor;
    
   protected PrivateQueryContext privateQueryContext;
   
   protected JpaMetadataHelper jpaMetadataHelper;  
   
   private Class<E> rootClazz;
   
   public AbstractJpaQueryBuilder(EntityManager entityManager) {
	   super(entityManager);
       this.jpaMetadataHelper = JpaMetadataHelper.getInstanceForMetamodel(entityManager.getMetamodel());
	   this.queryProcessor = JpaQueryProcessor.getInstance(jpaMetadataHelper, privateQueryContext);
   }
   
	@Override
	public String generateQueryString() {
		return queryProcessor.generateQueryString();
	}
   
	@Override
	public ITypedQuery<E> createQuery(String queryString) {
       return new JpaTypedQuery<E>(entityManager.createQuery(queryString, rootClazz));
	}
	
    @Override
    public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName) {
        return () -> jpaMetadataHelper.isNumericField(rootClass, propertyName);
    }

    @Override
    public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection) {
        return () -> jpaMetadataHelper.convertObject(rootClass, property, value, isCollection);
    }
    
	@Override
	public boolean isCollection(Class<?> rootClass, String path) {
		return jpaMetadataHelper.isCollection(rootClass, path);
	}

	@Override
	public boolean isEntity(Class<?> rootClass, String path) {
		return jpaMetadataHelper.isEntity(rootClass, path);
	}

	@Override
	public boolean isId(Class<?> rootClass, String path) {
		return jpaMetadataHelper.isId(rootClass, path);
	}
    
}
