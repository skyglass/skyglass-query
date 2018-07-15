package skyglass.data.filter;

public enum FilterType {
	Equals(0, " = "),
	NotEquals(1, " != "),
	Less(2, " < "),
	Greater(3, " > "),
	LessOrEquals(4, " <= "),
	GreaterOrEquals(5, " >= "),
	Like(6, " LIKE "),
	EqualsProp(7, " = "),
	NotEqualsProp(8, " != "),
	In(9, " IN "),
	NotIn(10, " NOT IN "),
	IsNull(11, " IS NULL "),
	IsNotNull(12, " IS NOT NULL "),
	Empty(13, " EMPTY "),
	NotEmpty(14, " NOT EMPTY "),
	Range(15, ""),
	And(100, " AND "),
	Or(101, " OR "),
	Not(102, " NOT "),
	Some(200, " SOME "),
	All(201, " ALL "),
	None(202, " NONE "),
	Exists(300, " EXISTS "),
	NotExists(301, " NOT EXISTS ");
	
	private String operator;
	
	private int value;
	
	private FilterType(int value, String operator) {
		this.value = value;
		this.operator = operator;
	}
	
	public String getOperator() {
		return operator;
	}
	
	public static FilterType valueOf(int value) {
		for (FilterType filterType: FilterType.values()) {
			if (filterType.value == value) {
				return filterType;
			}
		}
		return FilterType.Equals;
	}
	
    public boolean isTakesSingleValue() {
        return value <= 7;
    }

    public boolean isTakesListOfValues() {
        return this == In || this == NotIn;
    }

    public boolean isTakesNoValue() {
        return value >= 10 && value <= 14;
    }

    public boolean isTakesListOfSubFilters() {
        return this == And || this == Or || this == Not || value >= 200;
    }

    public boolean isTakesNoProperty() {
        return value >= 100 && value <= 102;
    }
}
