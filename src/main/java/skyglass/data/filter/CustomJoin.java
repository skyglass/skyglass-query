package skyglass.data.filter;

import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public class CustomJoin<F, E> implements IJoinResolver<F, E> {

    private IJoinType joinType;

    private PrivateQueryContext queryContext;

    private String fieldName;

    private String alias;

    private CompositeFilter<F, E> compositeFilter;

    private int subQueryCounter = 1;
    
    private F filter;
    
    private IJoinResolver<F, E> parent;

    public CustomJoin(F filter, IJoinResolver<F, E> parent, 
    		PrivateQueryContext queryContext, String fieldName, String alias, IJoinType joinType) {
    	this.filter = filter;
        this.parent = parent;
    	this.joinType = joinType;
        this.queryContext = queryContext;
        this.fieldName = fieldName;
        this.alias = alias;
        this.compositeFilter = new CompositeFilter<F, E>(filter, queryContext, this, true);
    }

    @Override
    public IJoinResolver<F, E> equals(String propertyName, Object value) {
        return compositeFilter.equals(propertyName, value);
    }

    @Override
    public IJoinResolver<F, E> notEquals(String propertyName, Object value) {
        return compositeFilter.notEquals(propertyName, value);
    }

    @Override
    public IJoinResolver<F, E> eqProperty(String propertyName, String otherPropertyName) {
        return null;
    }

    @Override
    public IJoinResolver<F, E> and() {
        return compositeFilter.and();
    }

    @Override
    public IJoinResolver<F, E> or() {
        return compositeFilter.or();
    }
    
    @Override
    public IJoinResolver<F, E> done() {
    	if (parent == null) {
    		throw new UnsupportedOperationException("done cannot be called on the root join resolver, please call resolve instead");
    	}
        return parent;
    }

    private PrivateQueryContext createQueryContext(boolean isAnd) {
        return new PrivateQueryContext(queryContext, isAnd);
    }
    
    private PrivateFilterItem createAtomicFilterItem(AtomicFilter atomicFilter) {
        String propertyName = IQueryBuilder.resolvePropertyName(alias + "." + atomicFilter.propertyName);
        return queryContext.createFilterItem(propertyName, atomicFilter.filterType, atomicFilter.value);
    }

    private PrivateCompositeFilterItem createCompositeFilterItem(CompositeFilter<F, E> compositeFilter) {
        for (CompositeFilter<F, E> child : compositeFilter.getCompositeChildren()) {
            queryContext.addRootChild(
            		createCompositeFilterItem(child));
        }
        for (AtomicFilter atomicFilter : compositeFilter.getAtomicChildren()) {
        	queryContext.addRootChild(createAtomicFilterItem(atomicFilter));
        }
        return queryContext.getRootFilterItem();
    }
    
    @Override
    public F resolve() {
        createCompositeFilterItem(compositeFilter);
        return filter;
    }

    @Override
    public F invert() {
        queryContext.createFilterItem(alias + ".id", FilterType.IsNull, null);
        return filter;
    }

    private String getNextSubQueryAlias() {
        return "subQuery" + new Integer(subQueryCounter++).toString();
    }
    
	@Override
    public IJoinResolver<F, E> addLeftJoin(String fieldName, String alias) {
        return new CustomJoin<F, E>(filter, this, queryContext, fieldName, alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<F, E> addJoin(String fieldName, String alias) {
        return new CustomJoin<F, E>(filter, this, queryContext, fieldName, alias, IJoinType.INNER);
    }

}
