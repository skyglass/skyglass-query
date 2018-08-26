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
import skyglass.data.filter.FieldResolver;
import skyglass.data.filter.FilterType;
import skyglass.data.filter.OrderField;
import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.PrivateFilterItem;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.data.filter.SelectType;
import skyglass.query.metadata.Metadata;
import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.IQueryProcessor;

public class JpaQueryProcessor implements IQueryProcessor {

    private static Logger logger = LoggerFactory.getLogger(JpaQueryProcessor.class);

    protected static int QLTYPE_JPQL = 0;
    protected static int QLTYPE_HQL = 1;
    protected static int QLTYPE_EQL = 1;

    protected int qlType;

    protected MetadataHelper metadataHelper;

    protected PrivateQueryContext queryContext;
    
    private String orderClause = "";
    
    private String whereClause = "";
    
    public static JpaQueryProcessor getInstance(MetadataHelper metadataHelper, PrivateQueryContext queryContext) {
        return new JpaQueryProcessor(metadataHelper, queryContext);
    }

    private JpaQueryProcessor(MetadataHelper metadataHelper, PrivateQueryContext queryContext) {
        this(QLTYPE_JPQL, metadataHelper, queryContext);
    }
    
    private JpaQueryProcessor(int qlType, MetadataHelper metadataHelper, 
    		PrivateQueryContext queryContext) {
        if (metadataHelper == null) {
            throw new IllegalArgumentException("A SearchProcessor cannot be initialized with a null MetadataHelper.");
        }
        this.qlType = qlType;
        this.metadataHelper = metadataHelper;
        this.queryContext = queryContext;
    }

    public MetadataHelper getMetadataHelper() {
        return metadataHelper;
    }

    @Override
    public String generateQueryString() {
        if (queryContext.getRootClazz() == null)
            throw new NullPointerException("The entity class for a query cannot be null");

        Collection<SelectField> selectFields = queryContext.getSelectFields();

        String select = generateSelectClause(selectFields, queryContext.isDistinct());
        String where = whereClause;
        String orderBy = orderClause;
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

    @Override
    public String generateCountQueryString() {
        if (queryContext.getRootClazz() == null)
            throw new NullPointerException("The entity class for a search cannot be null");

        String where = whereClause;
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

    protected String generateFromClause(boolean doEagerFetching) {
        StringBuilder sb = new StringBuilder(" from ");
        sb.append(queryContext.getRootClazz().getName());
        sb.append(" ");
        sb.append(queryContext.getRootAlias());
        sb.append(generateJoins(doEagerFetching));
        return sb.toString();
    }

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
    
    @Override
    public void applyFilter(PrivateCompositeFilterItem rootFilterItem) {
        String content = null;
        if (rootFilterItem.getChildren() == null || rootFilterItem.getChildren().size() == 0) {
            return;
        } else {
            content = filterToQL(rootFilterItem);
        };

        this.whereClause = (content == null) ? "" : " where " + content;
    }

    @Override
    public void applyOrder(List<OrderField> orderFields) {
        if (orderFields == null)
            return;

        StringBuilder sb = null;
        boolean first = true;
        for (OrderField orderField: orderFields) {
            if (first) {
                sb = new StringBuilder(" order by ");
                first = false;
            } else {
                sb.append(", ");
            }
            FieldResolver fieldResolver = orderField.getOrderField();
            if (fieldResolver.isMultiple()) {
                sb.append(applyMultipleOrder(orderField));
            } else {
                sb.append(applySingleOrder(orderField));
            }
        }
        if (!first) {
            this.orderClause = sb.toString();
        }
      }
    
    private String applyMultipleOrder(OrderField orderField) {
    	StringBuilder sb = new StringBuilder();
        sb.append(concat(0, orderField.getOrderField().getResolvers()));
        sb.append(orderField.isDescending() ? " desc" : " asc");
        return sb.toString();
    }

    private String applySingleOrder(OrderField orderField) {
    	StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String fieldResolver : orderField.getOrderField().getResolvers()) {
            if (first) {
                first = false;
            } else {
                sb.append(", ");
            }
            sb.append("lower(");
            sb.append(queryContext.getPathRef(fieldResolver));
            sb.append(")");
            sb.append(orderField.isDescending() ? " desc" : " asc");
        }
        return sb.toString();
    }

    private String concat(int i, Collection<String> fieldResolvers) {
        String expression = coalesce(getFieldResolver(i, fieldResolvers));
        if (i < fieldResolvers.size() - 1) {
            return concat(expression, concat(i + 1, fieldResolvers));
        } else {
            return expression;
        }
    }

    private String coalesce(String concat) {
    	StringBuilder sb = new StringBuilder();
        sb.append("coalesce(lower(");
        sb.append(concat);
        sb.append("))");
        return sb.toString();
    }

    private String concat(String concat1, String concat2) {
    	StringBuilder sb = new StringBuilder();
        sb.append("concat(");
        sb.append(concat1);
        sb.append(", ");
        sb.append(concat2);
        sb.append(")");
        return sb.toString();
    }
    
    private String getFieldResolver(int i, Collection<String> fieldResolvers) {
        int j = 0;
        for (String fieldResolver : fieldResolvers) {
            if (j == i) {
                return fieldResolver;
            }
            j++;
        }
        return null;
    }
    
    private String filterToQL(PrivateFilterItem filterItem) {
        if (filterItem instanceof PrivateCompositeFilterItem) {
            return filterCompositeToQL(filterItem);
        }
        String totalResult = null;
        for (String field : filterItem.getFieldResolver().getResolvers()) {
            String result = filterAtomicToQL(filterItem, field);
            if (totalResult == null) {
                totalResult = result;
            } else {
                totalResult = "(" + totalResult + ") OR (" + result + ")";
            }
        }
        return totalResult;
    }

	private String filterCompositeToQL(PrivateFilterItem filterItem) {		
        FilterType filterType = filterItem.getFilterType();
        String filterOperator = filterType.getOperator();
        PrivateCompositeFilterItem compositeFilterItem = (PrivateCompositeFilterItem) filterItem;

        switch (filterType) {
        case And:
        case Or:         
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
            String filterStr = filterToQL(compositeFilterItem.getSingleChild());
            if (filterStr == null)
                return null;

            return "NOT " + filterStr;
        case Some:
            return generateSimpleAllOrSome(compositeFilterItem, "some");
        case All:          
            compositeFilterItem = (PrivateCompositeFilterItem) filterItem;
            return generateSimpleAllOrSome(compositeFilterItem, "all");   
        case None:
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
	
	private String filterAtomicToQL(PrivateFilterItem filterItem, String path) {
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

        default:
            throw new IllegalArgumentException("Filter comparison ( " + filterOperator + " ) is invalid.");
        }
    }


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
