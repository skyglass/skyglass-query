package skyglass.data.filter;

import skyglass.query.model.criteria.IJoinType;

public class SubQueryProperty {

    private PropertyType type;

    private String name;

    private String parentName;

    private boolean correlated;

    private boolean projection;

    private boolean distinct;

    private IJoinType joinType;

    public SubQueryProperty(PropertyType type, String name, String parentName, IJoinType joinType, boolean correlated,
            boolean projection, boolean distinct) {
        this.type = type;
        this.name = name;
        this.parentName = parentName;
        this.joinType = joinType;
        this.correlated = correlated;
        this.projection = projection;
        this.distinct = distinct;
    }

    public PropertyType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getParentName() {
        return parentName;
    }

    public boolean isCorrelated() {
        return correlated;
    }

    public boolean isProjection() {
        return projection;
    }

    public boolean isDistinct() {
        return distinct;
    }

    public IJoinType getJoinType() {
        return joinType;
    }

}
