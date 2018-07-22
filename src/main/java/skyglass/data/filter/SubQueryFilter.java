package skyglass.data.filter;

import java.util.ArrayList;
import java.util.List;

import skyglass.query.model.criteria.IJoinType;

public class SubQueryFilter<T, SUB> extends CompositeFilter<T> {
    private Class<SUB> entityClass;
    private SubQueryType subQueryType;
    private PropertyType propertyType;
    private List<SubQueryProperty> properties = new ArrayList<>();
    private IJoinType joinType;
    private String alias;

    public SubQueryFilter(PrivateQueryContext queryContext, String alias, Class<SUB> entityClass, SubQueryType subQueryType, IJoinType joinType,
            IJoinResolver<T> parent, boolean isAnd) {
        this(queryContext, alias, entityClass, subQueryType, PropertyType.Property, joinType, parent, isAnd);
    }

    public SubQueryFilter(PrivateQueryContext queryContext, String alias, Class<SUB> entityClass, SubQueryType subQueryType, PropertyType propertyType,
            IJoinType joinType, IJoinResolver<T> parent, boolean isAnd) {
        super(new PrivateQueryContext(queryContext, queryContext.isDisjunction()), parent, isAnd);
        this.entityClass = entityClass;
        this.alias = alias;
        this.subQueryType = subQueryType;
        this.propertyType = propertyType;
        this.joinType = joinType;
    }

    public SubQueryFilter<T, SUB> addProjectionCorrelatedProperty(String name, String parentName, boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionCorrelatedProperty(String name, String parentName, IJoinType joinType,
            boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionCorrelatedProperty(PropertyType type, String name, String parentName,
            boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionCorrelatedProperty(PropertyType type, String name, String parentName,
            IJoinType joinType, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionProperty(String name, String parentName, boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionProperty(String name, String parentName, IJoinType joinType,
            boolean distinct) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionProperty(PropertyType type, String name, String parentName,
            boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProjectionProperty(PropertyType type, String name, String parentName,
            IJoinType joinType, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, false, true, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addCorrelatedProperty(String name, String parentName) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB> addCorrelatedProperty(String name, String parentName, IJoinType joinType) {
        properties.add(new SubQueryProperty(propertyType, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB> addCorrelatedProperty(PropertyType type, String name, String parentName) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB> addCorrelatedProperty(PropertyType type, String name, String parentName,
            IJoinType joinType) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, true, false, false));
        return this;
    }

    public SubQueryFilter<T, SUB> addProperty(String name, String parentName, boolean correlated, boolean projection,
            boolean distinct) {
        properties
                .add(new SubQueryProperty(propertyType, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProperty(String name, String parentName, IJoinType joinType, boolean correlated,
            boolean projection, boolean distinct) {
        properties
                .add(new SubQueryProperty(propertyType, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProperty(PropertyType type, String name, String parentName, boolean correlated,
            boolean projection, boolean distinct) {
        properties.add(new SubQueryProperty(type, name, parentName, joinType, correlated, projection, distinct));
        return this;
    }

    public SubQueryFilter<T, SUB> addProperty(PropertyType type, String name, String parentName, IJoinType joinType,
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

    public IJoinResolver<T> done() {
        return this;
    }

}
