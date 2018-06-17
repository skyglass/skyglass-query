package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.criteria.IJoinType;

public class CompositeFilter<T, F> implements IJoinResolver<T, F> {
    private List<CompositeFilter<T, F>> compositeChildren = new ArrayList<>();
    private List<AtomicFilter> atomicChildren = new ArrayList<>();
    private boolean isAnd;
    private IJoinResolver<T, F> parent;
    private IJoinResolver<T, F> joinResolver;

    public CompositeFilter(IJoinResolver<T, F> joinResolver, IJoinResolver<T, F> parent, boolean isAnd) {
        this.joinResolver = joinResolver;
        this.parent = parent;
        this.isAnd = isAnd;
    }

    public boolean isAnd() {
        return isAnd;
    }

    public List<CompositeFilter<T, F>> getCompositeChildren() {
        return compositeChildren;
    }

    public List<AtomicFilter> getAtomicChildren() {
        return atomicChildren;
    }

    @Override
    public IJoinResolver<T, F> equals(String propertyName, Object value) {
        return addAtomic(FilterType.EQ, propertyName, value);
    }

    @Override
    public IJoinResolver<T, F> notEquals(String propertyName, Object value) {
        return addAtomic(FilterType.NE, propertyName, value);
    }

    @Override
    public IJoinResolver<T, F> eqProperty(String propertyName, String otherPropertyName) {
        return addAtomic(FilterType.EQPR, propertyName, otherPropertyName);
    }

    @Override
    public IJoinResolver<T, F> and() {
        return addComposite(true);
    }

    @Override
    public IJoinResolver<T, F> or() {
        return addComposite(false);
    }

    @Override
    public IJoinResolver<T, F> done() {
        return parent;
    }

    private IJoinResolver<T, F> addComposite(boolean isAnd) {
        CompositeFilter<T, F> child = new CompositeFilter<T, F>(joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addIdExistsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addIdNotExistsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyExistsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEx,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addIdInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addIdNotInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty("id", "id", true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEq,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEq,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEq,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEq,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyInSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotInSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(parentAlias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertyNotInSubQuery(Class<SUB> clazz, String alias) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotIn,
                PropertyType.Property, IJoinType.INNER, joinResolver, this, isAnd);
        child.addProperty(alias, alias, true, true, false);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesEqualsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEq, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesNotEqualsSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEq, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropIn, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesNotInSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotIn, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesExistSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropEx, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private <SUB> SubQueryFilter<T, SUB, F> addPropertiesNotExistSubQuery(Class<SUB> clazz) {
        SubQueryFilter<T, SUB, F> child = new SubQueryFilter<T, SUB, F>(clazz, SubQueryType.PropNotEx, IJoinType.INNER,
                joinResolver, this, isAnd);
        compositeChildren.add(child);
        return child;
    }

    private IJoinResolver<T, F> addAtomic(FilterType filterType, String propertyName, Object value) {
        addAtomic(new AtomicFilter(filterType, propertyName, value));
        return this;
    }

    private IJoinResolver<T, F> addAtomic(AtomicFilter atomicFilter) {
        atomicChildren.add(atomicFilter);
        return this;
    }

    @Override
    public F resolve() {
        return joinResolver.resolve();
    }

    @Override
    public F resolve(String resolveAlias) {
        return joinResolver.resolve(resolveAlias);
    }

    @Override
    public F invert() {
        return joinResolver.invert();
    }

    @Override
    public F invert(String invertAlias) {
        return joinResolver.invert(invertAlias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> idExistsSubQuery(Class<SUB> clazz) {
        return addIdExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> idNotExistsSubQuery(Class<SUB> clazz) {
        return addIdNotExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return addPropertyNotExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> idInSubQuery(Class<SUB> clazz) {
        return addIdInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> idNotInSubQuery(Class<SUB> clazz) {
        return addIdNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyInSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String alias) {
        return addPropertyNotInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return addPropertyNotEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return addPropertyNotInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesEqualsSubQuery(Class<SUB> clazz) {
        return addPropertiesEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotEqualsSubQuery(Class<SUB> clazz) {
        return addPropertiesNotEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesInSubQuery(Class<SUB> clazz) {
        return addPropertiesInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotInSubQuery(Class<SUB> clazz) {
        return addPropertiesNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesExistSubQuery(Class<SUB> clazz) {
        return addPropertiesExistSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotExistSubQuery(Class<SUB> clazz) {
        return addPropertiesNotExistSubQuery(clazz);
    }

}
