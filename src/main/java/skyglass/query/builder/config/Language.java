package skyglass.query.builder.config;

import org.apache.commons.lang3.StringUtils;

public enum Language {
	EN("en"), DE("de"), CN("cn"), JP("jp"), ES("es"), FR("fr"), PT("pt"), IT("it");

	public static final Language DEFAULT = Language.EN;

	private final String languageCode;

	private Language(String languageCode) {
		this.languageCode = languageCode;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public static Language getByLanguageCode(String languageCode) {
		if (StringUtils.isNotBlank(languageCode)) {
			for (Language lang : Language.values()) {
				if (StringUtils.equalsIgnoreCase(languageCode, lang.getLanguageCode())) {
					return lang;
				}
			}
		}
		return Language.DEFAULT;
	}

	public static Language[] getLanguages(String excludedLanguageCode) {
		if (StringUtils.isBlank(excludedLanguageCode)) {
			return values();
		}
		Language[] result = new Language[Language.values().length - 1];
		Language excludedLanguage = Language.getByLanguageCode(excludedLanguageCode);
		int i = 0;
		for (Language lang : Language.values()) {
			if (lang != excludedLanguage) {
				result[i] = lang;
				i++;
			}
		}
		return result;
	}
}
