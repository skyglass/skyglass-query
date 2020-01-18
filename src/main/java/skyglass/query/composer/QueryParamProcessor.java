package skyglass.query.composer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class QueryParamProcessor {

	private static final String NATIVE_CHAR = "?";

	private static final String JPA_CHAR = ":";

	private static final String NATIVE_PARAM_FORMAT = "?%s";

	private static final String NATIVE_PARAM_IN_FORMAT = "%s IN (%s)";

	private static final String PARAM_IN_FORMAT = "%s IN :%s";

	private static final Pattern PARAM_REGEX_PATTERN = Pattern.compile("(?=:(\\w+))");

	private static final Pattern PARAM_NATIVE_REGEX_PATTERN = Pattern.compile("(?=\\?(\\w+))");

	static String parseParams(QueryComposer root, QueryParamBuilder builder, String part) {
		if (StringUtils.isBlank(part)) {
			return part;
		}
		QueryRequestDTO queryRequest = root.getQueryRequest();
		if (queryRequest != null) {
			List<QueryParam> paramValues = new ArrayList<>();
			List<String> matches = getAllMatches(root, part);
			for (String match : matches) {
				Object paramValue = getSimpleProperty(queryRequest, match);
				paramValues.add(QueryParam.create(match, paramValue));
			}

			for (QueryParam param : paramValues) {
				if (builder.isCollection(param.getValue())) {
					Collection<?> collection = (Collection<?>) param.getValue();
					part = replaceListPart(root, builder, param.getName(), part, collection);
					builder._doSetParameters(root, param.getName(), collection);
				} else {
					builder._doSetParameter(param.getName(), param.getValue());
				}
			}
		}
		return part;
	}

	static String parseSearchTerm(QueryComposer root) {
		QueryRequestDTO queryRequest = root.getQueryRequest();
		if (queryRequest != null) {
			return queryRequest.getSearchTerm();
		}
		return null;
	}

	static String parseSearchTerm(String paramName, QueryComposer root) {
		QueryRequestDTO queryRequest = root.getQueryRequest();
		if (queryRequest != null) {
			Object result = getSimpleProperty(queryRequest, paramName);
			if (result != null) {
				return result.toString();
			}
		}
		return null;
	}

	public static String parseField(String field, QueryRequestDTO queryRequest) {
		String language = queryRequest == null || StringUtils.isBlank(queryRequest.getLang()) ? QueryRequestDTO.DEFAULT_LANGUAGE : queryRequest.getLang();
		return field.replace("{lang}", language);
	}

	private static List<String> getAllMatches(QueryComposer root, String text) {
		Matcher m = root.isNativeQuery() ? PARAM_NATIVE_REGEX_PATTERN.matcher(text) : PARAM_REGEX_PATTERN.matcher(text);
		List<String> matches = new ArrayList<String>();
		while (m.find()) {
			matches.add(m.group(1));
		}
		return matches;
	}

	public static String processPart(QueryComposer root, QueryParamBuilder builder, String paramName, String part, Object value) {
		if (builder.isCollection(value)) {
			return replaceListPart(root, builder, paramName, part, (Collection<?>) value);
		}
		return part;
	}

	public static String replaceListPart(QueryComposer root, QueryParamBuilder builder, String paramName, String part, Collection<?> list) {
		if (root.isNativeQuery()) {
			StringBuilder replacement = new StringBuilder();
			for (int i = 1; i <= list.size(); i++) {
				if (i > 1) {
					replacement.append(", ");
				}
				replacement.append(root.isNativeQuery() ? NATIVE_CHAR : JPA_CHAR);
				replacement.append(paramName);
				replacement.append(Integer.toString(i));
			}
			return part.replace(String.format(NATIVE_PARAM_FORMAT, paramName), String.format("(%s)", replacement.toString()));
		}
		return part;
	}

	static String getInString(QueryComposer root, String propertyName, String paramName, Collection<?> list) {
		if (CollectionUtils.isEmpty(list)) {
			return null;
		}
		if (root.isNativeQuery()) {
			StringBuilder replacement = new StringBuilder();
			for (int i = 1; i <= list.size(); i++) {
				if (i > 1) {
					replacement.append(", ");
				}
				replacement.append(root.isNativeQuery() ? NATIVE_CHAR : JPA_CHAR);
				replacement.append(paramName);
				replacement.append(Integer.toString(i));
			}
			return String.format(getInFormat(root), propertyName, replacement.toString());
		}
		return String.format(getInFormat(root), propertyName, paramName);
	}

	static String parseParamName(String propertyName) {
		return propertyName.replace(".", "_");
	}

	private static String getInFormat(QueryComposer root) {
		return root.isNativeQuery() ? NATIVE_PARAM_IN_FORMAT : PARAM_IN_FORMAT;
	}

	public static void main(String[] args) {
		List<String> testList = Stream.of("test4", "test2", "test3").collect(Collectors.toList());
		for (String test : testList) {
			System.out.println(test);
		}
	}

	public static Object getSimpleProperty(QueryRequestDTO queryRequest, String paramName) {
		Object result = null;
		try {
			result = PropertyUtils.getSimpleProperty(queryRequest, paramName);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			//continue
		}
		if (result == null) {
			result = queryRequest.get(paramName);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T getPropertyValue(Object object, String paramName, Class<T> clazz) {
		T result = null;
		try {
			result = (T) PropertyUtils.getSimpleProperty(object, paramName);
		} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			//continue
		}
		return result;
	}

}
