package skyglass.query.composer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;

import skyglass.query.composer.QueryComposer;
import skyglass.query.composer.QueryComposerBuilder;
import skyglass.query.composer.SearchBuilder;
import skyglass.query.composer.util.QuerySearchUtil;

public class SearchProcessor {

	private static final Pattern SEARCH_TERM_PATTERN = Pattern.compile("(\\w+?)(:|<|>|=|!)(\\**\\w+?\\**)(,|\\|)", Pattern.UNICODE_CHARACTER_CLASS);

	public static Pair<Combination, List<SearchTerm>> parseSearch(QueryComposerBuilder builder, String searchTerm) {
		List<SearchTerm> result = new ArrayList<>();
		Combination combination = searchTerm.endsWith("|") ? Combination.Or : Combination.And;
		String search = searchTerm.endsWith(",") ? searchTerm : (searchTerm.endsWith("|") ? searchTerm : (searchTerm + ","));
		Matcher matcher = SEARCH_TERM_PATTERN.matcher(search);
		boolean found = false;
		SearchTerm previousSearchTerm = null;
		SearchTerm lastSearchTerm = null;
		while (matcher.find()) {
			found = true;
			previousSearchTerm = lastSearchTerm;
			String field = matcher.group(1);
			String alias = field;
			field = builder.resolveSearchFieldName(field);
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
		return Pair.of(combination, result);
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

	public static String getSearchPart(QueryComposer root, List<List<SearchBuilder>> searchBuilders, boolean and) {
		StringBuilder builder = new StringBuilder();
		boolean appendOuterPars = !and && searchBuilders.size() > 1;
		boolean outerFirst = true;
		if (appendOuterPars) {
			builder.append("( ");
		}
		for (List<SearchBuilder> list : searchBuilders) {
			List<SearchBuilder> andResult = andSearch(list);
			List<SearchBuilder> orResult = orSearch(list);
			boolean appendPars = orResult.size() > 0;
			if (outerFirst) {
				outerFirst = false;
			} else {
				builder.append(and ? " AND " : " OR ");
			}
			if (orResult.size() > 0 || andResult.size() > 0) {
				if (appendPars) {
					builder.append("( ");
				}
			}
			if (orResult.size() > 0) {
				boolean appendInnerPars = orResult.size() > 1 && andResult.size() > 0;
				if (appendInnerPars) {
					builder.append("( ");
				}
				boolean innerFirst = true;
				for (SearchBuilder searchBuilder : orResult) {
					if (innerFirst) {
						innerFirst = false;
					} else {
						builder.append(" OR ");
					}
					builder.append(QuerySearchUtil.applySearch(root.isNativeQuery(), searchBuilder));
				}
				if (appendInnerPars) {
					builder.append(" )");
				}
			}

			if (orResult.size() == 1 && andResult.size() > 0) {
				builder.append(" OR ");
			} else if (orResult.size() > 1 && andResult.size() > 0) {
				builder.append(" AND ");
			}

			if (andResult.size() > 0) {
				boolean innerFirst = true;
				for (SearchBuilder searchBuilder : andResult) {
					if (innerFirst) {
						innerFirst = false;
					} else {
						builder.append(" AND ");
					}
					builder.append(QuerySearchUtil.applySearch(root.isNativeQuery(), searchBuilder));
				}
			}

			if (appendPars) {
				builder.append(" )");
			}
		}
		if (appendOuterPars) {
			builder.append(")");
		}

		return builder.toString();

	}

	private static List<SearchBuilder> andSearch(List<SearchBuilder> searchBuilders) {
		return searchBuilders.stream().filter(s -> s.getSearchTerm().getCombination() == Combination.And).collect(Collectors.toList());
	}

	private static List<SearchBuilder> orSearch(List<SearchBuilder> searchBuilders) {
		return searchBuilders.stream().filter(s -> s.getSearchTerm().getCombination() == Combination.Or).collect(Collectors.toList());
	}

}
