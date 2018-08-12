package skyglass.query.api;

import skyglass.query.model.criteria.ISubQueryBuilder;

public abstract class AbstractSubQueryBuilder<E, S> extends AbstractCriteriaQueryBuilder<E, S>
        implements ISubQueryBuilder<E, S> {

}
