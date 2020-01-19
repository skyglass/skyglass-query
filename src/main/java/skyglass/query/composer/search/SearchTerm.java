package skyglass.query.composer.search;

import org.apache.commons.lang3.StringUtils;

public class SearchTerm {

	private String field;

	private String alias;

	private SearchOperator operator;

	private Object value;

	private String stringValue;

	private Combination combination;

	private SearchValueType valueType;

	public SearchTerm(String value) {
		this(null, null, null, value, null);
	}

	public SearchTerm(String field, String alias, String operator, String value, String combination) {
		this.field = field;
		this.alias = alias;
		this.operator = StringUtils.isBlank(operator) ? SearchOperator.Like : SearchOperator.from(operator);
		this.combination = StringUtils.isBlank(combination) ? Combination.And : Combination.from(combination);

		if (this.operator == SearchOperator.Equal) { // the operation may be complex operation
			final boolean startWithAsterisk = value.startsWith(SearchOperator.ZERO_OR_MORE_REGEX);
			if (startWithAsterisk) {
				value = value.substring(1);
			}
			final boolean endWithAsterisk = value.endsWith(SearchOperator.ZERO_OR_MORE_REGEX);
			if (endWithAsterisk) {
				value = value.substring(0, value.length() - 1);
			}

			if (startWithAsterisk && endWithAsterisk) {
				this.operator = SearchOperator.Contains;
			} else if (startWithAsterisk) {
				this.operator = SearchOperator.EndsWith;
			} else if (endWithAsterisk) {
				this.operator = SearchOperator.StartsWith;
			}
		}

		this.stringValue = value;
		this.value = resolveValue(value, this.operator);
	}

	public String getField() {
		return field;
	}

	public String getAlias() {
		return alias;
	}

	public SearchOperator getOperator() {
		return operator;
	}

	public Object getValue() {
		return value;
	}

	public String getStringValue() {
		return stringValue;
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

	private Object resolveValue(String value, SearchOperator operator) {
		this.valueType = SearchValueType.Text;
		Object result = value;
		if (isNotStringValueEmpty()) {
			if (operator.isNumeric() || operator == SearchOperator.Equal) {
				try {
					result = Integer.parseInt(value);
					this.valueType = SearchValueType.Integer;
				} catch (NumberFormatException e) {

				}
			}
		}
		return result;
	}

	public void adaptCombination(Combination combination) {
		this.combination = combination;
	}

	public boolean isNumeric() {
		return this.valueType.isNumeric();
	}

}
