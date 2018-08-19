package skyglass.query.api.jpa;

import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.api.AbstractQueryBuilder;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.IQueryProcessor;
import skyglass.query.model.query.JpaQueryProcessor;

public abstract class AbstractJpaQueryBuilder<E, S> extends AbstractQueryBuilder<E, S> {
	
   public AbstractJpaQueryBuilder(MetadataHelper metadataHelper) {
       super(metadataHelper);
   }   
   
	@Override
	public IQueryProcessor setQueryProcessor(MetadataHelper metadataHelper, 
			PrivateQueryContext privateQueryContext) {
		return JpaQueryProcessor.getInstance(metadataHelper, privateQueryContext);
	}
   

    
}
