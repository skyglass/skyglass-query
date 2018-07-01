package skyglass.data.filter;

public enum JunctionType {
	AND,
	OR;
	
	public static FilterType toFilterType(JunctionType junctionType) {
		if (junctionType == JunctionType.OR) {
			return FilterType.Or;
		}
		return FilterType.And;
	}
}
