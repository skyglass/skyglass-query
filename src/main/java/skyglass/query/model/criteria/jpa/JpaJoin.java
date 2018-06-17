package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Join;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoin;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPath;

public class JpaJoin<T, E> implements IJoin<T, E> {

    private Join<T, E> join;

    public JpaJoin(Join<T, E> join) {
        this.join = join;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public <S> IPath<S> get(String path) {
        return new JpaPath(join.get(path));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <S, U> IJoin<S, U> join(String alias, IJoinType joinType) {
        return new JpaJoin(join.join(alias, JpaJoinType.convert(joinType)));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <S, U> IJoin<S, U> on(IExpression<Boolean> onClause) {
        return new JpaJoin(join.on(((JpaExpression) onClause).getExpression()));
    }

}
