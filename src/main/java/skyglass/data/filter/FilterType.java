package skyglass.data.filter;

public enum FilterType {
	Equals("eq"),
	Like("like"),
	GreaterOrEquals("ge"),
	Greater("gt"),
	LessOrEquals("le"),
	Less("lt"),
	NotEquals("ne"),
	EqualsProp("eqpr"),
	NotEqualsProp("neqpr"),
	Exists("ex"),
	NotExists("nex"),
	In("in"),
	NotIn("nin"),
	IsNull("isn");
	
	private String name;
	
	private FilterType(String name) {
		this.name = name;
	}
	
	public static FilterType nameOf(String name) {
		for (FilterType filterType: FilterType.values()) {
			if (filterType.name.equals(name)) {
				return filterType;
			}
		}
		return FilterType.Equals;
	}
}
