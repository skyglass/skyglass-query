package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Root;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoin;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPath;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IRoot;

public class JpaRoot<E> implements IRoot<E> {

    private final Root<E> root;

    public JpaRoot(Root<E> root) {
        this.root = root;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> IPath<T> get(String path) {
        return new JpaPath(root.get(path));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <S, U> IJoin<S, U> join(String alias, IJoinType joinType) {
        return new JpaJoin(root.join(alias, JpaJoinType.convert(joinType)));
    }

    @Override
    public IPredicate in(IExpression<?>... expressions) {
        return new JpaPredicate(root.in(JpaExpression.convert(expressions)));
    }

}
