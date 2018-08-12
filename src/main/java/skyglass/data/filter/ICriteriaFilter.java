package skyglass.data.filter;

import java.util.function.Supplier;

import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.ICriteriaQueryBuilder;

public interface ICriteriaFilter<T, F> extends IBaseDataFilter<T, F> {
	
    public ICriteriaQueryBuilder<T, T> getQueryBuilder();
    
    public IPredicate createAtomicFilter(
    		String fieldName, FilterType filterType, Supplier<Object> filterValueResolver);

}
