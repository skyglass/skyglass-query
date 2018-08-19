package skyglass.query.api.jpa;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.AbstractQueryFilter;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public class JpaQueryFilter<T> extends AbstractQueryFilter<T, JpaQueryFilter<T>> {
	
    public JpaQueryFilter(Class<T> rootClazz, JunctionType junctionType, IQueryBuilder<T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

	public JpaQueryFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType,
			IQueryBuilder<T> queryBuilder, IFilterRequest request) {
		super(rootClazz, junctionType, joinType, queryBuilder, request);
	}

	@Override
	protected JpaQueryFilter<T> self() {
		return this;
	}

}
