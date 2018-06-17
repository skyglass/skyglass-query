package skyglass.query.model.criteria;

import java.util.List;

public interface ICriteriaQuery<S> {

    public void select(ISelection<? extends S> selection);

    public ICriteriaQuery<S> where(IExpression<Boolean> expression);

    public IPredicate getRestriction();

    public <T> IRoot<T> from(Class<T> clazz);

    public Class<S> getResultType();

    public ICriteriaQuery<S> orderBy(IOrder... order);

    public ICriteriaQuery<S> orderBy(List<IOrder> orderList);

    public <S2> ISubquery<S2> subquery(Class<S2> clazz);

}
