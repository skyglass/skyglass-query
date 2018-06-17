package skyglass.data.filter;

import skyglass.query.model.criteria.IQueryBuilder;

public interface CustomJpaFilterResolver<E, S> extends CustomFilterResolver {

    public void addCustomFilter(IQueryBuilder<E, S> queryBuilder);

}
