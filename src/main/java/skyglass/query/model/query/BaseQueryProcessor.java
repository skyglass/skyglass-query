package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skyglass.data.filter.CompositeFilterItem;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.data.filter.criteria.impl.SearchFilter;
import skyglass.data.filter.criteria.impl.SearchUtil;
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

    protected PrivateQueryContext queryContext;
    
    protected BaseQueryProcessor(int qlType, MetadataHelper metadataHelper, 
    		PrivateQueryContext queryContext) {
        if (metadataHelper == null) {
            throw new IllegalArgumentException("A SearchProcessor cannot be initialized with a null MetadataHelper.");
        }
        this.qlType = qlType;
        this.metadataHelper = metadataHelper;
        this.queryContext = queryContext;
    }

    /**
     * The MetadataHelper used by this search processor. This can only be set in
     * the constructor.
     */
    public MetadataHelper getMetadataHelper() {
        return metadataHelper;
    }

    /**
     * Generate the QL string for a given search. Fill paramList with the values
     * to be used for the query. All parameters within the query string are
     * specified as named parameters ":pX", where X is the index of the
     * parameter value in paramList.
     */
    public String generateQL() {
        if (queryContext.getRootClazz() == null)
            throw new NullPointerException("The entity class for a query cannot be null");

        Collection<SelectField> selectFields = queryContext.getSelectFields();

        String select = generateSelectClause(selectFields, queryContext.isDistinct());
        String where = generateWhereClause(queryContext.checkAndCleanFilters(), queryContext.isDisjunction());
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
        List<SelectField> fields = searchQuery.getFields();
        if (fields != null) {
            for (SelectField field : fields) {
                switch (field.getOperator()) {
                case SelectField.OP_AVG:
                case SelectField.OP_COUNT:
                case SelectField.OP_COUNT_DISTINCT:
                case SelectField.OP_MAX:
                case SelectField.OP_MIN:
                case SelectField.OP_SUM:
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
    protected String generateSelectClause(Collection<SelectField> fields, boolean distinct) {
        StringBuilder sb = null;
        boolean useOperator = false, notUseOperator = false;
        boolean first = true;

        if (fields != null) {
            for (SelectField field : fields) {
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
                case SelectField.OP_AVG:
                    sb.append("avg(");
                    useOperator = true;
                    break;
                case SelectField.OP_COUNT:
                    sb.append("count(");
                    useOperator = true;
                    break;
                case SelectField.OP_COUNT_DISTINCT:
                    sb.append("count(distinct ");
                    useOperator = true;
                    break;
                case SelectField.OP_MAX:
                    sb.append("max(");
                    useOperator = true;
                    break;
                case SelectField.OP_MIN:
                    sb.append("min(");
                    useOperator = true;
                    break;
                case SelectField.OP_SUM:
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
    protected void applyFetches(SearchContext ctx, List<String> fetches, List<SelectField> fields) {
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
                for (SelectField field : fields) {
                    if (field.getOperator() == SelectField.OP_PROPERTY) {
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
    protected String generateWhereClause(PrivateCompositeFilterItem rootFilterItem) {
        String content = null;
        if (rootFilterItem.getChildren() == null || rootFilterItem.getChildren().size() == 0) {
            return "";
        } else {
            content = filterToQL(rootFilterItem);
        };

        return (content == null) ? "" : " where " + content;
    }

    /**
     * Recursively generate the QL fragment for a given search filter option.
     */
    @SuppressWarnings("rawtypes")
	protected String filterToQL(PrivateFilterItem filterItem) {
        String path = filterItem.getFieldResolver().getFieldName();
        FilterType filterType = filterItem.getFilterType();
        String filterOperator = filterType.getOperator();
        Supplier<Object> filterValueResolver = filterItem.getFilterValueResolver();

        // If the filter needs a value and no value is specified, ignore this
        // filter.
        // Only NULL, NOT_NULL, EMPTY and NOT_EMPTY do not need a value.
        if (filterItem.hasNullValue() && !filterItem.isTakesNoValue()) {
            return null;
        }

        // for IN and NOT IN, if value is empty list, return false, and true
        // respectively
        if (filterType == FilterType.In || filterType == FilterType.NotIn) {
            if (filterItem.hasEmptyCollectionValue()) {
                return filterType == FilterType.In ? "1 = 2" : "1 = 1";
            }
        }

        Metadata metadata;

        switch (filterType) {
        case IsNull:
        case IsNotNull:
            return queryContext.getPathRef(path) + filterOperator;
        case In:
        case NotIn:
            return queryContext.getPathRef(path) + filterOperator + "(" + queryContext.registerParam(
            		path, filterValueResolver) + ")";
        case Equals:
        case NotEquals:
        case Greater:
        case Less:
        case GreaterOrEquals:
        case LessOrEquals:
            return queryContext.getPathRef(path) + filterOperator + queryContext.registerParam(
            		path, filterValueResolver);
        case Like:
            return "LOWER(" + queryContext.getPathRef(path) + ") LIKE LOWER(" 
            	+ queryContext.registerParam(path, filterValueResolver) + ")";
        case And:
        case Or:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }
            
            PrivateCompositeFilterItem compositeFilterItem = (PrivateCompositeFilterItem) filterItem;

            StringBuilder sb = new StringBuilder("(");
            boolean first = true;
            for (PrivateFilterItem child : compositeFilterItem.getChildren()) {
	            String filterStr = filterToQL(child);
	            if (filterStr != null) {
	                if (first) {
	                    first = false;
	                } else {
	                    sb.append(filterOperator);
	                }
	                sb.append(filterStr);
	            }
            }
            if (first)
                return null;

            sb.append(")");
            return sb.toString();
        case Not:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }
            
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            String filterStr = filterToQL(compositeFilterItem.getSingleChild());
            if (filterStr == null)
                return null;

            return "NOT " + filterStr;
        case Empty:
            metadata = metadataHelper.get(queryContext.getRootClazz(), path);
            if (metadata.isCollection()) {
                return "NOT EXISTS ELEMENTS(" + queryContext.getPathRef(path) + ")";
            } else if (metadata.isString()) {
                String pathRef = queryContext.getPathRef(path);
                return "(" + pathRef + " IS NULL OR " + pathRef + " = '')";
            } else {
                return queryContext.getPathRef(path) + " IS NULL";
            }
        case NotEmpty:
            metadata = metadataHelper.get(queryContext.getRootClazz(), path);
            if (metadata.isCollection()) {
                return "EXISTS ELEMENTS(" + queryContext.getPathRef(path) + ")";
            } else if (metadata.isString()) {
                String pathRef = queryContext.getPathRef(path);
                return "(" + pathRef + " IS NOT NULL AND " + pathRef + " != '')";
            } else {
                return queryContext.getPathRef(path) + " IS NOT NULL";
            }
        case Some:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }            
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            return generateSimpleAllOrSome(compositeFilterItem, "some");
        case All:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }            
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            return generateSimpleAllOrSome(compositeFilterItem, "all");   
        case None:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }            
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            return "NOT ( " + generateSimpleAllOrSome(compositeFilterItem, "some") + " )";  
        //TODO: to be implemented
        /*case Exists:
            if (!(filterItem instanceof PrivateCompositeFilterItem)) {
                return null;
            }            
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            return "EXISTS " + generateSubquery(ctx, property, negate((QueryFilter) value));*/

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
    protected String generateSimpleAllOrSome(PrivateCompositeFilterItem filterItem,
            String operation) {
        PrivateFilterItem child = filterItem.getSingleChild();
        String path = child.getFieldResolver().getFieldName();
        FilterType filterType = child.getFilterType();
        String filterOperator = filterType.getOperator();
        Supplier<Object> filterValueResolver = child.getFilterValueResolver();
        return queryContext.registerParam(path, filterValueResolver) 
        		+ filterOperator + operation + " ELEMENTS(" + queryContext.getPathRef(path) + ")";
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
