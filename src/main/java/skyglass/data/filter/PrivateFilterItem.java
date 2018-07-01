package skyglass.data.filter;

import java.util.List;
import java.util.function.Supplier;

import javax.persistence.criteria.CriteriaBuilder;

import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.query.FilterGroup;
import skyglass.query.model.query.InternalUtil;
import skyglass.query.model.query.QueryFilter;

public class PrivateFilterItem {

    private Class<?> rootClass;

    private FieldResolver fieldResolver;
    protected FilterType filterType;
    private Object filterValue;
    private Supplier<Object> filterValueResolver;
    private FilterGroup filterGroup = FilterGroup.Where;

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
    
    public boolean isTakesSingleValue() {
        return filterType.isTakesSingleValue();
    }

    public boolean isTakesListOfValues() {
        return filterType.isTakesListOfValues();
    }

    public boolean isTakesNoValue() {
        return filterType.isTakesNoValue();
    }

    public boolean isTakesSingleSubFilter() {
        return filterType.isTakesSingleSubFilter();
    }

    public boolean isTakesListOfSubFilters() {
        return filterType.isTakesListOfSubFilters();
    }

    public boolean isTakesNoProperty() {
        return filterType.isTakesNoProperty();
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

    @SuppressWarnings("unchecked")
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
            if (!(value instanceof List)) {
                return (operator == QueryFilter.OP_AND ? "AND: " : "OR: ") + "**INVALID VALUE - NOT A LIST: (" + value
                        + ") **";
            }

            String op = operator == QueryFilter.OP_AND ? " and " : " or ";

            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (Object o : ((List) value)) {
                if (first) {
                    first = false;
                } else {
                    sb.append(op);
                }
                if (o instanceof QueryFilter) {
                    sb.append(o.toString());
                } else {
                    sb.append("**INVALID VALUE - NOT A FILTER: (" + o + ") **");
                }
            }
            if (first)
                return (operator == QueryFilter.OP_AND ? "AND: " : "OR: ") + "**EMPTY LIST**";

            sb.append(")");
            return sb.toString();
        case QueryFilter.OP_NOT:
            if (!(value instanceof QueryFilter)) {
                return "NOT: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "not " + value.toString();
        case QueryFilter.OP_SOME:
            if (!(value instanceof QueryFilter)) {
                return "SOME: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "some `" + property + "` {" + value.toString() + "}";
        case QueryFilter.OP_ALL:
            if (!(value instanceof QueryFilter)) {
                return "ALL: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "all `" + property + "` {" + value.toString() + "}";
        case QueryFilter.OP_NONE:
            if (!(value instanceof QueryFilter)) {
                return "NONE: **INVALID VALUE - NOT A FILTER: (" + value + ") **";
            }
            return "none `" + property + "` {" + value.toString() + "}";
        default:
            return "**INVALID OPERATOR: (" + operator + ") - VALUE: " + InternalUtil.paramDisplayString(value) + " **";
        }
    }
    
}
