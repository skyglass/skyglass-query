package skyglass.data.filter;

public enum FilterClass {	
    BOOLEAN("Boolean", Boolean.class),
    LONG("Long", Long.class),
    STRING("String", String.class),
    UUID("UUID", java.util.UUID.class);
	
	private String name;
	
	private Class<?> clazz;
	
	private FilterClass(String name, Class<?> clazz) {
		this.name = name;
		this.clazz = clazz;
	}
	
	public static FilterClass nameOf(String name) {
		for (FilterClass filterClass: FilterClass.values()) {
			if (filterClass.name.equals(name)) {
				return filterClass;
			}
		}
		return FilterClass.STRING;
	}
	
	public static FilterClass classOf(Class<?> clazz) {
		for (FilterClass filterClass: FilterClass.values()) {
			if (filterClass.clazz.equals(clazz)) {
				return filterClass;
			}
		}
		return FilterClass.STRING;
	}	
}

