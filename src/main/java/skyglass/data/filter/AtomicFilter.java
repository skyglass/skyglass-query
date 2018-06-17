package skyglass.data.filter;

public class AtomicFilter {
	public FilterType filterType;
	public String propertyName;
	public Object value;
	
	public AtomicFilter(FilterType filterType, String propertyName, Object value) {
		this.filterType = filterType;
		this.propertyName = propertyName;
		this.value = value;
	}	   
}
