package skyglass.query.builder.composer.search;

import org.apache.commons.lang3.StringUtils;

public class SearchTerm {
	
	private String field;
	
	private Operator operator;
	
	private Object value;
	
	private Combination combination;
	
	public SearchTerm(String value) {
		this(null, null, value, null);
	}
	
	public SearchTerm(String field, String operator, Object value, String combination) {
		this.field = field;
		this.operator = StringUtils.isBlank(operator) ? Operator.Like : Operator.from(operator);
		this.value = value;
		this.value = resolveValue(value, this.operator);
		this.combination = StringUtils.isBlank(combination) ? Combination.And : Combination.from(combination);
	}
	
	public String getField() {
		return field;
	}

	public Operator getOperator() {
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
	
	private Object resolveValue(Object value, Operator operator) {
		if (isNotStringValueEmpty() && operator.isInteger()) {
			try {
				return Integer.parseInt(getStringValue());
			} catch (NumberFormatException e) {
				
			}
		}
		return value;
	}

}
