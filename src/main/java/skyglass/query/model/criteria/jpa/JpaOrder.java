package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Order;

import skyglass.query.model.criteria.IOrder;

public class JpaOrder implements IOrder {

    private Order order;

    public JpaOrder(Order order) {
        this.order = order;
    }

    public Order getOrder() {
        return order;
    }

}
