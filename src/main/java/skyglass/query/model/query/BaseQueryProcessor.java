package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skyglass.query.metadata.Metadata;
import skyglass.query.metadata.MetadataHelper;

/**
 * This class provides two methods for generating query language to fulfill an
 * <code>ISearch</code>.
 * <ol>
 * <li><code>generateQL()</code> - is used for getting the actual search
 * results.</li>
 * <li><code>generateRowCountQL()</code> - is used for getting just the number
 * of results.</li>
 * </ol>
 * Both methods return a query language string and a list of values for filling
 * named parameters. For example the following query and parameter list might be
 * returned:
 * 
 * <pre>
 * select _it from com.example.Cat _it
 *   where _it.age &gt; :p1 and _it.name != :p2
 *   
 * parameter list: [3, 'Mittens']
 * </pre>
 * 
 * This is an abstract class. A subclass must be used to implement individual
 * query languages. Currently only JPQL query language is supported (
 * <code>skyglass.data.criteria.impl.search.JPASearchToQLProcessor</code>).
 * That implementation could be used for EQL and HQL query language as well with
 * no or minor modifications.
 */
public abstract class BaseQueryProcessor {

    private static Logger logger = LoggerFactory.getLogger(BaseQueryProcessor.class);

    protected static int QLTYPE_JPQL = 0;
    protected static int QLTYPE_HQL = 1;
    protected static int QLTYPE_EQL = 1;

    protected int qlType;

    protected MetadataHelper metadataHelper;

    protected String rootAlias = "_it";

    protected static final String ROOT_PATH = "";
    
    protected ISearchQuery searchQuery;
    
    protected Class<?> searchClass;
    
    protected List<Object> paramList = new ArrayList<Object>();

    protected BaseQueryProcessor(int qlType, MetadataHelper metadataHelper, ISearchQuery searchQuery) {
        if (metadataHelper == null) {
            throw new IllegalArgumentException("A SearchProcessor cannot be initialized with a null MetadataHelper.");
        }
        this.qlType = qlType;
        this.metadataHelper = metadataHelper;
        this.searchQuery = searchQuery;
        this.searchClass = searchQuery.getSearchClass();
    }

    /**
     * The MetadataHelper used by this search processor. This can only be set in
     * the constructor.
     */
    public MetadataHelper getMetadataHelper() {
        return metadataHelper;
    }

    /**
     * This is the string used to represent the root entity of the search within
     * the query. The default value is <code>"_it"</code>. It may be necessary
     * to use a different alias if there are entities in the data model with the
     * name or property "_it".
     */
    public void setRootAlias(String alias) {
        this.rootAlias = alias;
    }

    /**
     * Generate the QL string for a given search. Fill paramList with the values
     * to be used for the query. All parameters within the query string are
     * specified as named parameters ":pX", where X is the index of the
     * parameter value in paramList.
     */
    public String generateQL() {
        if (searchClass == null)
            throw new NullPointerException("The entity class for a search cannot be null");

        SearchContext ctx = new SearchContext(searchClass, rootAlias, paramList);

        List<Field> fields = checkAndCleanFields(searchQuery.getFields());

        String select = generateSelectClause(ctx, fields, searchQuery.isDistinct());
        String where = generateWhereClause(ctx, checkAndCleanFilters(searchQuery.getFilters()), searchQuery.isDisjunction());
        String orderBy = generateOrderByClause(ctx, checkAndCleanSorts(searchQuery.getSorts()));
        applyFetches(ctx, checkAndCleanFetches(searchQuery.getFetches()), fields);
        String from = generateFromClause(ctx, true);

        StringBuilder sb = new StringBuilder();
        sb.append(select);
        sb.append(from);
        sb.append(where);
        sb.append(orderBy);

        String query = sb.toString();
        if (logger.isDebugEnabled())
            logger.debug("generateQL:\n  " + query);
        return query;
    }

