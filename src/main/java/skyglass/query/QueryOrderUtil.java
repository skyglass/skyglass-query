package skyglass.query;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.collections.CollectionUtils;

import skyglass.query.builder.FieldResolver;
import skyglass.query.builder.OrderField;

public class QueryOrderUtil {

	public static String applyOrder(List<OrderField> orderFields) {
		return applyOrder(orderFields, null);
	}

	public static String applyOrder(List<OrderField> orderFields, Function<String, String> converter) {
		if (CollectionUtils.isEmpty(orderFields)) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (OrderField orderField : orderFields) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			FieldResolver fieldResolver = orderField.getOrderField();
			if (fieldResolver.isMultiple()) {
				sb.append(applyMultipleOrder(orderField, converter));
			} else {
				sb.append(applySingleOrder(orderField, converter));
			}
		}
		if (!first) {
			return sb.toString();
		}
		return null;
	}

	private static String applyMultipleOrder(OrderField orderField, Function<String, String> converter) {
		StringBuilder sb = new StringBuilder();
		if (orderField.isString()) {
			sb.append(QueryFunctions.lowerCoalesce(orderField.getOrderField().getResolversArray(converter)));
		} else {
			sb.append(QueryFunctions.coalesce(orderField.getOrderField().getResolversArray(converter)));
		}
		sb.append(orderField.isDescending() ? " DESC" : " ASC");
		return sb.toString();
	}

	private static String applySingleOrder(OrderField orderField, Function<String, String> converter) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String fieldResolver : orderField.getOrderField().getResolvers(converter)) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			if (orderField.isString()) {
				sb.append(QueryFunctions.lower(fieldResolver));
			} else {
				sb.append(fieldResolver);
			}
			sb.append(orderField.isDescending() ? " DESC" : " ASC");
		}
		return sb.toString();
	}

}
