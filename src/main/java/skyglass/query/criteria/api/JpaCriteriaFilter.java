package skyglass.query.criteria.api;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.ICriteriaQueryBuilder;

public class JpaCriteriaFilter<T> extends AbstractCriteriaFilter<T, JpaCriteriaFilter<T>> {
	
    public JpaCriteriaFilter(Class<T> rootClazz, JunctionType junctionType, ICriteriaQueryBuilder<T, T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

	public JpaCriteriaFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType,
			ICriteriaQueryBuilder<T, T> queryBuilder, IFilterRequest request) {
		super(rootClazz, junctionType, joinType, queryBuilder, request);
	}

	@Override
	protected JpaCriteriaFilter<T> self() {
		return this;
	}

}
