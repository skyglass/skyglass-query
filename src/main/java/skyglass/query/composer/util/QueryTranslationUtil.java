
package skyglass.query.composer.util;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

import skyglass.query.composer.config.Language;

/**
 *
 * @author skliarm
 */
public class QueryTranslationUtil {

	public static final List<String> LANGUAGES = Stream.of(Language.values()).map(e -> e.getLanguageCode()).collect(Collectors.toList());

	public static String getNativeSearchLikeTerm(String fieldName, String parameterName) {
		return getSearchTerm(fieldName, parameterName, true);
	}

	public static String getJpaSearchLikeTerm(String fieldName, String parameterName) {
		return getSearchTerm(fieldName, parameterName, false);
	}

	private static String getSearchTerm(String fieldName, String parameterName, boolean nativeQuery) {
		StringBuilder builder = new StringBuilder();
		builder.append(" (");

		String parameterChar = nativeQuery ? "?" : ":";
		for (Language lang : Language.values()) {
			if (builder.length() > 2) {
				builder.append("OR ");
			}

			String languageColumn = lang.getLanguageCode();
			if (nativeQuery) {
				languageColumn = languageColumn.toUpperCase();
			}

			builder.append("LOWER(").append(fieldName).append(".").append(languageColumn).append(")");
			builder.append(" LIKE ");
			builder.append("LOWER(").append(parameterChar).append(parameterName).append(") ");
		}
		builder.append(") ");
		return builder.toString();
	}

	public static String getTranslatedField(String currentLang, String field) {
		return QueryFunctions.coalesce(getTranslatedFields(currentLang, field));
	}

	public static String[] getTranslatedFields(String currentLang, String... fields) {
		currentLang = getCurrentLang(currentLang);
		String[] result = new String[fields.length * Language.values().length];
		int j = 0;
		for (int i = 0; i < fields.length; i++) {
			result[j] = fields[i] + "." + getCurrentLang(currentLang);
			j++;
			for (Language lang : Language.getLanguages(currentLang)) {
				result[j] = fields[i] + "." + lang.getLanguageCode();
				j++;
			}
		}
		return result;
	}

	private static String getCurrentLang(String currentLang) {
		return StringUtils.isNotBlank(currentLang) ? currentLang : getDefaultLang();
	}

	private static String getDefaultLang() {
		return Language.DEFAULT.getLanguageCode();
	}
	
	public static String coalesce(String field) {
		return coalesce(null, field);
	}

	public static String coalesce(String currentLang, String field) {
		return QueryFunctions.coalesce(getTranslatedFields(currentLang, field));
	}

}
