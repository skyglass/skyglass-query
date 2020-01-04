package skyglass.query;

import org.apache.commons.lang3.StringUtils;

public class QueryFunctions {

	private static String coalesce(String[] fieldResolvers, boolean lower) {
		StringBuilder sb = new StringBuilder();
		sb.append("COALESCE(");
		int i = 0;
		for (String fieldResolver : fieldResolvers) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(lower ? lower(fieldResolver) : fieldResolver);
			i++;
		}
		sb.append(")");
		return sb.toString();
	}
	
	private static String concat(String[] fieldResolvers, boolean lower) {
		StringBuilder sb = new StringBuilder();
		sb.append("CONCAT(");
		int i = 0;
		for (String fieldResolver : fieldResolvers) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("COALESCE(");
			sb.append(lower ? lower(fieldResolver) : fieldResolver);
			sb.append(", '')");
			i++;
		}
		sb.append(")");
		return sb.toString();
	}

	public static String lower(String fieldResolver) {
		return String.format("LOWER(%s)", fieldResolver);
	}

	public static String coalesce(String... fieldResolvers) {
		return coalesce(fieldResolvers, false);
	}

	public static String lowerCoalesce(String... fieldResolvers) {
		return coalesce(fieldResolvers, true);
	}
	
	public static String concat(String... fieldResolvers) {
		return concat(fieldResolvers, false);
	}

	public static String lowerConcat(String... fieldResolvers) {
		return concat(fieldResolvers, true);
	}

	public static String and(String queryStr1, String queryStr2) {
		return StringUtils.isNotBlank(queryStr1) ? (queryStr1 + " AND " + queryStr2) : queryStr2;
	}

	public static <T extends Enum<T>> String ordinalToString(Enum<T>[] enumValues, String path) {
		return String.format(getEnumString(enumValues), path);
	}

	private static <T extends Enum<T>> String getEnumString(Enum<T>[] enumValues) {
		String result = "CASE %s";
		for (Enum<T> value : enumValues) {
			result += " WHEN " + value.ordinal() + " THEN '" + value.toString() + "'";
		}
		result += " END";
		return result;
	}

}
