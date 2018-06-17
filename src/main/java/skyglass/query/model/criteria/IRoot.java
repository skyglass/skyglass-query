package skyglass.query.model.criteria;

public interface IRoot<E> extends IExpression<E> {

    public <T> IPath<T> get(String path);

    public <S, U> IJoin<S, U> join(String alias, IJoinType joinType);

}