    /**
     * Generate the QL string that will query the total number of results from a
     * given search (paging is ignored). Fill paramList with the values to be
     * used for the query. All parameters within the query string are specified
     * as named parameters ":pX", where X is the index of the parameter value in
     * paramList.
     * 
     * <b>NOTE:</b> Returns null if column operators are used in the search.
     * Such a search will always return 1 row.
     */
    public String generateRowCountQL() {
        if (searchClass == null)
            throw new NullPointerException("The entity class for a search cannot be null");

        SearchContext ctx = new SearchContext(searchClass, rootAlias, paramList);

        String where = generateWhereClause(ctx, checkAndCleanFilters(searchQuery.getFilters()), searchQuery.isDisjunction());
        String from = generateFromClause(ctx, false);

        boolean useOperator = false, notUseOperator = false;
        List<Field> fields = searchQuery.getFields();
        if (fields != null) {
            for (Field field : fields) {
                switch (field.getOperator()) {
                case Field.OP_AVG:
                case Field.OP_COUNT:
                case Field.OP_COUNT_DISTINCT:
                case Field.OP_MAX:
                case Field.OP_MIN:
                case Field.OP_SUM:
                    useOperator = true;
                    break;
                default:
                    notUseOperator = true;
                    break;
                }
            }
        }
        if (useOperator && notUseOperator) {
            throw new Error("A search can not have a mix of fields with operators and fields without operators.");
        } else if (useOperator) {
            return null; // if we're using column operators, the query will
                         // always return 1 result.
        }

        StringBuilder sb = new StringBuilder();
        if (!searchQuery.isDistinct()) {
            sb.append("select count(*)");
        } else if (fields.size() == 0) {
            sb.append("select count(distinct ");
            sb.append(rootAlias).append(".id)");
        } else if (fields.size() == 1) {
            sb.append("select count(distinct ");
            String prop = fields.get(0).getProperty();
            if (prop == null || "".equals(prop)) {
                sb.append(ctx.getRootAlias());
            } else {
                sb.append(getPathRef(ctx, prop));
            }
            sb.append(")");
        } else {
            throw new IllegalArgumentException("Unfortunately, JPA Generic DAO does not currently support "
                    + "the count operation on a search that has distinct set with multiple fields.");
        }
        sb.append(from);
        sb.append(where);

        String query = sb.toString();
        if (logger.isDebugEnabled())
            logger.debug("generateRowCountQL:\n  " + query);
        return query;
    }

    /**
     * Internal method for generating the select clause based on the fields of
     * the given search.
     */
    protected String generateSelectClause(SearchContext ctx, List<Field> fields, boolean distinct) {

        StringBuilder sb = null;
        boolean useOperator = false, notUseOperator = false;
        boolean first = true;

        if (fields != null) {
            for (Field field : fields) {
                if (first) {
                    sb = new StringBuilder("select ");
                    if (distinct)
                        sb.append("distinct ");
                    first = false;
                } else {
                    sb.append(", ");
                }

                String prop;
                if (field.getProperty() == null || "".equals(field.getProperty())) {
                    prop = ctx.getRootAlias();
                } else {
                    prop = getPathRef(ctx, field.getProperty());
                }

                switch (field.getOperator()) {
                case Field.OP_AVG:
                    sb.append("avg(");
                    useOperator = true;
                    break;
                case Field.OP_COUNT:
                    sb.append("count(");
                    useOperator = true;
                    break;
                case Field.OP_COUNT_DISTINCT:
                    sb.append("count(distinct ");
                    useOperator = true;
                    break;
                case Field.OP_MAX:
                    sb.append("max(");
                    useOperator = true;
                    break;
                case Field.OP_MIN:
                    sb.append("min(");
                    useOperator = true;
                    break;
                case Field.OP_SUM:
                    sb.append("sum(");
                    useOperator = true;
                    break;
                default:
                    notUseOperator = true;
                    break;
                }
                sb.append(prop);
                if (useOperator) {
                    sb.append(")");
                }
            }
        }
        if (first) {
            // there are no fields
            if (distinct)
                return "select distinct " + ctx.getRootAlias();
            else
                return "select " + ctx.getRootAlias();
        }
        if (useOperator && notUseOperator) {
            throw new Error("A search can not have a mix of fields with operators and fields without operators.");
        }
        return sb.toString();
    }

    /**
     * Apply the fetch list to the alias tree in the search context.
     */
    protected void applyFetches(SearchContext ctx, List<String> fetches, List<Field> fields) {
        if (fetches != null) {
            // apply fetches
            boolean hasFetches = false, hasFields = false;
            for (String fetch : fetches) {
                getAlias(ctx, fetch, true);
                hasFetches = true;
            }
            if (hasFetches && fields != null) {
                // don't fetch nodes whose ancestors aren't found in the select
                // clause
                List<String> fieldProps = new ArrayList<String>();
                for (Field field : fields) {
                    if (field.getOperator() == Field.OP_PROPERTY) {
                        fieldProps.add(field.getProperty() + ".");
                    }
                    hasFields = true;
                }
                if (hasFields) {
                    for (AliasNode node : ctx.aliases.values()) {
                        if (node.fetch) {
                            // make sure it has an ancestor in the select clause
                            boolean hasAncestor = false;
                            for (String field : fieldProps) {
                                if (node.getFullPath().startsWith(field)) {
                                    hasAncestor = true;
                                    break;
                                }
                            }
                            if (!hasAncestor)
                                node.fetch = false;
                        }
                    }
                }
            }
        }
    }

    /**
     * Internal method for generating from clause. This method should be called
     * after generating other clauses because it relies on the aliases they
     * create. This method takes every path that is called for in the other
     * clauses and makes it available as an alias using left joins. It also adds
     * join fetching for properties specified by <code>fetches</code> if
     * <code>doEagerFetching</code> is <code>true</code>. <b>NOTE:</b> When
     * using eager fetching, <code>applyFetches()</code> must be executed first.
     */
    protected String generateFromClause(SearchContext ctx, boolean doEagerFetching) {
        StringBuilder sb = new StringBuilder(" from ");
        sb.append(ctx.rootClass.getName());
        sb.append(" ");
        sb.append(ctx.getRootAlias());
        sb.append(generateJoins(ctx, doEagerFetching));
        return sb.toString();
    }

