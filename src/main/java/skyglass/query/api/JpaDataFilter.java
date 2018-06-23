package skyglass.query.api;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public class JpaDataFilter<T> extends AbstractDataFilter<T, JpaDataFilter<T>> {
	
    public JpaDataFilter(Class<T> rootClazz, JunctionType junctionType, IQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

	public JpaDataFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType,
			IQueryBuilder<T, T> queryBuilder, IFilterRequest request) {
		super(rootClazz, junctionType, joinType, queryBuilder, request);
	}

	@Override
	protected JpaDataFilter<T> self() {
		return this;
	}

}
