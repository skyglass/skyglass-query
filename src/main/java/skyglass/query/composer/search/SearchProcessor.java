package skyglass.query.composer.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchProcessor {

	private static final Pattern SEARCH_TERM_PATTERN = Pattern.compile("(\\w+?)(:|<|>|=|!)(\\**\\w+?\\**)(,|\\|)", Pattern.UNICODE_CHARACTER_CLASS);

	public static List<SearchTerm> parseSearch(List<String> searchTerms) {
		List<SearchTerm> result = new ArrayList<>();
		Map<String, Integer> fieldIndexMap = new HashMap<>();
		for (String searchTerm : searchTerms) {
			Matcher matcher = SEARCH_TERM_PATTERN.matcher(searchTerm + ",");
			boolean found = false;
			SearchTerm previousSearchTerm = null;
			SearchTerm lastSearchTerm = null;
			while (matcher.find()) {
				found = true;
				previousSearchTerm = lastSearchTerm;
				String field = matcher.group(1);
				String alias = field;
				Integer index = fieldIndexMap.get(field);
				if (index == null) {
					fieldIndexMap.put(field, 1);
				} else {
					index = index + 1;
					field = field + Integer.toString(index);
					fieldIndexMap.put(field, index);
				}
				lastSearchTerm = new SearchTerm(field, alias, matcher.group(2), matcher.group(3), matcher.group(4));
				result.add(lastSearchTerm);
			}
			if (!found) {
				result.add(new SearchTerm(searchTerm));
			} else {
				if (previousSearchTerm != null) {
					lastSearchTerm.adaptCombination(previousSearchTerm.getCombination());
				}
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
			return searchTerm.getValue() + "%";
		}

		if (operator == SearchOperator.EndsWith) {
			return "%" + searchTerm.getValue();
		}

		if (operator == SearchOperator.Contains
				|| operator == SearchOperator.Like) {
			return "%" + searchTerm.getValue() + "%";
		}

		return searchTerm.getValue();
	}

}