    /**
     * Internal method for generating the join portion of the from clause. This
     * method should be called after generating other clauses because it relies
     * on the aliases they create. This method takes every path that is called
     * for in the other clauses and makes it available as an alias using left
     * joins. It also adds join fetching for properties specified by
     * <code>fetches</code> if <code>doEagerFetching</code> is <code>true</code>
     * . <b>NOTE:</b> When using eager fetching, <code>applyFetches()</code>
     * must be executed first.
     */
    protected String generateJoins(SearchContext ctx, boolean doEagerFetching) {
        StringBuilder sb = new StringBuilder();

        // traverse alias graph breadth-first
        Queue<AliasNode> queue = new LinkedList<AliasNode>();
        queue.offer(ctx.aliases.get(ROOT_PATH));
        while (!queue.isEmpty()) {
            AliasNode node = queue.poll();
            if (node.parent != null) {
                sb.append(" left join ");
                if (doEagerFetching && node.fetch)
                    sb.append("fetch ");
                sb.append(node.parent.alias);
                sb.append(".");
                sb.append(node.property);
                sb.append(" as ");
                sb.append(node.alias);
            }
            for (AliasNode child : node.children) {
                queue.offer(child);
            }
        }

        return sb.toString();
    }

    /**
     * Internal method for generating order by clause. Uses sort options from
     * search.
     */
    protected String generateOrderByClause(SearchContext ctx, List<Sort> sorts) {
        if (sorts == null)
            return "";

        StringBuilder sb = null;
        boolean first = true;
        for (Sort sort : sorts) {
            if (first) {
                sb = new StringBuilder(" order by ");
                first = false;
            } else {
                sb.append(", ");
            }
            if (sort.isIgnoreCase() && metadataHelper.get(ctx.rootClass, sort.getProperty()).isString()) {
                sb.append("lower(");
                sb.append(getPathRef(ctx, sort.getProperty()));
                sb.append(")");
            } else {
                sb.append(getPathRef(ctx, sort.getProperty()));
            }
            sb.append(sort.isDesc() ? " desc" : " asc");
        }
        if (first) {
            return "";
        }
        return sb.toString();
    }

    /**
     * Internal method for generating where clause for given search. Uses filter
     * options from search.
     */
    protected String generateWhereClause(SearchContext ctx, List<QueryFilter> filters, boolean isDisjunction) {
        String content = null;
        if (filters == null || filters.size() == 0) {
            return "";
        } else if (filters.size() == 1) {
            content = filterToQL(ctx, filters.get(0));
        } else {
            QueryFilter junction = new QueryFilter(null, filters,
                    isDisjunction ? QueryFilter.OP_OR : QueryFilter.OP_AND);
            content = filterToQL(ctx, junction);
        }

        return (content == null) ? "" : " where " + content;
    }

