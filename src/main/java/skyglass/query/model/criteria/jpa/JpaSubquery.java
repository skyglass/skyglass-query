package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Subquery;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISubquery;

public class JpaSubquery<S> implements ISubquery<S> {

    private Subquery<S> subquery;

    public JpaSubquery(Subquery<S> subquery) {
        this.subquery = subquery;
    }

    public Subquery<S> getSubquery() {
        return subquery;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <E> IRoot<E> from(Class<E> clazz) {
        return new JpaRoot(subquery.from(clazz));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ISubquery<S> groupBy(IExpression<?> expression) {
        return new JpaSubquery(subquery.groupBy(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ISubquery<S> select(IExpression<S> expression) {
        return new JpaSubquery(subquery.select(((JpaExpression) expression).getExpression()));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ISubquery<S> distinct(boolean distinct) {
        return new JpaSubquery(subquery.distinct(distinct));
    }

    @Override
    public IPredicate in(IExpression<?>... expressions) {
        return new JpaPredicate(subquery.in(JpaExpression.convert(expressions)));
    }

}
