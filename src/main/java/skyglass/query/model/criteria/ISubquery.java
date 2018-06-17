package skyglass.query.model.criteria;

public interface ISubquery<S> extends IExpression<S> {

    public <E> IRoot<E> from(Class<E> clazz);

    public ISubquery<S> groupBy(IExpression<?> expression);

    public ISubquery<S> select(IExpression<S> expression);

    public ISubquery<S> distinct(boolean distinct);

}
