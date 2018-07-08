package skyglass.data.filter;

public enum FilterType {
	Equals(0),
	NotEquals(1),
	Less(2),
	Greater(3),
	LessOrEquals(4),
	GreaterOrEquals(5),
	Like(6),
	EqualsProp(7),
	NotEqualsProp(8),
	In(9),
	NotIn(10),
	IsNull(11),
	IsNotNull(12),
	Empty(13),
	NotEmpty(14),
	Range(15),
	And(100),
	Or(101),
	Not(102),
	Some(200),
	All(201),
	None(202),
	Exists(300),
	NotExists(301);
	
	private String name;
	
	private int value;
	
	private FilterType(int value) {
		this.value = value;
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
