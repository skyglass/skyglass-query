package skyglass.query.builder.string;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import skyglass.query.QueryOrderUtil;
import skyglass.query.QuerySearchUtil;
import skyglass.query.builder.OrderField;

public class QueryProcessor {

	public static List<SelectField> parseSelect(String selectString) {
		if (StringUtils.isEmpty(selectString)) {
			return null;
		}

		List<SelectField> result = new ArrayList<>();
		String[] parts = selectString.replaceAll("(?i)distinct ?\\(?", "").split(" ?, ?");
		for (String part : parts) {
			String[] subParts = part.split("(?i) as ");
			SelectField selectField = null;
			if (subParts.length == 1) {
				String path = subParts[0].replaceAll(" ?\\)?", "").trim();
				String[] pathParts = path.split("\\.");
				selectField = new SelectField(pathParts[pathParts.length - 1], path);
			} else {
				selectField = new SelectField(subParts[1].replaceAll(" ?\\)?", "").trim(), subParts[0].trim());
			}
			result.add(selectField);
		}

		return result;
	}

	public static String applySelect(List<SelectField> selectFields) {
		if (CollectionUtils.isEmpty(selectFields)) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SelectField selectField : selectFields) {
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(selectField.getExpression() + " AS " + selectField.getAlias());
		}
		if (!first) {
			return sb.toString();
		}
		return null;
	}

	public static String applyOrder(List<OrderField> orderFields) {
		return QueryOrderUtil.applyOrder(orderFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchType searchType, boolean translatable, boolean nativeQuery, String... searchFields) {
		return QuerySearchUtil.applySearch(queryRequest, searchType, translatable, nativeQuery, searchFields);
	}

	public static String applySearch(boolean nativeQuery, SearchField... searchFields) {
		return QuerySearchUtil.applySearch(nativeQuery, searchFields);
	}

}