    /**
     * Recursively generate the QL fragment for a given search filter option.
     */
    @SuppressWarnings("rawtypes")
	protected String filterToQL(SearchContext ctx, QueryFilter filter) {
        String property = filter.getProperty();
        Object value = filter.getValue();
        int operator = filter.getOperator();

        // If the operator needs a value and no value is specified, ignore this
        // filter.
        // Only NULL, NOT_NULL, EMPTY and NOT_EMPTY do not need a value.
        if (value == null && !filter.isTakesNoValue()) {
            return null;
        }

        // for IN and NOT IN, if value is empty list, return false, and true
        // respectively
        if (operator == QueryFilter.OP_IN || operator == QueryFilter.OP_NOT_IN) {
            if (value instanceof Collection && ((Collection) value).size() == 0) {
                return operator == QueryFilter.OP_IN ? "1 = 2" : "1 = 1";
            }
            if (value instanceof Object[] && ((Object[]) value).length == 0) {
                return operator == QueryFilter.OP_IN ? "1 = 2" : "1 = 1";
            }
        }

        // convert numbers to the expected type if needed (ex: Integer to Long)
        if (filter.isTakesListOfValues()) {
            value = prepareValue(ctx.rootClass, property, value, true);
        } else if (filter.isTakesSingleValue()) {
            value = prepareValue(ctx.rootClass, property, value, false);
        }

        Metadata metadata;

        switch (operator) {
        case QueryFilter.OP_NULL:
            return getPathRef(ctx, property) + " is null";
        case QueryFilter.OP_NOT_NULL:
            return getPathRef(ctx, property) + " is not null";
        case QueryFilter.OP_IN:
            return getPathRef(ctx, property) + " in (" + param(ctx, value) + ")";
        case QueryFilter.OP_NOT_IN:
            return getPathRef(ctx, property) + " not in (" + param(ctx, value) + ")";
        case QueryFilter.OP_EQUAL:
            return getPathRef(ctx, property) + " = " + param(ctx, value);
        case QueryFilter.OP_NOT_EQUAL:
            return getPathRef(ctx, property) + " != " + param(ctx, value);
        case QueryFilter.OP_GREATER_THAN:
            return getPathRef(ctx, property) + " > " + param(ctx, value);
        case QueryFilter.OP_LESS_THAN:
            return getPathRef(ctx, property) + " < " + param(ctx, value);
        case QueryFilter.OP_GREATER_OR_EQUAL:
            return getPathRef(ctx, property) + " >= " + param(ctx, value);
        case QueryFilter.OP_LESS_OR_EQUAL:
            return getPathRef(ctx, property) + " <= " + param(ctx, value);
        case QueryFilter.OP_GREATER_OR_EQUAL_STRING:
            return "LOWER(" + getPathRef(ctx, property) + ") >= LOWER(" + param(ctx, value) + ")";
        case QueryFilter.OP_LESS_OR_EQUAL_STRING:
            return "LOWER(" + getPathRef(ctx, property) + ") <= LOWER(" + param(ctx, value) + ")";
        case QueryFilter.OP_LIKE:
            return getPathRef(ctx, property) + " like " + param(ctx, value.toString());
        case QueryFilter.OP_ILIKE:
            return "lower(" + getPathRef(ctx, property) + ") like lower(" + param(ctx, value.toString()) + ")";
        case QueryFilter.OP_AND:
        case QueryFilter.OP_OR:
            if (!(value instanceof List)) {
                return null;
            }

            String op = filter.getOperator() == QueryFilter.OP_AND ? " and " : " or ";

            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (Object o : ((List) value)) {
                if (o instanceof QueryFilter) {
                    String filterStr = filterToQL(ctx, (QueryFilter) o);
                    if (filterStr != null) {
                        if (first) {
                            first = false;
                        } else {
                            sb.append(op);
                        }
                        sb.append(filterStr);
                    }
                }
            }
            if (first)
                return null;

            sb.append(")");
            return sb.toString();
        case QueryFilter.OP_NOT:
            if (!(value instanceof QueryFilter)) {
                return null;
            }
            String filterStr = filterToQL(ctx, (QueryFilter) value);
            if (filterStr == null)
                return null;

            return "not " + filterStr;
        case QueryFilter.OP_EMPTY:
            metadata = metadataHelper.get(ctx.rootClass, property);
            if (metadata.isCollection()) {
                return "not exists elements(" + getPathRef(ctx, property) + ")";
            } else if (metadata.isString()) {
                String pathRef = getPathRef(ctx, property);
                return "(" + pathRef + " is null or " + pathRef + " = '')";
            } else {
                return getPathRef(ctx, property) + " is null";
            }
        case QueryFilter.OP_NOT_EMPTY:
            metadata = metadataHelper.get(ctx.rootClass, property);
            if (metadata.isCollection()) {
                return "exists elements(" + getPathRef(ctx, property) + ")";
            } else if (metadata.isString()) {
                String pathRef = getPathRef(ctx, property);
                return "(" + pathRef + " is not null and " + pathRef + " != '')";
            } else {
                return getPathRef(ctx, property) + " is not null";
            }
        case QueryFilter.OP_SOME:
            if (!(value instanceof QueryFilter)) {
                return null;
            } else if (value instanceof QueryFilter) {
                String simple = generateSimpleAllOrSome(ctx, property, (QueryFilter) value, "some");
                if (simple != null) {
                    return simple;
                } else {
                    return "exists " + generateSubquery(ctx, property, (QueryFilter) value);
                }
            }
        case QueryFilter.OP_ALL:
            if (!(value instanceof QueryFilter)) {
                return null;
            } else if (value instanceof QueryFilter) {
                String simple = generateSimpleAllOrSome(ctx, property, (QueryFilter) value, "all");
                if (simple != null) {
                    return simple;
                } else {
                    return "not exists " + generateSubquery(ctx, property, negate((QueryFilter) value));
                }
            }
        case QueryFilter.OP_NONE:
            if (!(value instanceof QueryFilter)) {
                return null;
            } else if (value instanceof QueryFilter) {
                // NOTE: Using "all" for the simple all or some is logically
                // incorrect. It should be "some". However,
                // because of a bug in how Hibernate 3.1.1 tries to simplify
                // "not ... some/all ...) it actually ends
                // up working as desired. TODO: If and when the Hibernate bug is
                // fixed, this should be switched to "some".
                String simple = generateSimpleAllOrSome(ctx, property, (QueryFilter) value, "all");
                if (simple != null) {
                    return "not ( " + simple + " )";
                } else {
                    return "not exists " + generateSubquery(ctx, property, (QueryFilter) value);
                }
            }
        default:
            throw new IllegalArgumentException("Filter comparison ( " + operator + " ) is invalid.");
        }
    }

