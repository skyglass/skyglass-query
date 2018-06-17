package skyglass.query.model.criteria.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Selection;

import skyglass.query.model.criteria.ICriteriaQuery;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IOrder;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IRoot;
import skyglass.query.model.criteria.ISelection;
import skyglass.query.model.criteria.ISubquery;

public class JpaCriteriaQuery<S> implements ICriteriaQuery<S> {

    private CriteriaQuery<S> criteriaQuery;

    public JpaCriteriaQuery(CriteriaQuery<S> criteriaQuery) {
        this.criteriaQuery = criteriaQuery;
    }

    public CriteriaQuery<S> getCriteriaQuery() {
        return criteriaQuery;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void select(ISelection<? extends S> selection) {
        criteriaQuery.select((Selection<S>) selection);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ICriteriaQuery<S> where(IExpression<Boolean> expression) {
        return new JpaCriteriaQuery(criteriaQuery.where((Expression<Boolean>) expression));

    }

    @Override
    public IPredicate getRestriction() {
        return new JpaPredicate(criteriaQuery.getRestriction());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <T> IRoot<T> from(Class<T> clazz) {
        return new JpaRoot(criteriaQuery.from(clazz));
    }

    @Override
    public Class<S> getResultType() {
        return criteriaQuery.getResultType();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ICriteriaQuery<S> orderBy(IOrder... orders) {
        Order[] jpaOrders = new Order[orders.length];
        for (int i = 0; i < orders.length; i++) {
            jpaOrders[i] = ((JpaOrder) orders[i]).getOrder();
        }
        return new JpaCriteriaQuery(criteriaQuery.orderBy(jpaOrders));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public ICriteriaQuery<S> orderBy(List<IOrder> orderList) {
        List<Order> jpaOrders = new ArrayList<>(orderList.size());
        for (IOrder order : orderList) {
            jpaOrders.add(((JpaOrder) order).getOrder());
        }
        return new JpaCriteriaQuery(criteriaQuery.orderBy(jpaOrders));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public <S2> ISubquery<S2> subquery(Class<S2> clazz) {
        return new JpaSubquery(criteriaQuery.subquery(clazz));
    }

}
