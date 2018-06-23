package skyglass.query.model.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import skyglass.data.filter.FilterType;

public class QueryFilter implements Serializable {

    private static final long serialVersionUID = 5234491851884212534L;

    public static final String ROOT_ENTITY = "";

    protected String property;

    protected Object value;

    protected FilterType filterType;

    private FilterGroup filterGroup = FilterGroup.Where;

    public QueryFilter() {
    }

    public QueryFilter(String property, Object value, FilterType operator) {
        this.property = property;
        this.value = value;
        this.operator = operator;
    }

    public QueryFilter(String property, Object value) {
        this.property = property;
        this.value = value;
        this.operator = OP_EQUAL;
    }

    public static final int OP_EQUAL = 0, OP_NOT_EQUAL = 1, OP_LESS_THAN = 2, OP_GREATER_THAN = 3, OP_LESS_OR_EQUAL = 4,
            OP_GREATER_OR_EQUAL = 5, OP_LIKE = 6, OP_ILIKE = 7, OP_IN = 8, OP_NOT_IN = 9, OP_NULL = 10,
            OP_NOT_NULL = 11, OP_EMPTY = 12, OP_NOT_EMPTY = 13, OP_RANGE = 14, OP_DOUBLE_RANGE = 15,
            OP_RANGE_STRING = 16, OP_DOUBLE_RANGE_STRING = 17, OP_LESS_OR_EQUAL_STRING = 18,
            OP_GREATER_OR_EQUAL_STRING = 19;
    public static final int OP_AND = 100, OP_OR = 101, OP_NOT = 102;
    public static final int OP_SOME = 200, OP_ALL = 201,
            OP_NONE = 202 /*
                           * not SOME
                           */;

    /**
     * Create a new SearchFilter using the == operator.
     */
    public static QueryFilter equal(String property, Object value) {
        return new QueryFilter(property, value, OP_EQUAL);
    }

    /**
     * Create a new SearchFilter using the < operator.
     */
    public static QueryFilter lessThan(String property, Object value) {
        return new QueryFilter(property, value, OP_LESS_THAN);
    }

    /**
     * Create a new SearchFilter using the > operator.
     */
    public static QueryFilter greaterThan(String property, Object value) {
        return new QueryFilter(property, value, OP_GREATER_THAN);
    }

    /**
     * Create a new SearchFilter using the <= operator.
     */
    public static QueryFilter lessOrEqual(String property, Object value) {
        return new QueryFilter(property, value, OP_LESS_OR_EQUAL);
    }

    /**
     * Create a new SearchFilter using the >= operator.
     */
    public static QueryFilter greaterOrEqual(String property, Object value) {
        return new QueryFilter(property, value, OP_GREATER_OR_EQUAL);
    }

    /**
     * Create a new SearchFilter using the <= operator for string.
     */
    public static QueryFilter lessOrEqualString(String property, Object value) {
        return new QueryFilter(property, value, OP_LESS_OR_EQUAL_STRING);
    }

    /**
     * Create a new SearchFilter using the >= operator for string.
     */
    public static QueryFilter greaterOrEqualString(String property, Object value) {
        return new QueryFilter(property, value, OP_GREATER_OR_EQUAL_STRING);
    }

    /**
     * Create a new SearchFilter using the IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static QueryFilter in(String property, Collection<?> value) {
        return new QueryFilter(property, value, OP_IN);
    }

    /**
     * Create a new SearchFilter using the IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static QueryFilter in(String property, Object... value) {
        return new QueryFilter(property, value, OP_IN);
    }

    /**
     * Create a new SearchFilter using the NOT IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static QueryFilter notIn(String property, Collection<?> value) {
        return new QueryFilter(property, value, OP_NOT_IN);
    }

    /**
     * Create a new SearchFilter using the NOT IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static QueryFilter notIn(String property, Object... value) {
        return new QueryFilter(property, value, OP_NOT_IN);
    }

    /**
     * Create a new SearchFilter using the LIKE operator.
     */
    public static QueryFilter like(String property, String value) {
        return new QueryFilter(property, value, OP_LIKE);
    }

    /**
     * Create a new SearchFilter using the ILIKE operator.
     */
    public static QueryFilter ilike(String property, String value) {
        return new QueryFilter(property, value, OP_ILIKE);
    }

    /**
     * Create a new SearchFilter using the != operator.
     */
    public static QueryFilter notEqual(String property, Object value) {
        return new QueryFilter(property, value, OP_NOT_EQUAL);
    }

    /**
     * Create a new SearchFilter using the IS NULL operator.
     */
    public static QueryFilter isNull(String property) {
        return new QueryFilter(property, true, OP_NULL);
    }

    /**
     * Create a new SearchFilter using the IS NOT NULL operator.
     */
    public static QueryFilter isNotNull(String property) {
        return new QueryFilter(property, true, OP_NOT_NULL);
    }

    /**
     * Create a new SearchFilter using the IS EMPTY operator.
     */
    public static QueryFilter isEmpty(String property) {
        return new QueryFilter(property, true, OP_EMPTY);
    }

    /**
     * Create a new SearchFilter using the IS NOT EMPTY operator.
     */
    public static QueryFilter isNotEmpty(String property) {
        return new QueryFilter(property, true, OP_NOT_EMPTY);
    }

    /**
     * Create a new SearchFilter using multiple value RANGE operator.
     */
    public static QueryFilter range(String property, Object min, Object max) {
        return QueryFilter.and(greaterOrEqual(property, min), lessOrEqual(property, max));
    }