    /**
     * Generate a QL string for a subquery on the given property that uses the
     * given filter. This is used by SOME, ALL and NONE filters.
     * 
     * @param ctx
     *            - a new context just for this sub-query
     * @param property
     *            - the property of the main query that points to the collection
     *            on which to query
     * @param filter
     *            - the filter to use for the where clause of the sub-query
     */
    protected String generateSubquery(SearchContext ctx, String property, QueryFilter filter) {
        SearchContext ctx2 = new SearchContext();
        ctx2.rootClass = metadataHelper.get(ctx.rootClass, property).getJavaClass();
        ctx2.setRootAlias(rootAlias + (ctx.nextSubqueryNum++));
        ctx2.paramList = ctx.paramList;
        ctx2.nextAliasNum = ctx.nextAliasNum;
        ctx2.nextSubqueryNum = ctx.nextSubqueryNum;

        List<QueryFilter> filters = new ArrayList<QueryFilter>(1);
        filters.add(filter);
        String where = generateWhereClause(ctx2, filters, false);
        String joins = generateJoins(ctx2, false);
        ctx.nextAliasNum = ctx2.nextAliasNum;
        ctx.nextSubqueryNum = ctx2.nextSubqueryNum;

        StringBuilder sb = new StringBuilder();
        sb.append("(from ");
        sb.append(getPathRef(ctx, property));
        sb.append(" ");
        sb.append(ctx2.getRootAlias());
        sb.append(joins);
        sb.append(where);
        sb.append(")");

        return sb.toString();
    }

    /**
     * <p>
     * In the case of simple ALL/SOME/NONE filters, a simpler jpql syntax is
     * used (which is also compatible with collections of values). Simple
     * filters include ALL/SOME/NONE filters with exactly one sub-filter where
     * that filter applies to the elements of the collection directly (as
     * opposed to their properties) and the operator is =, !=, <, <=, >, or >=.
     * 
     * <p>
     * For example:
     * 
     * <pre>
     * Filter.some(&quot;some_collection_of_strings&quot;, Filter.equal(&quot;&quot;, &quot;Bob&quot;)
     * Filter.all(&quot;some_collection_of_numbers&quot;, Filter.greaterThan(null, 23)
     * </pre>
     * 
     * If the filter meets these criteria as a simple ALL/SOME/NONE filter, the
     * QL string for the filter will be returned. If not, <code>null</code> is
     * returned.
     * 
     * @param ctx
     *            - the context of the SOME/ALL/NONE filter
     * @param property
     *            - the property of the SOME/ALL/NONE filter
     * @param filter
     *            - the sub-filter that is the value of the SOME/ALL/NONE filter
     * @param operation
     *            - a string used to fill in the collection operation. The value
     *            should be either "some" or "all".
     */
    protected String generateSimpleAllOrSome(SearchContext ctx, String property, QueryFilter filter,
            String operation) {
        if (filter.getProperty() != null && !filter.getProperty().equals(""))
            return null;

        String op;

        switch (filter.getOperator()) {
        case QueryFilter.OP_EQUAL:
            op = " = ";
            break;
        case QueryFilter.OP_NOT_EQUAL:
            op = " != ";
            break;
        case QueryFilter.OP_LESS_THAN:
            op = " > ";
            break;
        case QueryFilter.OP_LESS_OR_EQUAL:
            op = " >= ";
            break;
        case QueryFilter.OP_GREATER_THAN:
            op = " < ";
            break;
        case QueryFilter.OP_GREATER_OR_EQUAL:
            op = " <= ";
            break;
        default:
            return null;
        }

        Object value = InternalUtil.convertIfNeeded(filter.getValue(),
                metadataHelper.get(ctx.rootClass, property).getJavaClass());
        return param(ctx, value) + op + operation + " elements(" + getPathRef(ctx, property) + ")";
    }

    /**
     * Convert a property value to the expected type for that property. Ex. a
     * Long to and Integer.
     * 
     * @param isCollection
     *            <code>true</code> if the value is a collection of values, for
     *            example with IN and NOT_IN operators.
     * @return the converted value.
     */
    @SuppressWarnings("rawtypes")
    protected Object prepareValue(Class<?> rootClass, String property, Object value, boolean isCollection) {
        if (value == null)
            return null;

        Class<?> expectedClass;
        if (property != null && ("class".equals(property) || property.endsWith(".class"))) {
            expectedClass = Class.class;
        } else if (property != null && ("size".equals(property) || property.endsWith(".size"))) {
            expectedClass = Integer.class;
        } else {
            expectedClass = metadataHelper.get(rootClass, property).getJavaClass();
        }

        // convert numbers to the expected type if needed (ex: Integer to Long)
        if (isCollection) {
            // Check each element in the collection.
            Object[] val2;

            if (value instanceof Collection) {
                val2 = new Object[((Collection) value).size()];
                int i = 0;
                for (Object item : (Collection) value) {
                    val2[i++] = InternalUtil.convertIfNeeded(item, expectedClass);
                }
            } else {
                val2 = new Object[((Object[]) value).length];
                int i = 0;
                for (Object item : (Object[]) value) {
                    val2[i++] = InternalUtil.convertIfNeeded(item, expectedClass);
                }
            }
            return val2;
        } else {
            return InternalUtil.convertIfNeeded(value, expectedClass);
        }
    }

