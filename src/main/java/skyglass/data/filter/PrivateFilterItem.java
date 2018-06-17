package skyglass.data.filter;

import java.util.UUID;
import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;

import skyglass.query.model.criteria.IQueryBuilder;

public class PrivateFilterItem {

    private Class<?> rootClass;

    private FieldResolver fieldResolver;
    private FilterType filterType;
    private Object filterValue;
    private Supplier<Object> filterValueResolver;

    private FilterClass filterClass;

    // should be overriden to define different behaviour
    protected Supplier<Object> objectConverter(String fieldName, Object value) {
        return () -> value;
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue) {
        this(rootClass, fieldResolver, filterValue, FilterType.EQ, FilterClass.STRING);
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue,
            FilterType filterType) {
        this(rootClass, fieldResolver, filterValue, filterType, FilterClass.STRING);
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue,
            FilterClass filterClass) {
        this(rootClass, fieldResolver, filterValue, FilterType.EQ, filterClass);
    }

    protected PrivateFilterItem(Class<?> rootClass, FieldResolver fieldResolver, Object filterValue,
            FilterType filterType, FilterClass filterClass) {
        this.rootClass = rootClass;
        this.fieldResolver = fieldResolver;
        this.filterType = filterType;
        this.filterClass = filterClass;
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

    public Supplier<Object> getFilterValueResolver() {
        if (filterValue == null) {
            return () -> null;
        }
        if (filterValueResolver == null) {
            this.filterValueResolver = filterValueResolver();
        }
        return filterValueResolver;
    }

    public FilterClass getFilterClass() {
        return filterClass;
    }

    private Supplier<Object> filterValueResolver() {
        if (filterValue instanceof CriteriaBuilder) {
            return () -> filterValue;
        }

        if (filterType == FilterType.LIKE) {
            return () -> IQueryBuilder.processFilterString(filterValue);
        }

        final Object result;
        String stringValue = filterValue.toString();

        if (filterClass == FilterClass.BOOLEAN) {
            result = Boolean.valueOf(stringValue);
        } else if (filterClass == FilterClass.LONG) {
            result = Long.valueOf(stringValue);
        } else if (filterClass == FilterClass.STRING) {
            return objectConverter(fieldResolver.getResolver(), filterValue);
        } else if (filterClass == FilterClass.UUID) {
            result = UUID.fromString(stringValue);
        } else {
            throw new IllegalArgumentException("Unsupported filter classname: " + filterClass);
        }

        return () -> result;
    }

}
