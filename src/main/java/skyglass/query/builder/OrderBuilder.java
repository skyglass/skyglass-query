package skyglass.query.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to build order fields from QueryRequestDTO in a declarative way.
 * Each OrderField class contains information on how to build correspondent SQL ORDER BY part
 * 
 */
public class OrderBuilder {

	private List<OrderField> orderFields = new ArrayList<OrderField>();

	private QueryRequestDTO queryRequest;

	public OrderBuilder(QueryRequestDTO queryRequest) {
		this.queryRequest = queryRequest;
	}

	public OrderBuilder bindOrder(String alias, String... orderFields) {
		return bindOrder(alias, FieldType.String, orderFields);
	}

	public OrderBuilder bindOrder(String alias, FieldType fieldType, String... orderFields) {
		if (alias.equals(queryRequest.getOrderField())) {
			setOrder(queryRequest.getOrderType(), fieldType, orderFields);
		}
		return this;
	}

	public boolean shouldBindOrder(String alias) {
		return (alias.equals(queryRequest.getOrderField()));
	}

	public OrderBuilder addOrder(OrderType orderType, String... orderFields) {
		return addOrder(orderType, FieldType.String, orderFields);
	}

	public OrderBuilder addOrder(OrderType orderType, FieldType fieldType, String... orderFields) {
		OrderField order = new OrderField(new FieldResolver(orderFields), orderType, fieldType);
		this.orderFields.add(order);
		return this;
	}

	public OrderBuilder setOrder(OrderType orderType, String... orderFields) {
		return setOrder(orderType, FieldType.String, orderFields);
	}

	public OrderBuilder setOrder(OrderType orderType, FieldType fieldType, String... orderFields) {
		this.orderFields.clear();
		addOrder(orderType, fieldType, orderFields);
		return this;
	}

	public OrderBuilder setDefaultOrder(OrderType orderType, String... orderFields) {
		return setDefaultOrder(orderType, FieldType.String, orderFields);
	}

	public OrderBuilder setDefaultOrder(OrderType orderType, FieldType fieldType, String... orderFields) {
		if (this.orderFields.size() == 0) {
			setOrder(orderType, fieldType, orderFields);
		}
		return this;
	}
	
	
	public OrderBuilder addDefaultOrder(OrderType orderType, String... orderFields) {
		return addDefaultOrder(orderType, FieldType.String, orderFields);
	}

	public OrderBuilder addDefaultOrder(OrderType orderType, FieldType fieldType, String... orderFields) {
		addOrder(orderType, fieldType, orderFields);
		return this;
	}

	public boolean shouldSetDefaultOrder() {
		return this.orderFields.size() == 0;
	}

	public List<OrderField> getOrderFields() {
		return orderFields;
	}

}
