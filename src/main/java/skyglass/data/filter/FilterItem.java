package skyglass.data.filter;

public class FilterItem {
	
	private String fieldName;
	
	private FieldType fieldType;
	
	private Object filterValue;
	
	private FilterType filterType;
	
	private FilterClass filterClass;
	
	public FilterItem(String fieldName, Object filterValue) {
		this(fieldName, FieldType.Path, filterValue);
	}

	public FilterItem(String fieldName, Object filterValue, FilterType filterType) {
		this(fieldName, FieldType.Path, filterValue, filterType);
	}
	
    public FilterItem(String fieldName, FieldType fieldType, Object filterValue) {
    	this(fieldName, fieldType, filterValue, FilterType.EQ, FilterClass.STRING);
    }   
    
    public FilterItem(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType) {
    	this(fieldName, fieldType, filterValue, filterType, FilterClass.STRING);
    }  

	public FilterItem(String fieldName, Object filterValue, FilterClass filterClass) {
		this(fieldName, FieldType.Path, filterValue, filterClass);
	}

	public FilterItem(String fieldName, Object filterValue, FilterType filterType, FilterClass filterClass) {
		this(fieldName, FieldType.Path, filterValue, filterType, filterClass);
	}
	
    public FilterItem(String fieldName, FieldType fieldType, Object filterValue, FilterClass filterClass) {
    	this(fieldName, fieldType, filterValue, FilterType.EQ, filterClass);
    } 
	
	public FilterItem(String fieldName, FieldType fieldType, Object filterValue, FilterType filterType, FilterClass filterClass) {
		this.fieldName = fieldName;
		this.fieldType = fieldType;
		this.filterValue = filterValue;
		this.filterType = filterType;
		this.filterClass = filterClass; 
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

	public FilterClass getFilterClass() {
		return filterClass;
	}

}
