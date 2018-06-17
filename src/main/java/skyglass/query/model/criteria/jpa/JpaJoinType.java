package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.JoinType;

import skyglass.query.model.criteria.IJoinType;

public class JpaJoinType {

    public static JoinType convert(IJoinType joinType) {
        if (joinType == IJoinType.LEFT) {
            return JoinType.LEFT;
        }
        if (joinType == IJoinType.RIGHT) {
            return JoinType.LEFT;
        }
        return JoinType.INNER;
    }

}
