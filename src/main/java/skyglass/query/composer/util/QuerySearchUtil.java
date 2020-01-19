package skyglass.query.composer.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import skyglass.query.composer.FieldResolver;
import skyglass.query.composer.QueryRequestDTO;
import skyglass.query.composer.SearchBuilder;
import skyglass.query.composer.config.Language;
import skyglass.query.composer.search.SearchField;
import skyglass.query.composer.search.SearchTerm;
import skyglass.query.composer.search.SearchType;

public class QuerySearchUtil {

	private static final List<String> LANGUAGES = Stream.of(Language.values()).map(e -> e.getLanguageCode()).collect(Collectors.toList());

	public static String applySearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchTerm, SearchType.IgnoreCase, false, nativeQuery, searchFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, SearchType searchType, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchTerm, searchType, false, nativeQuery, searchFields);
	}

	public static String applyTranslatableSearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchTerm, SearchType.IgnoreCase, true, nativeQuery, searchFields);
	}

	public static String applyTranslatableSearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, SearchType searchType, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchTerm, searchType, true, nativeQuery, searchFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, SearchType searchType, boolean translatable, boolean nativeQuery, String... searchFields) {
		return applySearch(queryRequest, searchTerm, searchType, SearchBuilder.SEARCH_TERM_PARAM_NAME, translatable, nativeQuery, searchFields);
	}

	public static String applySearch(QueryRequestDTO queryRequest, SearchTerm searchTerm, SearchType searchType, String searchTermField, boolean translatable, boolean nativeQuery,
			String... searchFields) {
		SearchField search = new SearchField(searchTerm, new FieldResolver(searchFields), searchTermField, searchType, translatable, queryRequest.getLang());
		return applySearch(nativeQuery, search);
	}

	public static String applySearch(boolean nativeQuery, SearchBuilder searchBuilder) {
		return applySearch(nativeQuery, searchBuilder.getSearchFields());
	}

	private static String applySearch(boolean nativeQuery, SearchField... searchFields) {
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
		for (String lang : getLanguages()) {
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
			builder.append(" " + searchField.getOperator() + " ");
			builder.append("LOWER(").append(parameterChar).append(searchField.getParamName());
			if (searchField.isIgnoreCase()) {
				builder.append(")");
			}
		}
		return builder.toString();
	}

	private static String getSearchTerm(SearchField searchField, boolean nativeQuery) {
		StringBuilder builder = new StringBuilder();
		boolean appendPars = searchField.getFieldResolver().getResolvers().size() > 1;
		if (appendPars) {
			builder.append("( ");
		}
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
				builder.append(" " + searchField.getOperator() + " ");
				if (searchField.isIgnoreCase()) {
					builder.append("LOWER(");
				}
				builder.append(parameterChar).append(searchField.getParamName());
				if (searchField.isIgnoreCase()) {
					builder.append(")");
				}
			}
		}
		if (appendPars) {
			builder.append(" )");
		}
		return builder.toString();
	}

	private static List<String> getLanguages() {
		return LANGUAGES;
	}

}
