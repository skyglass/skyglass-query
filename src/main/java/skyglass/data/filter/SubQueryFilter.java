package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.criteria.IJoinType;

public class SubQueryFilter<T, SUB, F> extends CompositeFilter<T, F> {
    private Class<SUB> entityClass;
    private SubQueryType subQueryType;
    private PropertyType propertyType;
    private List<SubQueryProperty> properties = new ArrayList<>();
    private IJoinType joinType;

    public SubQueryFilter(Class<SUB> entityClass, SubQueryType subQueryType, IJoinType joinType,
            IJoinResolver<T, F> joinResolver, IJoinResolver<T, F> parent, boolean isAnd) {
        this(entityClass, subQueryType, PropertyType.Property, joinType, joinResolver, parent, isAnd);
    }

    public SubQueryFilter(Class<SUB> entityClass, SubQueryType subQueryType, PropertyType propertyType,
            IJoinType joinType, IJoinResolver<T, F> joinResolver, IJoinResolver<T, F> parent, boolean isAnd) {
        super(joinResolver, parent, isAnd);
        this.entityClass = entityClass;
        this.subQueryType = subQueryType;
        this.propertyType = propertyType;
        this.joinType = joinType;
    }

    public SubQueryFilter<T, SUB, F> addProjectionCorrelatedProperty(String name, String parentName, boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionCorrelatedProperty(String name, String parentName, IJoinType joinType,
            boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionCorrelatedProperty(PropertyType type, String name, String parentName,
            boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionCorrelatedProperty(PropertyType type, String name, String parentName,
            IJoinType joinType, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionProperty(String name, String parentName, boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionProperty(String name, String parentName, IJoinType joinType,
            boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionProperty(PropertyType type, String name, String parentName,
            boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProjectionProperty(PropertyType type, String name, String parentName,
            IJoinType joinType, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addCorrelatedProperty(String name, String parentName) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addCorrelatedProperty(String name, String parentName, IJoinType joinType) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addCorrelatedProperty(PropertyType type, String name, String parentName) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addCorrelatedProperty(PropertyType type, String name, String parentName,
            IJoinType joinType) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProperty(String name, String parentName, boolean correlated, boolean projection,
            boolean distinct) {
        properties
                .add(new SubQueryProperty(propertyType, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProperty(String name, String parentName, IJoinType joinType, boolean correlated,
            boolean projection, boolean distinct) {
        properties
                .add(new SubQueryProperty(propertyType, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProperty(PropertyType type, String name, String parentName, boolean correlated,
            boolean projection, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB, F> addProperty(PropertyType type, String name, String parentName, IJoinType joinType,
            boolean correlated, boolean projection, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public List<SubQueryProperty> getProperties() {
        return properties;
    }

    public Class<SUB> getEntityClass() {
        return entityClass;
    }

    public SubQueryType getSubQueryType() {
        return subQueryType;
    }

    public IJoinType getJoinType() {
        return joinType;
    }

    public IJoinResolver<T, F> done() {
        return this;
    }

}
