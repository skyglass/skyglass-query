package skyglass.query.builder.composer.search;

import org.apache.commons.lang3.StringUtils;

public class SearchTerm {
	
	private String field;
	
	private Operator operator;
	
	private String value;
	
	private Combination combination;
	
	public SearchTerm(String value) {
		this(null, null, value, null);
	}
	
	public SearchTerm(String field, String operator, String value, String combination) {
		this.field = field;
		this.operator = StringUtils.isBlank(operator) ? Operator.Like : Operator.from(operator);
		this.value = value;
		this.combination = StringUtils.isBlank(combination) ? Combination.And : Combination.from(combination);
	}
	
	public String getField() {
		return field;
	}

	public Operator getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}
	
	public Combination getCombination() {
		return combination;
	}
	
	public boolean hasField() {
		return field != null;
	}

}
