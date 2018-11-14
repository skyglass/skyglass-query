package skyglass.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import skyglass.query.builder.FieldResolver;
import skyglass.query.builder.string.QueryRequestDTO;
import skyglass.query.builder.string.SearchField;
import skyglass.query.builder.string.SearchType;
import skyglass.query.builder.string.StringPartBuilder;

public class QuerySearchUtil {

	private static final List<String> LANGUAGES = Arrays.asList("cn", "de", "en", "es", "fr", "jp", "pt");

	public static String applySearch(QueryRequestDTO queryRequest, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, SearchType.IgnoreCase, false, nativeQuery, searchFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchType searchType, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchType, false, nativeQuery, searchFields);
	}

	public static String applyTranslatableSearch(QueryRequestDTO queryRequest, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, SearchType.IgnoreCase, true, nativeQuery, searchFields);
	}

	public static String applyTranslatableSearch(QueryRequestDTO queryRequest, SearchType searchType, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchType, true, nativeQuery, searchFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchType searchType, boolean translatable, boolean nativeQuery, String... searchFields) {
		SearchField search = new SearchField(new FieldResolver(queryRequest, searchFields), StringPartBuilder.SEARCH_TERM_FIELD, searchType, translatable, queryRequest.getLang());
		return applySearch(nativeQuery, search);
	}

	public static String applySearch(boolean nativeQuery, SearchField... searchFields) {
		StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (SearchField searchField : searchFields) {
			if (searchField.getFieldResolver().isEmpty()) {
				continue;
			}
			if (first) {
				first = false;
			} else {
				builder.append(" OR ");
			}
			builder.append(getSearchTerm(searchField, nativeQuery));
		}
		if (first) {
			return null;
		}
		return builder.toString();
	}

	private static String getTranslatableSearchTerm(String fieldResolver, SearchField searchField, boolean nativeQuery) {
		StringBuilder builder = new StringBuilder();
		String parameterChar = nativeQuery ? "?" : ":";
		boolean first = true;
		for (String lang : getLanguages(searchField)) {
			if (first) {
				first = false;
			} else {
				builder.append(" OR ");
			}
			if (searchField.isIgnoreCase()) {
				builder.append("LOWER(");
			}
			builder.append(fieldResolver).append(".").append(lang);
			if (searchField.isIgnoreCase()) {
				builder.append(")");
			}
			builder.append(" LIKE ");
			builder.append("LOWER(").append(parameterChar).append(searchField.getParamName());
			if (searchField.isIgnoreCase()) {
				builder.append(")");
			}
		}
		return builder.toString();
	}

	public static String getSearchTerm(SearchField searchField, boolean nativeQuery) {
		StringBuilder builder = new StringBuilder();
		builder.append("( ");
		String parameterChar = nativeQuery ? "?" : ":";
		boolean first = true;
		for (String fieldResolver : searchField.getFieldResolver().getResolvers()) {
			if (first) {
				first = false;
			} else {
				builder.append(" OR ");
			}
			if (searchField.isTranslatable()) {
				builder.append(getTranslatableSearchTerm(fieldResolver, searchField, nativeQuery));
			} else {
				if (searchField.isIgnoreCase()) {
					builder.append("LOWER(");
				}
				builder.append(fieldResolver);
				if (searchField.isIgnoreCase()) {
					builder.append(")");
				}
				builder.append(" LIKE ");
				if (searchField.isIgnoreCase()) {
					builder.append("LOWER(");
				}
				builder.append(parameterChar).append(searchField.getParamName());
				if (searchField.isIgnoreCase()) {
					builder.append(")");
				}
			}
		}
		builder.append(" )");
		return builder.toString();
	}

	private static List<String> getLanguages(SearchField searchField) {
		return StringUtils.isBlank(searchField.getLang()) ? LANGUAGES : Collections.singletonList(searchField.getLang());
	}

}
