package skyglass.data.filter;

import skyglass.query.model.criteria.ICriteriaQueryBuilder;

public interface CustomJpaFilterResolver<E, S> extends CustomFilterResolver {

    public void addCustomFilter(ICriteriaQueryBuilder<E, S> queryBuilder);

}
