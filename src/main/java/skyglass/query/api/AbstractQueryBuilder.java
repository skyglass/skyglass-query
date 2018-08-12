package skyglass.query.api;

import javax.persistence.EntityManager;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public abstract class AbstractQueryBuilder<E, S> implements IQueryBuilder<E> {
    
   protected PrivateQueryContext privateQueryContext;
   
   protected EntityManager entityManager;
   
   protected Class<E> rootClazz;
   
   public AbstractQueryBuilder(EntityManager entityManager) {
	   this.entityManager = entityManager;
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
	   return this.privateQueryContext;
   }
   
}