    /**
     * Return a filter that negates the given filter.
     */
    protected QueryFilter negate(QueryFilter filter) {
        return QueryFilter.not(addExplicitNullChecks(filter));
    }

    /**
     * Used by {@link #negate(QueryFilter)}. There's a complication with null
     * values in the database so that !(x == 1) is not the opposite of (x == 1).
     * Rather !(x == 1 and x != null) is the same as (x == 1). This method
     * applies the null check explicitly to all filters included in the given
     * filter tree.
     */
    protected QueryFilter addExplicitNullChecks(QueryFilter filter) {
        return QueryUtil.walkFilter(filter, new QueryUtil.FilterVisitor() {
            @Override
            public QueryFilter visitAfter(QueryFilter filter) {
                if (filter.isTakesSingleValue() || filter.isTakesListOfValues()) {
                    return QueryFilter.and(filter, QueryFilter.isNotNull(filter.getProperty()));
                } else {
                    return filter;
                }
            }
        }, false);

    }

    /**
     * Add value to paramList and return the named parameter string ":pX".
     */
    @SuppressWarnings("rawtypes")
	protected String param(SearchContext ctx, Object value) {
        if (value instanceof Class) {
            return ((Class<?>) value).getName();
        }

        if (value instanceof Collection) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Collection) value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                ctx.paramList.add(o);
                sb.append(":p");
                sb.append(Integer.toString(ctx.paramList.size()));
            }
            return sb.toString();
        } else if (value instanceof Object[]) {
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for (Object o : (Object[]) value) {
                if (first) {
                    first = false;
                } else {
                    sb.append(",");
                }
                ctx.paramList.add(o);
                sb.append(":p");
                sb.append(Integer.toString(ctx.paramList.size()));
            }
            return sb.toString();
        } else {
            ctx.paramList.add(value);
            return ":p" + Integer.toString(ctx.paramList.size());
        }
    }

    /**
     * Given a full path to a property (ex. department.manager.salary), return
     * the reference to that property that uses the appropriate alias (ex.
     * a4_manager.salary).
     */
    protected String getPathRef(SearchContext ctx, String path) {
        if (path == null || "".equals(path)) {
            return ctx.getRootAlias();
        }

        String[] parts = splitPath(ctx, path);

        return getAlias(ctx, parts[0], false).alias + "." + parts[1];
    }

    /**
     * Split a path into two parts. The first part will need to be aliased. The
     * second part will be a property of that alias. For example:
     * (department.manager.salary) would return [department.manager, salary].
     */
    protected String[] splitPath(SearchContext ctx, String path) {
        if (path == null || "".equals(path))
            return null;

        int pos = path.lastIndexOf('.');

        if (pos == -1) {
            return new String[] { "", path };
        } else {
            String lastSegment = path.substring(pos + 1);
            String currentPath = path;
            boolean first = true;

            // Basically gobble up as many segments as possible until we come to
            // an entity. Entities must become aliases so we can use our left
            // join feature.
            // The exception is that if a segment is an id, we want to skip the
            // entity preceding it because (entity.id) is actually stored in the
            // same table as the foreign key.
            while (true) {
                if (metadataHelper.isId(ctx.rootClass, currentPath)) {
                    // if it's an id property
                    // skip one segment
                    if (pos == -1) {
                        return new String[] { "", path };
                    }
                    pos = currentPath.lastIndexOf('.', pos - 1);
                } else if (!first && metadataHelper.get(ctx.rootClass, currentPath).isEntity()) {
                    // when we reach an entity (excluding the very first
                    // segment), we're done
                    return new String[] { currentPath, path.substring(currentPath.length() + 1) };
                }
                first = false;

                // For size, we need to go back to the 'first' behavior
                // for the next segment.
                if (pos != -1 && lastSegment.equals("size")
                        && metadataHelper.get(ctx.rootClass, currentPath.substring(0, pos)).isCollection()) {
                    first = true;
                }

                // if that was the last segment, we're done
                if (pos == -1) {
                    return new String[] { "", path };
                }
                // proceed to the next segment
                currentPath = currentPath.substring(0, pos);
                pos = currentPath.lastIndexOf('.');
                if (pos == -1) {
                    lastSegment = currentPath;
                } else {
                    lastSegment = currentPath.substring(pos + 1);
                }
            }

        }

        // 1st
        // if "id", go 2; try again
        // if component, go 1; try again
        // if entity, go 1; try again
        // if size, go 1; try 1st again

        // successive
        // if "id", go 2; try again
        // if component, go 1; try again
        // if entity, stop
    }

    /**
     * Given a full path to an entity (ex. department.manager), return the alias
     * to reference that entity (ex. a4_manager). If there is no alias for the
     * given path, one will be created.
     */
    protected AliasNode getAlias(SearchContext ctx, String path, boolean setFetch) {
        if (path == null || path.equals("")) {
            return ctx.aliases.get(ROOT_PATH);
        } else if (ctx.aliases.containsKey(path)) {
            AliasNode node = ctx.aliases.get(path);
            if (setFetch) {
                while (node.parent != null) {
                    node.fetch = true;
                    node = node.parent;
                }
            }

            return node;
        } else {
            String[] parts = splitPath(ctx, path);

            int pos = parts[1].lastIndexOf('.');

            String alias = "a" + (ctx.nextAliasNum++) + "_" + (pos == -1 ? parts[1] : parts[1].substring(pos + 1));

            AliasNode node = new AliasNode(parts[1], alias);

            // set up path recursively
            getAlias(ctx, parts[0], setFetch).addChild(node);

            node.fetch = setFetch;
            ctx.aliases.put(path, node);

            return node;
        }
    }

    protected static final class AliasNode {
        String property;
        String alias;
        boolean fetch;
        AliasNode parent;
        List<AliasNode> children = new ArrayList<AliasNode>();

        AliasNode(String property, String alias) {
            this.property = property;
            this.alias = alias;
        }

        void addChild(AliasNode node) {
            children.add(node);
            node.parent = this;
        }

        public String getFullPath() {
            if (parent == null)
                return "";
            else if (parent.parent == null)
                return property;
            else
                return parent.getFullPath() + "." + property;
        }
    }

    protected static final class SearchContext {
        Class<?> rootClass;
        Map<String, AliasNode> aliases = new HashMap<String, AliasNode>();
        List<Object> paramList;

        int nextAliasNum = 1;
        int nextSubqueryNum = 1;

        public SearchContext() {
        }

        public SearchContext(Class<?> rootClass, String rootAlias, List<Object> paramList) {
            this.rootClass = rootClass;
            setRootAlias(rootAlias);
            this.paramList = paramList;
        }

        public void setRootAlias(String rootAlias) {
            this.aliases.put(ROOT_PATH, new AliasNode(ROOT_PATH, rootAlias));
        }

        public String getRootAlias() {
            return this.aliases.get(ROOT_PATH).alias;
        }
    }

    // ---- SECURITY CHECK ---- //

    /**
     * <ol>
     * <li>Check for injection attack in property strings.
     * <li>The field list may not contain nulls.
     * </ol>
     */
    protected List<Field> checkAndCleanFields(List<Field> fields) {
        if (fields == null)
            return null;

        for (Field field : fields) {
            if (field == null) {
                throw new IllegalArgumentException("The search contains a null field.");
            }
            if (field.getProperty() != null)
                securityCheckProperty(field.getProperty());
        }

        return fields;
    }

    /**
     * <ol>
     * <li>Check for injection attack in property strings.
     * <li>Remove null fetches from the list.
     * </ol>
     */
    protected List<String> checkAndCleanFetches(List<String> fetches) {
        return QueryUtil.walkList(fetches, new QueryUtil.ItemVisitor<String>() {
            @Override
            public String visit(String fetch) {
                securityCheckProperty(fetch);
                return fetch;
            }
        }, true);
    }

    /**
     * <ol>
     * <li>Check for injection attack in property strings.
     * <li>Remove null sorts from the list.
     * </ol>
     */
    protected List<Sort> checkAndCleanSorts(List<Sort> sorts) {
        return QueryUtil.walkList(sorts, new QueryUtil.ItemVisitor<Sort>() {
            @Override
            public Sort visit(Sort sort) {
                securityCheckProperty(sort.getProperty());
                return sort;
            }
        }, true);
    }

    /**
     * <ol>
     * <li>Check for injection attack in property strings.
     * <li>Check for values that are incongruent with the operator.
     * <li>Remove null filters from the list.
     * <li>Simplify out junctions (and/or) that have only one sub-filter.
     * <li>Remove filters that require sub-filters but have none
     * (and/or/not/some/all/none)
     * </ol>
     */
    public static List<QueryFilter> checkAndCleanFilters(List<QueryFilter> filters) {
        return QueryUtil.walkFilters(filters, new QueryUtil.FilterVisitor() {
            @SuppressWarnings({ "rawtypes" })
			@Override
            public QueryFilter visitBefore(QueryFilter filter) {
                if (filter != null && filter.getValue() != null) {
                    if (filter.isTakesListOfSubFilters()) {
                        // make sure that filters that take lists of filters
                        // actually have lists of filters for their values
                        if (filter.getValue() instanceof List) {
                            for (Object o : (List) filter.getValue()) {
                                if (!(o instanceof QueryFilter)) {
                                    throw new IllegalArgumentException("The search has a filter (" + filter
                                            + ") for which the value should be a List of Filters but there is an element in the list that is of type: "
                                            + o.getClass());
                                }
                            }
                        } else {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be a List of Filters but is not a list. The actual type is "
                                    + filter.getValue().getClass());
                        }
                    } else if (filter.isTakesSingleSubFilter()) {
                        // make sure filters that take filters actually have
                        // filters for their values
                        if (!(filter.getValue() instanceof QueryFilter)) {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be of type Filter but is of type: "
                                    + filter.getValue().getClass());
                        }
                    } else if (filter.isTakesListOfValues()) {
                        // make sure filters that take collections or arrays
                        // actually have collections or arrays for their values
                        if (!(filter.getValue() instanceof Collection) && !(filter.getValue() instanceof Object[])) {
                            throw new IllegalArgumentException("The search has a filter (" + filter
                                    + ") for which the value should be a collection or array but is of type: "
                                    + filter.getValue().getClass());
                        }
                    }
                }

                return filter;
            }

            @SuppressWarnings("unchecked")
            @Override
            public QueryFilter visitAfter(QueryFilter filter) {
                if (filter == null)
                    return null;

                if (!filter.isTakesNoProperty()) {
                    securityCheckProperty(filter.getProperty());
                }

                // Remove operators that take sub filters but have none
                // assigned. Replace conjunctions that only have a single
                // sub-filter with that sub-filter.
                if (filter.isTakesSingleSubFilter()) {
                    if (filter.getValue() == null) {
                        return null;
                    }
                } else if (filter.isTakesListOfSubFilters()) {
                    if (filter.getValue() == null) {
                        return null;
                    } else {
                        List<QueryFilter> list = (List<QueryFilter>) filter.getValue();
                        if (list.size() == 0) {
                            return null;
                        } else if (list.size() == 1) {
                            return list.get(0);
                        }
                    }
                }

                return filter;
            }
        }, true);
    }

    /**
     * Regex pattern for a valid property name/path.
     */
    protected static Pattern INJECTION_CHECK = Pattern.compile("^[\\w\\.]*$");

    /**
     * Used by <code>securityCheck()</code> to check a property string for
     * injection attack.
     */
    protected static void securityCheckProperty(String property) {
        if (property == null)
            return;
        if (!INJECTION_CHECK.matcher(property).matches())
            throw new IllegalArgumentException(
                    "A property used in a Search may only contain word characters (alphabetic, numberic and underscore \"_\") and dot \".\" separators. This constraint was violated: "
                            + property);
    }

    private static final ExampleOptions defaultExampleOptions = new ExampleOptions();

    public QueryFilter getFilterFromExample(Object example) {
        return getFilterFromExample(example, null);
    }

    public QueryFilter getFilterFromExample(Object example, ExampleOptions options) {
        if (example == null)
            return null;
        if (options == null)
            options = defaultExampleOptions;

        List<QueryFilter> filters = new ArrayList<QueryFilter>();
        LinkedList<String> path = new LinkedList<String>();
        Metadata metadata = metadataHelper.get(example.getClass());
        getFilterFromExampleRecursive(example, metadata, options, path, filters);

        if (filters.size() == 0) {
            return null;
        } else if (filters.size() == 1) {
            return filters.get(0);
        } else {
            return new QueryFilter("AND", filters, QueryFilter.OP_AND);
        }
    }

    private void getFilterFromExampleRecursive(Object example, Metadata metaData, ExampleOptions options,
            LinkedList<String> path, List<QueryFilter> filters) {
        if (metaData.isEntity() && !metaData.getIdType().isEmbeddable()) {
            Object id = metaData.getIdValue(example);
            if (id != null) {
                filters.add(QueryFilter.equal(listToPath(path, "id"), id));
                return;
            }
        }

        for (String property : metaData.getProperties()) {
            if (options.getExcludeProps() != null && options.getExcludeProps().size() != 0) {
                if (options.getExcludeProps().contains(listToPath(path, property)))
                    continue;
            }

            Metadata pMetaData = metaData.getPropertyType(property);
            if (pMetaData.isCollection()) {
                // ignore collections
            } else {
                Object value = metaData.getPropertyValue(example, property);
                if (value == null) {
                    if (!options.isExcludeNulls()) {
                        filters.add(QueryFilter.isNull(listToPath(path, property)));
                    }
                } else if (options.isExcludeZeros() && value instanceof Number && ((Number) value).longValue() == 0) {
                    // ignore zeros
                } else {
                    if (pMetaData.isEntity() || pMetaData.isEmbeddable()) {
                        path.add(property);
                        getFilterFromExampleRecursive(value, pMetaData, options, path, filters);
                        path.removeLast();
                    } else if (pMetaData.isString()
                            && (options.getLikeMode() != ExampleOptions.EXACT || options.isIgnoreCase())) {
                        String val = value.toString();
                        switch (options.getLikeMode()) {
                        case ExampleOptions.START:
                            val = val + "%";
                            break;
                        case ExampleOptions.END:
                            val = "%" + val;
                            break;
                        case ExampleOptions.ANYWHERE:
                            val = "%" + val + "%";
                            break;
                        }
                        filters.add(new QueryFilter(listToPath(path, property), val,
                                options.isIgnoreCase() ? QueryFilter.OP_ILIKE : QueryFilter.OP_LIKE));
                    } else {
                        filters.add(QueryFilter.equal(listToPath(path, property), value));
                    }
                }
            }
        }
    }

    private String listToPath(List<String> list, String lastProperty) {
        StringBuilder sb = new StringBuilder();
        for (String prop : list) {
            sb.append(prop).append(".");
        }
        sb.append(lastProperty);
        return sb.toString();
    }
}
