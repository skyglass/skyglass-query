package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.criteria.IJoinType;

public class CompositeFilter<T> implements IJoinResolver<T> {
    private List<CompositeFilter<T>> compositeChildren = new ArrayList<>();
    private List<AtomicFilter> atomicChildren = new ArrayList<>();
    private boolean isAnd;
    private IJoinResolver<T> parent;
    private PrivateQueryContext queryContext;
    private int subQueryCounter = 1;

    public CompositeFilter(PrivateQueryContext queryContext, IJoinResolver<T> parent, boolean isAnd) {
        this.queryContext = queryContext;
        this.parent = parent;
        this.isAnd = isAnd;
    }

    public boolean isAnd() {
        return isAnd;
    }

    public List<CompositeFilter<T>> getCompositeChildren() {
        return compositeChildren;
    }

    public List<AtomicFilter> getAtomicChildren() {
        return atomicChildren;
    }

    @Override
    public IJoinResolver<T> equals(String propertyName, Object value) {
        return addAtomic(FilterType.Equals, propertyName, value);
    }

    @Override
    public IJoinResolver<T> notEquals(String propertyName, Object value) {
        return addAtomic(FilterType.NotEquals, propertyName, value);
    }

    @Override
    public IJoinResolver<T> eqProperty(String propertyName, String otherPropertyName) {
        return addAtomic(FilterType.EqualsProp, propertyName, otherPropertyName);
    }

    @Override
    public IJoinResolver<T> and() {
        return addComposite(true);
    }

    @Override
    public IJoinResolver<T> or() {
        return addComposite(false);
    }

    @Override
    public IJoinResolver<T> done() {
        return parent;
    }

    private IJoinResolver<T> addComposite(boolean isAnd) {
        CompositeFilter<T> child = new CompositeFilter<T>(queryContext, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addIdExistsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addIdNotExistsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyExistsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addIdInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addIdNotInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEq,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEq,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEq,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEq,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyInSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotInSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertyNotInSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesEqualsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEq, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesNotEqualsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEq, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropIn, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesNotInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotIn, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesExistSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropEx, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB> addPropertiesNotExistSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB> child = new SubQueryFilter<T, SUB>(queryContext, getNextSubQueryAlias(), clazz, SubQueryType.PropNotEx, IJoinType.INNER,
                this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private IJoinResolver<T> addAtomic(FilterType filterType, String propertyName, Object value) {
        addAtomic(new AtomicFilter(filterType, propertyName, value));
        return this;
    }

    private IJoinResolver<T> addAtomic(AtomicFilter atomicFilter) {
        atomicChildren.add(atomicFilter);
        return this;
    }

    @Override
    public IJoinResolver<T> resolve() {
        return parent.resolve();
    }

    @Override
    public IJoinResolver<T> resolve(String resolveAlias) {
        return parent.resolve(resolveAlias);
    }

    @Override
    public IJoinResolver<T> invert() {
        return parent.invert();
    }

    @Override
    public IJoinResolver<T> invert(String invertAlias) {
        return parent.invert(invertAlias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> idExistsSubQuery(Class<SUB> clazz) {
        return addIdExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> idNotExistsSubQuery(Class<SUB> clazz) {
        return addIdNotExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyExistsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return addPropertyNotExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> idInSubQuery(Class<SUB> clazz) {
        return addIdInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> idNotInSubQuery(Class<SUB> clazz) {
        return addIdNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyInSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotInSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return addPropertyNotEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyNotInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesEqualsSubQuery(Class<SUB> clazz) {
        return addPropertiesEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesNotEqualsSubQuery(Class<SUB> clazz) {
        return addPropertiesNotEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesInSubQuery(Class<SUB> clazz) {
        return addPropertiesInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesNotInSubQuery(Class<SUB> clazz) {
        return addPropertiesNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesExistSubQuery(Class<SUB> clazz) {
        return addPropertiesExistSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB> propertiesNotExistSubQuery(Class<SUB> clazz) {
        return addPropertiesNotExistSubQuery(clazz);
    }

	@Override
    public IJoinResolver<T> addLeftJoin(String alias) {
    	return new CustomJoin<T, T>(this, queryContext, alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<T> addJoin(String alias) {
        return new CustomJoin<T, T>(this, queryContext, alias, IJoinType.INNER);
    }
    
	@Override
    public IJoinResolver<T> addSubQueryLeftJoin(String alias) {
        return new CustomJoin<T, T>(this, 
        		new PrivateQueryContext(queryContext, queryContext.isDisjunction()), alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<T> addSubQueryJoin(String alias) {
        return new CustomJoin<T, T>(this,
        		new PrivateQueryContext(queryContext, queryContext.isDisjunction()), alias, IJoinType.INNER);
    }
	
    private String getNextSubQueryAlias() {
        return "cf" + new Integer(subQueryCounter++).toString();
    }

}
