package skyglass.query.api;

import skyglass.query.criteria.api.AbstractCriteriaQueryBuilder;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.ISubQueryBuilder;

public abstract class AbstractSubQueryBuilder<E, S> extends AbstractCriteriaQueryBuilder<E, S>
        implements ISubQueryBuilder<E, S> {

	public AbstractSubQueryBuilder(MetadataHelper metadataHelper) {
		super(metadataHelper);
	}

}
