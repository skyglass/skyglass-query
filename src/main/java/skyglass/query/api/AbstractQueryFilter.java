package skyglass.query.api;

import java.util.List;

import skyglass.data.filter.AbstractBaseDataFilter;
import skyglass.data.filter.CustomFilterResolver;
import skyglass.data.filter.IQueryFilter;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public abstract class AbstractQueryFilter<T, F> extends AbstractBaseDataFilter<T, F> 
	implements IQueryFilter<T, F> {

    public AbstractQueryFilter(Class<T> rootClazz, JunctionType junctionType, IQueryBuilder<T> queryBuilder,
            IFilterRequest request) {
        this(rootClazz, junctionType, IJoinType.INNER, queryBuilder, request);
    }

    public AbstractQueryFilter(Class<T> rootClazz, JunctionType junctionType, IJoinType joinType, IQueryBuilder<T> queryBuilder,
            IFilterRequest request) {
        super(rootClazz, junctionType, joinType, request, 
        		queryBuilder);
    }

    @Override
    protected void resolveCustomFilter(CustomFilterResolver filterResolver) {
    }

    @Override
    protected void applyOrder(List<OrderField> orderFields) {
    	getQueryProcessor().applyOrder(orderFields);
    }

    @Override
    protected void applyFilter(PrivateCompositeFilterItem rootFilterItem) {
    	getQueryProcessor().applyFilter(rootFilterItem);
    }


}
