package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IValueResolver;

public class PrivateQueryContext {
	
    private PrivateFilterItemTree rootFilterItem;
    
    private IValueResolver valueResolver;
    
    private IJoinBuilder joinBuilder;
    
    private PrivatePathResolver pathResolver;
    
    private Class<?> rootClazz;
    
    private Map<String, FieldResolver> fieldResolverMap = new HashMap<String, FieldResolver>();
    
    private Map<String, PrivateQueryContext> subQueryContextMap = new HashMap<String, PrivateQueryContext>();
    
    private Collection<PrivateExpression> groupByList = new ArrayList<>();
    
    private Collection<PrivateSelect> selectList = new ArrayList<>();
    
    private boolean distinct = false;
    
    public PrivateQueryContext(JunctionType junctionType, IValueResolver valueResolver,
    		IJoinBuilder joinBuilder, Class<?> rootClazz, IJoinType joinType) {
        this.rootFilterItem = new PrivateFilterItemTree(junctionType);
        this.valueResolver = valueResolver;
        this.joinBuilder = joinBuilder;
        this.rootClazz = rootClazz;
        this.pathResolver = new PrivatePathResolver(joinBuilder, joinType);
    }
    
    public PrivateQueryContext(PrivateQueryContext parent, boolean isAnd) {
        this(isAnd ? JunctionType.AND : JunctionType.OR, parent.valueResolver, 
        		parent.joinBuilder, parent.rootClazz, parent.pathResolver.getJoinType());
    }
    
    public void addRootChild(PrivateFilterItem filterItem) {
    	rootFilterItem.addChild(filterItem);
    }
    
    public PrivateFilterItemTree getRootFilterItem() {
    	return rootFilterItem;
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, Object filterValue) {
        return createFilterItem(fieldName, FieldType.Path, filterValue);
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType, Object filterValue) {
        return createFilterItem(fieldName, FilterType.Equals, filterValue);
    }
    
    public PrivateFilterItem createFilterItem(String fieldName, FilterType filterType, Object filterValue) {
        return createFilterItem(fieldName, FieldType.Path, filterType, filterValue);
    }

    public PrivateFilterItem createFilterItem(String fieldName, FieldType fieldType,
    		FilterType filterType, Object filterValue) {
        return new DataFilterItem(valueResolver, rootClazz, addFieldResolver(fieldName, fieldType), filterValue,
                filterType);
    }
    
    public FieldResolver addFieldResolver(String fieldName, FieldType fieldType) {
        FieldResolver fieldResolver = fieldResolverMap.get(fieldName);
        if (fieldResolver == null) {
            fieldResolver = new FieldResolver(this, fieldName, fieldType);
            fieldResolverMap.put(fieldName, fieldResolver);
        }
        if (fieldResolver.getFieldType() == null && fieldType != null) {
            fieldResolver.setFieldType(fieldType);
        }
        return fieldResolver;
    }
    
    public FieldResolver addCustomFieldResolver(String fieldName, CustomFieldResolver customFieldResolver) {
        FieldResolver fieldResolver = addFieldResolver(fieldName, FieldType.Criteria);
        fieldResolver.setCustomFieldResolver(customFieldResolver);
        return fieldResolver;
    }

    public FieldResolver addFieldResolver(String fieldName, FieldType fieldType, String expression) {
        FieldResolver fieldResolver = addFieldResolver(fieldName, fieldType);
        fieldResolver.addResolvers(expression);
        return fieldResolver;
    }

    public FieldResolver addFieldResolvers(String fieldName, FieldType fieldType, String... expressions) {
        FieldResolver fieldResolver = addFieldResolver(fieldName, fieldType);
        fieldResolver.addResolvers(expressions);
        return fieldResolver;
    }
    
    public String resolveAliasPath(String path, IJoinType joinType) {
    	return pathResolver.resolveAliasPath(path, joinType);
    }
    
    public String resolvePropertyPath(String path) {
    	return pathResolver.resolvePropertyPath(path);
    }
    
    public String resolvePropertyPath(String path, IJoinType joinType) {
    	return pathResolver.resolvePropertyPath(path, joinType);
    }
    
    public void setJoinType(IJoinType joinType) {
    	pathResolver.setJoinType(joinType);
    }
    
    public void addSubQueryContext(String path, PrivateQueryContext subQueryContext) {
    	subQueryContextMap.put(path, subQueryContext);
    }
    
    public void addGroupBy(String path) {
    	groupByList.add(new PrivateExpression(path));
    }
    
    public void addSelect(String alias, String path) {
    	addSelect(alias, path, ExpressionType.Property);
    }
    
    public void addSelect(String alias, String path, ExpressionType expressionType) {
    	selectList.add(new PrivateSelect(path, expressionType, alias));
    }
    
    public void addSubQueryExpression(String path, String subQueryPath, FilterType filterType,
    		PrivateQueryContext subQueryContext) {
    	createFilterItem(path, filterType, subQueryPath);
    	subQueryContextMap.put(path, subQueryContext);
    }
    
    public void setDistinct(boolean distinct) {
    	this.distinct = distinct;
    }

}
