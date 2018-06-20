package skyglass.data.filter;

import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;

import skyglass.query.model.criteria.IQueryBuilder;

public class PrivateFilterItem {

    private Class<?> rootClass;

    private FieldResolver fieldResolver;
    private FilterType filterType;
    private Object filterValue;
    private Supplier<Object> filterValueResolver;

    // should be overriden to define different behaviour
    protected Supplier<Object> objectConverter(String fieldName, Object value) {
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

    public Supplier<Object> getFilterValueResolver() {
        if (filterValue == null) {
            return () -> null;
        }
        if (filterValueResolver == null) {
            this.filterValueResolver = filterValueResolver();
        }
        return filterValueResolver;
    }

    private Supplier<Object> filterValueResolver() {
        if (filterValue instanceof CriteriaBuilder) {
            return () -> filterValue;
        }
        if (filterType == FilterType.Like) {
            return () -> IQueryBuilder.processFilterString(filterValue);
        }
        Object result = objectConverter(fieldResolver.getResolver(), filterValue);
        return () -> result;
    }

}
