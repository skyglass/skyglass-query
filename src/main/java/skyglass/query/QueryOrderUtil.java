package skyglass.query;

import java.util.Collection;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import skyglass.query.builder.FieldResolver;
import skyglass.query.builder.OrderField;

public class QueryOrderUtil {

	public static String applyOrder(List<OrderField> orderFields) {
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
				sb.append(applyMultipleOrder(orderField));
			} else {
				sb.append(applySingleOrder(orderField));
			}
		}
		if (!first) {
			return sb.toString();
		}
		return null;
	}

	private static String applyMultipleOrder(OrderField orderField) {
		StringBuilder sb = new StringBuilder();
		sb.append(concat(0, orderField.getOrderField().getResolvers()));
		sb.append(orderField.isDescending() ? " DESC" : " ASC");
		return sb.toString();
	}

	private static String applySingleOrder(OrderField orderField) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (String fieldResolver : orderField.getOrderField().getResolvers()) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append("LOWER(");
			sb.append(fieldResolver);
			sb.append(")");
			sb.append(orderField.isDescending() ? " DESC" : " ASC");
		}
		return sb.toString();
	}

	private static String concat(int i, Collection<String> fieldResolvers) {
		String expression = coalesce(getFieldResolver(i, fieldResolvers));
		if (i < fieldResolvers.size() - 1) {
			return concat(expression, concat(i + 1, fieldResolvers));
		} else {
			return expression;
		}
	}

	private static String coalesce(String concat) {
		StringBuilder sb = new StringBuilder();
		sb.append("COALESCE(LOWER(");
		sb.append(concat);
		sb.append("))");
		return sb.toString();
	}

	private static String concat(String concat1, String concat2) {
		StringBuilder sb = new StringBuilder();
		sb.append("CONCAT(");
		sb.append(concat1);
		sb.append(", ");
		sb.append(concat2);
		sb.append(")");
		return sb.toString();
	}

	private static String getFieldResolver(int i, Collection<String> fieldResolvers) {
		int j = 0;
		for (String fieldResolver : fieldResolvers) {
			if (j == i) {
				return fieldResolver;
			}
			j++;
		}
		return null;
	}

}