    /**
     * Create a new SearchFilter using multiple value RANGE operator.
     */
    public static QueryFilter rangeString(String property, Object min, Object max) {
        return QueryFilter.and(greaterOrEqualString(property, min), lessOrEqualString(property, max));
    }

    /**
     * Create a new SearchFilter using the AND operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of
     * <code>SearchFilter</code>s can be specified.
     */
    public static QueryFilter and(QueryFilter... filters) {
        QueryFilter filter = new QueryFilter("AND", null, OP_AND);
        for (QueryFilter f : filters) {
            filter.add(f);
        }
        return filter;
    }

    /**
     * Create a new SearchFilter using the OR operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of
     * <code>Filter</code>s can be specified.
     */
    public static QueryFilter or(QueryFilter... filters) {
        QueryFilter filter = and(filters);
        filter.property = "OR";
        filter.operator = OP_OR;
        return filter;
    }

    /**
     * Create a new SearchFilter using the NOT operator.
     */
    public static QueryFilter not(QueryFilter filter) {
        return new QueryFilter("NOT", filter, OP_NOT);
    }

    /**
     * Create a new Filter using the SOME operator.
     */
    public static QueryFilter some(String property, QueryFilter filter) {
        return new QueryFilter(property, filter, OP_SOME);
    }

    /**
     * Create a new Filter using the ALL operator.
     */
    public static QueryFilter all(String property, QueryFilter filter) {
        return new QueryFilter(property, filter, OP_ALL);
    }

    /**
     * Create a new Filter using the NONE operator.
     */
    public static QueryFilter none(String property, QueryFilter filter) {
        return new QueryFilter(property, filter, OP_NONE);
    }

    /**
     * Used with OP_OR and OP_AND filters. These filters take a collection of
     * filters as their value. This method adds a filter to that list.
     */
    @SuppressWarnings("unchecked")
    public void add(QueryFilter filter) {
        if (value == null || !(value instanceof List)) {
            value = new ArrayList();
        }
        ((List) value).add(filter);
    }

    /**
     * Used with OP_OR and OP_AND filters. These filters take a collection of
     * filters as their value. This method removes a filter from that list.
     */
    @SuppressWarnings("unchecked")
    public void remove(QueryFilter filter) {
        if (value == null || !(value instanceof List)) {
            return;
        }
        ((List) value).remove(filter);
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public int getOperator() {
        return operator;
    }

    public void setOperator(int operator) {
        this.operator = operator;
    }

    /**
     * @return true if the operator should have a single value specified.
     * 
     *         <p>
     *         <code>EQUAL, NOT_EQUAL, LESS_THAN, LESS_OR_EQUAL, GREATER_THAN, GREATER_OR_EQUAL, LIKE, ILIKE</code>
     */
    public boolean isTakesSingleValue() {
        return operator <= 7;
    }

    /**
     * @return true if the operator should have a list of values specified.
     * 
     *         <p>
     *         <code>IN, NOT_IN</code>
     */
    public boolean isTakesListOfValues() {
        return operator == OP_IN || operator == OP_NOT_IN;
    }

    /**
     * @return true if the operator does not require a value to be specified.
     * 
     *         <p>
     *         <code>NULL, NOT_NULL, EMPTY, NOT_EMPTY</code>
     */
    public boolean isTakesNoValue() {
        return operator >= 10 && operator <= 13;
    }

    /**
     * @return true if the operator should have a single Filter specified for
     *         the value.
     * 
     *         <p>
     *         <code>NOT, ALL, SOME, NONE</code>
     */
    public boolean isTakesSingleSubFilter() {
        return operator == OP_NOT || operator >= 200;
    }

    /**
     * @return true if the operator should have a list of Filters specified for
     *         the value.
     * 
     *         <p>
     *         <code>AND, OR</code>
     */
    public boolean isTakesListOfSubFilters() {
        return operator == OP_AND || operator == OP_OR;
    }

    /**
     * @return true if the operator does not require a property to be specified.
     * 
     *         <p>
     *         <code>AND, OR, NOT</code>
     */
    public boolean isTakesNoProperty() {
        return operator >= 100 && operator <= 102;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + operator;
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
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
        QueryFilter other = (QueryFilter) obj;
        if (operator != other.operator)
            return false;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        switch (operator) {
        case QueryFilter.OP_IN:
            return "`" + property + "` in (" + InternalUtil.paramDisplayString(value) + ")";
        case QueryFilter.OP_NOT_IN:
            return "`" + property + "` not in (" + InternalUtil.paramDisplayString(value) + ")";
        case QueryFilter.OP_EQUAL:
            return "`" + property + "` = " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_NOT_EQUAL:
            return "`" + property + "` != " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_GREATER_THAN:
            return "`" + property + "` > " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_LESS_THAN:
            return "`" + property + "` < " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_GREATER_OR_EQUAL:
            return "`" + property + "` >= " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_LESS_OR_EQUAL:
            return "`" + property + "` <= " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_LIKE:
            return "`" + property + "` LIKE " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_ILIKE:
            return "`" + property + "` ILIKE " + InternalUtil.paramDisplayString(value);
        case QueryFilter.OP_NULL:
            return "`" + property + "` IS NULL";
        case QueryFilter.OP_NOT_NULL:
            return "`" + property + "` IS NOT NULL";
        case QueryFilter.OP_EMPTY:
            return "`" + property + "` IS EMPTY";
        case QueryFilter.OP_NOT_EMPTY:
            return "`" + property + "` IS NOT EMPTY";
        case QueryFilter.OP_AND:
        case QueryFilter.OP_OR:
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

    public void setHavingType() {
        this.filterType = FilterGroup.Having;
    }

    public boolean isHavingType() {
        return this.filterType == FilterGroup.Having;
    }

}
