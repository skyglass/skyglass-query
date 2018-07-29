package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.criteria.IJoinType;

public class CompositeFilter<F, E> implements IJoinResolver<F, E> {
    private List<CompositeFilter<F, E>> compositeChildren = new ArrayList<>();
    private List<AtomicFilter> atomicChildren = new ArrayList<>();
    private boolean isAnd;
    private F filter;
    private IJoinResolver<F, E> parent;
    private PrivateQueryContext queryContext;
    private int subQueryCounter = 1;

    public CompositeFilter(F filter, PrivateQueryContext queryContext, IJoinResolver<F, E> parent, boolean isAnd) {
    	this.filter = filter;
        this.queryContext = queryContext;
        this.parent = parent;
        this.isAnd = isAnd;
    }

    public boolean isAnd() {
        return isAnd;
    }

    public List<CompositeFilter<F, E>> getCompositeChildren() {
        return compositeChildren;
    }

    public List<AtomicFilter> getAtomicChildren() {
        return atomicChildren;
    }

    @Override
    public IJoinResolver<F, E> equals(String propertyName, Object value) {
        return addAtomic(FilterType.Equals, propertyName, value);
    }

    @Override
    public IJoinResolver<F, E> notEquals(String propertyName, Object value) {
        return addAtomic(FilterType.NotEquals, propertyName, value);
    }

    @Override
    public IJoinResolver<F, E> eqProperty(String propertyName, String otherPropertyName) {
        return addAtomic(FilterType.EqualsProp, propertyName, otherPropertyName);
    }

    @Override
    public IJoinResolver<F, E> and() {
        return addComposite(true);
    }

    @Override
    public IJoinResolver<F, E> or() {
        return addComposite(false);
    }

    @Override
    public IJoinResolver<F, E> done() {
        return parent;
    }

    private IJoinResolver<F, E> addComposite(boolean isAnd) {
        CompositeFilter<F, E> child = new CompositeFilter<F, E>(filter, queryContext, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private IJoinResolver<F, E> addAtomic(FilterType filterType, String propertyName, Object value) {
        addAtomic(new AtomicFilter(filterType, propertyName, value));
        return this;
    }

    private IJoinResolver<F, E> addAtomic(AtomicFilter atomicFilter) {
        atomicChildren.add(atomicFilter);
        return this;
    }

    @Override
    public F resolve() {
        return parent.resolve();
    }

    @Override
    public F invert() {
        return parent.invert();
    }

	@Override
    public IJoinResolver<F, E> addLeftJoin(String fieldName, String alias) {
    	return new CustomJoin<F, E>(filter, this, queryContext, fieldName, alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<F, E> addJoin(String fieldName, String alias) {
        return new CustomJoin<F, E>(filter, this, queryContext, fieldName, alias, IJoinType.INNER);
    }

	
    private String getNextSubQueryAlias() {
        return "cf" + new Integer(subQueryCounter++).toString();
    }

}
