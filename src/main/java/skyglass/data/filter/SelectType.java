package skyglass.data.filter;

public enum SelectType {

    Property(0),
    Count(1),
    CountDistinct(2),
    Max(3),
    Min(4),
    Sum(5),
    Avg(6);
    
	private int value;
	
	private SelectType(int value) {
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}
	
	public static SelectType valueOf(int value) {
		for (SelectType selectOperator: SelectType.values()) {
			if (selectOperator.value == value) {
				return selectOperator;
			}
		}
		return SelectType.Property;
	}
}
