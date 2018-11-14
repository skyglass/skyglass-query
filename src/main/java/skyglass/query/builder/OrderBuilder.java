package skyglass.query.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import skyglass.query.builder.string.QueryRequestDTO;

public class OrderBuilder {

	private List<OrderField> orderFields = new ArrayList<OrderField>();

	private QueryRequestDTO queryRequest;

	public OrderBuilder(QueryRequestDTO queryRequest) {
		this.queryRequest = queryRequest;
	}

	public OrderBuilder bindOrder(String alias, String... orderFields) {
		if (alias.equals(queryRequest.getOrderField())) {
			setOrder(queryRequest.getOrderType(), orderFields);
		}
		return this;
	}

	public OrderBuilder bindTranslatableOrder(String alias, String... orderFields) {
		return bindOrder(alias, getTranslatedOrderFields(orderFields));
	}

	public OrderBuilder addOrder(OrderType orderType, String... orderFields) {
		OrderField order = new OrderField(new FieldResolver(queryRequest, orderFields), orderType);
		this.orderFields.add(order);
		return this;
	}

	public OrderBuilder setOrder(OrderType orderType, String... orderFields) {
		this.orderFields.clear();
		addOrder(orderType, orderFields);
		return this;
	}

	public OrderBuilder setDefaultTranslatableOrder(OrderType orderType, String... orderFields) {
		return setDefaultOrder(orderType, getTranslatedOrderFields(orderFields));
	}

	public OrderBuilder setDefaultTranslatableOrders(OrderType orderType, String... orderFields) {
		return setDefaultOrders(orderType, getTranslatedOrderFields(orderFields));
	}

	public OrderBuilder setDefaultOrder(OrderType orderType, String... orderFields) {
		if (this.orderFields.size() == 0) {
			setOrder(orderType, orderFields);
		}
		return this;
	}

	public OrderBuilder setDefaultOrders(OrderType orderType, String... orderFields) {
		if (this.orderFields.size() == 0) {
			for (String orderField : orderFields) {
				addOrder(orderType, orderField);
			}
		}
		return this;
	}

	public List<OrderField> getOrderFields() {
		return orderFields;
	}

	private String[] getTranslatedOrderFields(String... orderFields) {
		String[] result = new String[orderFields.length];
		for (int i = 0; i < orderFields.length; i++) {
			result[i] = orderFields[i] + "." + getCurrentLang();
		}
		return result;
	}

	private String getCurrentLang() {
		return StringUtils.isNotBlank(queryRequest.getLang()) ? queryRequest.getLang() : QueryRequestDTO.DEFAULT_LANGUAGE_CODE;
	}
}
