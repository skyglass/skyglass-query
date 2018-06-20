package skyglass.data.filter;

public class FilterItem {
	
	private String fieldName;
	
	private FieldType fieldType;
	
	private Object filterValue;
	
	private FilterType filterType;
	
	public FilterItem(String fieldName, Object filterValue) {
		this(fieldName, FieldType.Path, filterValue);
	}
	
    public FilterItem(String fieldName, FieldType fieldType, Object filterValue) {
    	this(fieldName, fieldType, filterValue, FilterType.Equals);
    } 
    
	public FilterItem(String fieldName, Object filterValue, FilterType filterType) {
		this(fieldName, FieldType.Path, filterValue, filterType);
	}
	
	public FilterItem(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.filterValue = filterValue;
		this.filterType = filterType;
	}
	
	public String getFieldName() {
		return fieldName;
	}
	
	public FieldType getFieldType() {
		return fieldType;
	}

	public Object getFilterValue() {
		return filterValue;
	}

	public FilterType getFilterType() {
		return filterType;
	}

}
