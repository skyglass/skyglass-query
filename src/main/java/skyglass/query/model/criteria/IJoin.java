package skyglass.query.model.criteria;

public interface IJoin<T, E> {

    public <S> IPath<S> get(String path);

    public <S, U> IJoin<S, U> join(String alias, IJoinType joinType);

    public <S, U> IJoin<S, U> on(IExpression<Boolean> onClause);

}
