package skyglass.data.filter;

import java.util.Collection;
import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;

import skyglass.query.model.criteria.ICriteriaQueryBuilder;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.query.FilterGroup;
import skyglass.query.model.query.InternalUtil;

public class PrivateFilterItem {

    private Class<?> rootClass;

    protected FieldResolver fieldResolver;
    protected FilterType filterType;
    protected Object filterValue;
    private Supplier<Object> filterValueResolver;
    private FilterGroup filterGroup = FilterGroup.Where;

    // should be overriden to define different behaviour
    protected Supplier<Object> objectConverter(String fieldName, Object value, boolean isCollection) {
        return () -> value;
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue) {
        this(rootClass, fieldResolver, filterValue, FilterType.Equals);
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue,
            FilterType filterType) {
        this.rootClass = rootClass;
        this.fieldResolver = fieldResolver;
        this.filterType = filterType;
        this.filterValue = filterValue;
    }

    public Class<?> getRootClass() {
        return rootClass;
    }

    public FieldResolver getFieldResolver() {
        return fieldResolver;
    }

    public FilterType getFilterType() {
        return filterType;
    }
    
    public boolean hasNullValue() {
    	return filterValue != null;
    }
    
    public boolean hasNotNullValue() {
    	return filterValue != null;
    }
    
    public boolean hasEmptyCollectionValue() {
    	if (hasCollectionValue()) {
    		if (filterValue instanceof Collection<?>) {
    			return ((Collection<?>)filterValue).size() == 0;
    		} else {
    			return ((Object[])filterValue).length == 0;
    		}
    	}
    	return false;
    }
    
    public boolean hasCollectionValue() {
    	return filterValue instanceof Collection || filterValue instanceof Object[];
    }
    
    public Class<?> getValueClass() {
    	return filterValue.getClass();
    }

    public Supplier<Object> getFilterValueResolver() {
        if (filterValueResolver == null) {
            this.filterValueResolver = filterValueResolver();
        }
        return filterValueResolver;
    }

    private Supplier<Object> filterValueResolver() {
        if (filterValue == null) {
            return () -> null;
        }
        if (filterValue instanceof CriteriaBuilder) {
            return () -> filterValue;
        }
        if (filterType == FilterType.Like) {
            return () -> IQueryBuilder.processFilterString(filterValue);
        }
        Object result = objectConverter(fieldResolver.getResolver(), filterValue, isTakesListOfValues());
        return () -> result;
    }
    
    public boolean isTakesSingleValue() {
        return filterType.isTakesSingleValue();
    }

    public boolean isTakesListOfValues() {
        return filterType.isTakesListOfValues();
    }

    public boolean isTakesNoValue() {
        return filterType.isTakesNoValue();
    }

    public boolean isTakesListOfSubFilters() {
        return filterType.isTakesListOfSubFilters();
    }

    public boolean isTakesNoProperty() {
        return filterType.isTakesNoProperty();
    }
    
    public void setHavingType() {
        this.filterGroup = FilterGroup.Having;
    }

    public boolean isHavingType() {
        return this.filterGroup == FilterGroup.Having;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + filterType.hashCode();
        result = prime * result + ((fieldResolver == null) ? 0 
        		: fieldResolver.getFieldName().hashCode());
        result = prime * result + ((filterValue == null) ? 0 : filterValue.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        PrivateFilterItem other = (PrivateFilterItem) obj;
        if (filterType != other.filterType)
            return false;
        if (fieldResolver == null) {
            if (other.fieldResolver != null)
                return false;
        } else if (!fieldResolver.getFieldName().equals(other.fieldResolver.getFieldName()))
            return false;
        if (filterValue == null) {
            if (other.filterValue != null)
                return false;
        } else if (!filterValue.equals(other.filterValue))
            return false;
        return true;
    }

    @Override
    public String toString() {
        switch (filterType) {
        case In:
            return "`" + fieldResolver.getFieldName() + "` in (" + InternalUtil.paramDisplayString(filterValue) + ")";
        case NotIn:
            return "`" + fieldResolver.getFieldName() + "` not in (" + InternalUtil.paramDisplayString(filterValue) + ")";
        case Equals:
            return "`" + fieldResolver.getFieldName() + "` = " + InternalUtil.paramDisplayString(filterValue);
        case NotEquals:
            return "`" + fieldResolver.getFieldName() + "` != " + InternalUtil.paramDisplayString(filterValue);
        case Greater:
            return "`" + fieldResolver.getFieldName() + "` > " + InternalUtil.paramDisplayString(filterValue);
        case Less:
            return "`" + fieldResolver.getFieldName() + "` < " + InternalUtil.paramDisplayString(filterValue);
        case GreaterOrEquals:
            return "`" + fieldResolver.getFieldName() + "` >= " + InternalUtil.paramDisplayString(filterValue);
        case LessOrEquals:
            return "`" + fieldResolver.getFieldName() + "` <= " + InternalUtil.paramDisplayString(filterValue);
        case Like:
            return "`" + fieldResolver.getFieldName() + "` LIKE " + InternalUtil.paramDisplayString(filterValue);
        case IsNull:
            return "`" + fieldResolver.getFieldName() + "` IS NULL";
        case IsNotNull:
            return "`" + fieldResolver.getFieldName() + "` IS NOT NULL";
        case Empty:
            return "`" + fieldResolver.getFieldName() + "` IS EMPTY";
        case NotEmpty:
            return "`" + fieldResolver.getFieldName() + "` IS NOT EMPTY";
        case And:
        case Or:
        case Not:
        case Some:
        case All:
        case None:
        default:
            return "**INVALID OPERATOR: (" + filterType + ") - VALUE: " + InternalUtil.paramDisplayString(filterValue) + " **";
        }
    }
    
}
