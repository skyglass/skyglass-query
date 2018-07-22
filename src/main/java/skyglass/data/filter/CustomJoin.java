package skyglass.data.filter;

import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public class CustomJoin<E, S> implements IJoinResolver<E> {

    private IJoinType joinType;

    private PrivateQueryContext queryContext;

    private String aliasPath;

    private String alias;

    private CompositeFilter<E> compositeFilter = new CompositeFilter<E>(queryContext, this, true);

    private int subQueryCounter = 1;
    
    private IJoinResolver<E> parent;

    public CustomJoin(IJoinResolver<E> parent, PrivateQueryContext queryContext, String aliasPath, IJoinType joinType) {
        this.parent = parent;
    	this.joinType = joinType;
        this.queryContext = queryContext;
        this.aliasPath = aliasPath;
    }

    @Override
    public IJoinResolver<E> equals(String propertyName, Object value) {
        return compositeFilter.equals(propertyName, value);
    }

    @Override
    public IJoinResolver<E> notEquals(String propertyName, Object value) {
        return compositeFilter.notEquals(propertyName, value);
    }

    @Override
    public IJoinResolver<E> eqProperty(String propertyName, String otherPropertyName) {
        return null;
    }

    @Override
    public IJoinResolver<E> and() {
        return compositeFilter.and();
    }

    @Override
    public IJoinResolver<E> or() {
        return compositeFilter.or();
    }

    @Override
    public IJoinResolver<E> done() {
        return (IJoinResolver<E>) resolve();
    }

    private PrivateQueryContext createQueryContext(boolean isAnd) {
        return new PrivateQueryContext(queryContext, isAnd);
    }
    
    private PrivateFilterItem createAtomicFilterItem(AtomicFilter atomicFilter) {
        String propertyName = IQueryBuilder.resolvePropertyName(aliasPath + "." + atomicFilter.propertyName);
        return queryContext.createFilterItem(propertyName, atomicFilter.filterType, atomicFilter.value);
    }

    private PrivateFilterItem createAtomicSubQueryFilterItem(PrivateQueryContext subQueryContext,
    		AtomicFilter atomicFilter) {
        String propertyName = subQueryContext.resolvePropertyPath(atomicFilter.propertyName);
        return subQueryContext.createFilterItem(propertyName, atomicFilter.filterType, atomicFilter.value);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private PrivateCompositeFilterItem createCompositeFilterItem(CompositeFilter<E> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return createSubQueryFilterItem(aliasPath, queryContext, (SubQueryFilter) compositeFilter);
        }
        for (CompositeFilter<E> child : compositeFilter.getCompositeChildren()) {
            queryContext.addRootChild(
            		createCompositeFilterItem(child));
        }
        for (AtomicFilter atomicFilter : compositeFilter.getAtomicChildren()) {
        	queryContext.addRootChild(createAtomicFilterItem(atomicFilter));
        }
        return queryContext.getRootFilterItem();
    }

    private <SUB> PrivateCompositeFilterItem createSubQueryFilterItem(String parentPath, 
    		PrivateQueryContext parentContext,
    		SubQueryFilter<E, SUB> subQueryFilter) {
    	PrivateQueryContext subQueryContext = createQueryContext(subQueryFilter.isAnd());
        String subQueryAlias = getNextSubQueryAlias();
        for (CompositeFilter<E> compositeFilter : subQueryFilter.getCompositeChildren()) {
            subQueryContext.addRootChild(
            		createCompositeSubQueryFilterItem(subQueryAlias, subQueryContext, 
            				compositeFilter));
        }
        for (AtomicFilter atomicFilter : subQueryFilter.getAtomicChildren()) {
        	subQueryContext.addRootChild(createAtomicSubQueryFilterItem(subQueryContext, atomicFilter));
        }
        SubQueryFilter<E, SUB> propertiesSubQueryFilter = (SubQueryFilter<E, SUB>) subQueryFilter;
        String projectionName = null;
        String[] parentNames = new String[propertiesSubQueryFilter.getProperties().size()];
        int i = 0;
        for (SubQueryProperty property : propertiesSubQueryFilter.getProperties()) {
            String path = subQueryContext.resolvePropertyPath(property.getName(), property.getJoinType());
            String parentName = IQueryBuilder.resolvePropertyName(aliasPath + "." + property.getParentName());
            parentNames[i] = parentName;
            if (property.isProjection()) {
                if (property.getType() == PropertyType.Group) {
                    subQueryContext.addGroupBy(path);
                } else if (property.getType() == PropertyType.Max) {
                    projectionName = property.getName();
                    subQueryContext.addSelectField(property.getName(), SelectType.Max);
                } else {
                    projectionName = property.getName();
                    subQueryContext.addSelectField(property.getName());
                }
                if (property.isDistinct()) {
                    subQueryContext.setDistinct(true);
                }
            }
            if (property.isCorrelated()) {
                String correlatedName = null;
                if (IQueryBuilder.hasAlias(path)) {
                    correlatedName = path;
                } else {
                    correlatedName = subQueryAlias + "." + path;
                }
                subQueryContext.createFilterItem(property.getName(), FilterType.EqualsProp, correlatedName);
            }
            i++;
        }
        addSubQueriesPropertyFilterItem(parentPath, 
        		subQueryAlias + "." + projectionName, 
        		parentContext, subQueryContext, subQueryFilter.getSubQueryType());
        return subQueryContext.getRootFilterItem();
    }

    private <E1, SUB> void addSubQueriesPropertyFilterItem(String path,
    		String subQueryPath, PrivateQueryContext parentContext, 
    		PrivateQueryContext subQueryContext, SubQueryType subQueryType) {
        if (subQueryType == SubQueryType.PropEx) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.Exists, subQueryContext);
        } else if (subQueryType == SubQueryType.PropNotEx) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.NotExists, subQueryContext);
        } else if (subQueryType == SubQueryType.PropEq) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.EqualsProp, subQueryContext);
        } else if (subQueryType == SubQueryType.PropNotEq) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.NotEqualsProp, subQueryContext);
        } else if (subQueryType == SubQueryType.PropIn) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.In, subQueryContext);
        } else if (subQueryType == SubQueryType.PropNotIn) {
            parentContext.addSubQueryExpression(path, subQueryPath,FilterType.NotIn, subQueryContext);
        }
    }

    @SuppressWarnings("unchecked")
    private <SUB> PrivateFilterItem createCompositeSubQueryFilterItem(
    		String path, PrivateQueryContext parentContext,
    		CompositeFilter<E> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return createSubQueryFilterItem(path, parentContext, 
            		(SubQueryFilter<E, SUB>) compositeFilter);
        }
        PrivateQueryContext subQueryContext = createQueryContext(compositeFilter.isAnd());
        for (CompositeFilter<E> child : compositeFilter.getCompositeChildren()) {
        	subQueryContext.addRootChild(
            		createCompositeSubQueryFilterItem(path, subQueryContext, child));
        }
        for (AtomicFilter atomicChild : compositeFilter.getAtomicChildren()) {
        	subQueryContext.addRootChild(
        			createAtomicSubQueryFilterItem(subQueryContext, atomicChild));
        }
        PrivateCompositeFilterItem rootFilterItem = subQueryContext.getRootFilterItem();
        parentContext.addSubQueryContext(path, subQueryContext);
        return rootFilterItem;
    }

    @Override
    public IJoinResolver<E> resolve() {
        return resolve(null);
    }

    @Override
    public IJoinResolver<E> resolve(String resolveAlias) {
        createCompositeFilterItem(compositeFilter);
        return parent;
    }

    @Override
    public IJoinResolver<E> invert() {
        return invert(null);
    }

    @Override
    public IJoinResolver<E> invert(String resolveAlias) {
        if (resolveAlias != null) {
            aliasPath = resolveAlias;
        }
        if (alias == null) {
            alias = queryContext.resolveAliasPath(aliasPath, joinType);
        }
        queryContext.createFilterItem(alias + ".id", FilterType.IsNull, null);
        return parent;
    }

    private String getNextSubQueryAlias() {
        return "subQuery" + new Integer(subQueryCounter++).toString();
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> idExistsSubQuery(Class<SUB> clazz) {
        return compositeFilter.idExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> idNotExistsSubQuery(Class<SUB> clazz) {
        return compositeFilter.idNotExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyExistsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return compositeFilter.propertyNotExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> idInSubQuery(Class<SUB> clazz) {
        return compositeFilter.idInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> idNotInSubQuery(Class<SUB> clazz) {
        return compositeFilter.idNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyInSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotInSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return compositeFilter.propertyNotEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyNotInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesEqualsSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesNotEqualsSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesInSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesNotInSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesExistSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesExistSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB> propertiesNotExistSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotExistSubQuery(clazz);
    }
    
	@Override
    public IJoinResolver<E> addLeftJoin(String alias) {
        return new CustomJoin<E, S>(this, queryContext, alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<E> addJoin(String alias) {
        return new CustomJoin<E, S>(this, queryContext, alias, IJoinType.INNER);
    }
    
	@Override
    public IJoinResolver<E> addSubQueryLeftJoin(String alias) {
        return new CustomJoin<E, S>(this, 
        		new PrivateQueryContext(queryContext, queryContext.isDisjunction()), alias, IJoinType.LEFT);
    }    
        
	@Override
    public IJoinResolver<E> addSubQueryJoin(String alias) {
        return new CustomJoin<E, S>(this,
        		new PrivateQueryContext(queryContext, queryContext.isDisjunction()), alias, IJoinType.INNER);
    }

}
