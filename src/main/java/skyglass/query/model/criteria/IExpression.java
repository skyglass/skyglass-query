package skyglass.query.model.criteria;

public interface IExpression<T> extends ISelection<T> {

    public IPredicate in(IExpression<?>... expression);

}
