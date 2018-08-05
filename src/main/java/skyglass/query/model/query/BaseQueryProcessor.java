package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skyglass.data.filter.AliasNode;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.data.filter.SelectType;
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
        String where = generateWhereClause(queryContext.checkAndCleanFilters());
        String orderBy = generateOrderByClause(queryContext.getOrderFields());
        applyFetches(queryContext.getFetches(), selectFields);
        String from = generateFromClause(true);

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
        if (queryContext.getRootClazz() == null)
            throw new NullPointerException("The entity class for a search cannot be null");

        String where = generateWhereClause(queryContext.checkAndCleanFilters());
        String from = generateFromClause(false);

        boolean useOperator = false, notUseOperator = false;
        List<SelectField> fields = queryContext.getSelectFields();
        if (fields != null) {
            for (SelectField field : fields) {
                switch (field.getOperator()) {
                case Avg:
                case Count:
                case CountDistinct:
                case Max:
                case Min:
                case Sum:
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
        if (!queryContext.isDistinct()) {
            sb.append("select count(*)");
        } else if (fields.size() == 0) {
            sb.append("select count(distinct ");
            sb.append(queryContext.getRootAlias()).append(".id)");
        } else if (fields.size() == 1) {
            sb.append("select count(distinct ");
            String prop = fields.get(0).getProperty();
            if (prop == null || "".equals(prop)) {
                sb.append(queryContext.getRootAlias());
            } else {
                sb.append(queryContext.getPathRef(prop));
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
                    prop = queryContext.getRootAlias();
                } else {
                    prop = queryContext.getPathRef(field.getProperty());
                }

                switch (field.getOperator()) {
                case Avg:
                    sb.append("avg(");
                    useOperator = true;
                    break;
                case Count:
                    sb.append("count(");
                    useOperator = true;
                    break;
                case CountDistinct:
                    sb.append("count(distinct ");
                    useOperator = true;
                    break;
                case Max:
                    sb.append("max(");
                    useOperator = true;
                    break;
                case Min:
                    sb.append("min(");
                    useOperator = true;
                    break;
                case Sum:
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
                return "select distinct " + queryContext.getRootAlias();
            else
                return "select " + queryContext.getRootAlias();
        }
        if (useOperator && notUseOperator) {
            throw new Error("A search can not have a mix of fields with operators and fields without operators.");
        }
        return sb.toString();
    }

    /**
     * Apply the fetch list to the alias tree in the search context.
     */
    protected void applyFetches(Collection<String> fetches, Collection<SelectField> selectFields) {
    	boolean applyFetch = false;
        if (fetches != null) {
            // apply fetches
            boolean hasFetches = false, hasFields = false;
            List<String> fetchProps = new ArrayList<String>();
            for (String fetch : fetches) {
                fetchProps.add(queryContext.getPathRef(fetch));
                hasFetches = true;
            }
            if (hasFetches && selectFields != null) {
                // don't fetch nodes whose ancestors aren't found in the select
                // clause
                List<String> selectProps = new ArrayList<String>();
                for (SelectField field : selectFields) {
                    if (field.getOperator() == SelectType.Property) {
                        selectProps.add(queryContext.getPathRef(field.getProperty()));
                    }
                    hasFields = true;
                }
                if (hasFields) {
                    for (String selectProp: selectProps) {
                            // make sure it has an ancestor in the select clause
                            boolean hasAncestor = false;
                            for (String fetchProp : fetchProps) {
                                if (fetchProp.startsWith(selectProp)) {
                                    hasAncestor = true;
                                    break;
                                }
                            }
                            if (!hasAncestor)
                                applyFetch = false;
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
    protected String generateFromClause(boolean doEagerFetching) {
        StringBuilder sb = new StringBuilder(" from ");
        sb.append(queryContext.getRootClazz().getName());
        sb.append(" ");
        sb.append(queryContext.getRootAlias());
        sb.append(generateJoins(doEagerFetching));
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
    protected String generateJoins(boolean doEagerFetching) {
        StringBuilder sb = new StringBuilder();

        // traverse alias graph breadth-first
        Queue<AliasNode> queue = new LinkedList<AliasNode>();
        queue.offer(queryContext.getRootNode());
        while (!queue.isEmpty()) {
            AliasNode node = queue.poll();
            if (node.getParent() != null) {
                sb.append(" left join ");
                if (doEagerFetching && node.isFetch())
                    sb.append("fetch ");
                sb.append(node.getParent().getAlias());
                sb.append(".");
                sb.append(node.getProperty());
                sb.append(" as ");
                sb.append(node.getAlias());
            }
            for (AliasNode child : node.getChildren()) {
                queue.offer(child);
            }
        }

        return sb.toString();
    }

    /**
     * Internal method for generating order by clause. Uses sort options from
     * search.
     */
    protected String generateOrderByClause(List<OrderField> orderFields) {
        if (orderFields == null)
            return "";

        StringBuilder sb = null;
        boolean first = true;
        for (OrderField orderField: orderFields) {
            if (first) {
                sb = new StringBuilder(" order by ");
                first = false;
            } else {
                sb.append(", ");
            }
                sb.append("lower(");
                sb.append(queryContext.getPathRef(orderField.getOrderField().getFieldName()));
                sb.append(")");
            sb.append(orderField.isDescending() ? " desc" : " asc");
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
            throw new IllegalArgumentException("Filter comparison ( " + filterOperator + " ) is invalid.");
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
    /*protected String generateSubquery(String property, PrivateFilterItem filterItem) {
        Class<?> rootClass = metadataHelper.get(queryContext.getRootClazz(), property).getJavaClass();
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
    }*/

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

    private String listToPath(List<String> list, String lastProperty) {
        StringBuilder sb = new StringBuilder();
        for (String prop : list) {
            sb.append(prop).append(".");
        }
        sb.append(lastProperty);
        return sb.toString();
    }
}
