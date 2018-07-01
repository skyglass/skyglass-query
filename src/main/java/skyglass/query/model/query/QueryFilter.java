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



    public QueryFilter() {
    }

    public QueryFilter(String property, Object value, FilterType filterType) {
        this.property = property;
        this.value = value;
        this.filterType = filterType;
    }

    public QueryFilter(String property, Object value) {
        this.property = property;
        this.value = value;
        this.filterType = OP_EQUAL;
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



}
