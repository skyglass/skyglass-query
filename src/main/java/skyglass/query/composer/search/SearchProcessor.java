package skyglass.query.composer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Joiner;

public class SearchProcessor {

	private static Pattern SEARCH_TERM_PATTERN = Pattern.compile("^(\\w+?)(" + Joiner.on("|")
			.join(SearchOperator.SIMPLE_OPERATION_SET) + ")(\\p{Punct}?)(\\w+?)(\\p{Punct}?)(,|\\|)$", Pattern.UNICODE_CHARACTER_CLASS);

	//private static final Pattern SEARCH_TERM_PATTERN = Pattern.compile("(\\w+?)(:|<|>)(\\w+?)(,|\\|)", Pattern.UNICODE_CHARACTER_CLASS);

	public static List<SearchTerm> parseSearch(List<String> searchTerms) {
		List<SearchTerm> result = new ArrayList<>();
		for (String searchTerm : searchTerms) {
			Matcher matcher = SEARCH_TERM_PATTERN.matcher(searchTerm + ",");
			boolean found = false;
			while (matcher.find()) {
				found = true;
				result.add(new SearchTerm(matcher.group(1),
						matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5), matcher.group(6)));
			}
			if (!found) {
				result.add(new SearchTerm(searchTerm));
			}
		}
		return result;
	}

	public static Object getExpression(SearchTerm searchTerm, SearchType searchType) {
		SearchOperator operator = searchTerm.getOperator();
		if (!operator.isText()) {
			return searchTerm.getValue();
		}

		if (searchType == SearchType.StartsIgnoreCase
				|| operator == SearchOperator.StartsWith) {
			return "%" + searchTerm.getValue();
		}

		if (operator == SearchOperator.EndsWith) {
			return searchTerm.getValue() + "%";
		}

		if (operator == SearchOperator.EndsWith) {
			return searchTerm.getValue() + "%";
		}

		if (operator == SearchOperator.Contains
				|| operator == SearchOperator.Like) {
			return "%" + searchTerm.getValue() + "%";
		}

		return searchTerm.getValue();
	}

}
