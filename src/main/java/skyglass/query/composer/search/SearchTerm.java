package skyglass.query.composer.search;

import org.apache.commons.lang3.StringUtils;

public class SearchTerm {

	private String field;

	private SearchOperator operator;

	private Object value;

	private Combination combination;

	public SearchTerm(String value) {
		this(null, null, null, value, null, null);
	}

	public SearchTerm(String field, String operator, String prefix, Object value, String suffix, String combination) {
		this.field = field;
		this.operator = StringUtils.isBlank(operator) ? SearchOperator.Like : SearchOperator.from(operator);
		this.combination = StringUtils.isBlank(combination) ? Combination.And : Combination.from(combination);

		if (this.operator == SearchOperator.Equal) { // the operation may be complex operation
			final boolean startWithAsterisk = prefix != null && prefix.contains(SearchOperator.ZERO_OR_MORE_REGEX);
			final boolean endWithAsterisk = suffix != null && suffix.contains(SearchOperator.ZERO_OR_MORE_REGEX);

			if (startWithAsterisk && endWithAsterisk) {
				this.operator = SearchOperator.Contains;
			} else if (startWithAsterisk) {
				this.operator = SearchOperator.EndsWith;
			} else if (endWithAsterisk) {
				this.operator = SearchOperator.StartsWith;
			}
		}

		this.value = resolveValue(value, this.operator);
	}

	public String getField() {
		return field;
	}

	public SearchOperator getOperator() {
		return operator;
	}

	public Object getValue() {
		return value;
	}

	public String getStringValue() {
		return value == null ? null : value.toString();
	}

	public boolean isNotStringValueEmpty() {
		return StringUtils.isNotBlank(getStringValue());
	}

	public Combination getCombination() {
		return combination;
	}

	public boolean hasField() {
		return field != null;
	}

	private Object resolveValue(Object value, SearchOperator operator) {
		if (isNotStringValueEmpty() && operator.isInteger()) {
			try {
				return Integer.parseInt(getStringValue());
			} catch (NumberFormatException e) {

			}
		}
		return value;
	}

}
