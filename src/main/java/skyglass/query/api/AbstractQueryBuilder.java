package skyglass.query.api;

import java.util.function.Supplier;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.IQueryProcessor;

public abstract class AbstractQueryBuilder<E, S> implements IQueryBuilder<E> {
	
   private IQueryProcessor queryProcessor;
	   
   private MetadataHelper metadataHelper; 
    
   protected PrivateQueryContext privateQueryContext;
   
   protected Class<E> rootClazz;
   
   public AbstractQueryBuilder(MetadataHelper metadataHelper) {
	   this.metadataHelper = metadataHelper;
   }
   
   @Override
   public PrivateQueryContext getPrivateQueryContext() {
	   return privateQueryContext;
   }
   
   @Override
   public PrivateQueryContext setPrivateQueryContext(JunctionType junctionType, Class<E> rootClazz, IJoinType joinType) {
	   this.privateQueryContext = new PrivateQueryContext(
			   junctionType, this, rootClazz, joinType);
	   this.rootClazz = rootClazz;
	   this.queryProcessor = setQueryProcessor(metadataHelper, privateQueryContext);
	   return this.privateQueryContext;
   }
   
	@Override
	public IQueryProcessor getQueryProcessor() {
		return queryProcessor;
	}
  
	@Override
	public String generateQueryString() {
		return queryProcessor.generateQueryString();
	}
	
	@Override
	public String generateCountQueryString() {
		return queryProcessor.generateCountQueryString();
	}
  
   @Override
   public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName) {
       return () -> metadataHelper.isNumericField(rootClass, propertyName);
   }

   @Override
   public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection) {
       return () -> metadataHelper.convertObject(rootClass, property, value, isCollection);
   }
   
	@Override
	public boolean isCollection(Class<?> rootClass, String path) {
		return metadataHelper.isCollection(rootClass, path);
	}

	@Override
	public boolean isEntity(Class<?> rootClass, String path) {
		return metadataHelper.isEntity(rootClass, path);
	}

	@Override
	public boolean isId(Class<?> rootClass, String path) {
		return metadataHelper.isId(rootClass, path);
	}
   
}
