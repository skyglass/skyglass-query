package skyglass.data.filter;

import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IQueryBuilder;

public class CustomJoin<E, S, F> implements IJoinResolver<E, F> {

    private IJoinType joinType;

    private PrivateQueryContext queryContext;

    private String aliasPath;

    private String alias;

    private CompositeFilter<E, F> compositeFilter = new CompositeFilter<E, F>(this, this, true);

    private int subQueryCounter = 1;
    
    private F filter;

    public CustomJoin(F filter, PrivateQueryContext queryContext, String aliasPath, IJoinType joinType) {
        this.filter = filter;
    	this.joinType = joinType;
        this.queryContext = queryContext;
        this.aliasPath = aliasPath;
    }

    @Override
    public IJoinResolver<E, F> equals(String propertyName, Object value) {
        return compositeFilter.equals(propertyName, value);
    }

    @Override
    public IJoinResolver<E, F> notEquals(String propertyName, Object value) {
        return compositeFilter.notEquals(propertyName, value);
    }

    @Override
    public IJoinResolver<E, F> eqProperty(String propertyName, String otherPropertyName) {
        return null;
    }

    @Override
    public IJoinResolver<E, F> and() {
        return compositeFilter.and();
    }

    @Override
    public IJoinResolver<E, F> or() {
        return compositeFilter.or();
    }

    @SuppressWarnings("unchecked")
    @Override
    public IJoinResolver<E, F> done() {
        return (IJoinResolver<E, F>) resolve();
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
    private PrivateFilterItemTree createCompositeFilterItem(CompositeFilter<E, F> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return createSubQueryFilterItem(aliasPath, queryContext, (SubQueryFilter) compositeFilter);
        }
        for (CompositeFilter<E, F> child : compositeFilter.getCompositeChildren()) {
            queryContext.addRootChild(
            		createCompositeFilterItem(child));
        }
        for (AtomicFilter atomicFilter : compositeFilter.getAtomicChildren()) {
        	queryContext.addRootChild(createAtomicFilterItem(atomicFilter));
        }
        return queryContext.getRootFilterItem();
    }

    private <SUB> PrivateFilterItemTree createSubQueryFilterItem(String parentPath, 
    		PrivateQueryContext parentContext,
    		SubQueryFilter<E, SUB, F> subQueryFilter) {
    	PrivateQueryContext subQueryContext = createQueryContext(subQueryFilter.isAnd());
        String subQueryAlias = getNextSubQueryAlias();
        for (CompositeFilter<E, F> compositeFilter : subQueryFilter.getCompositeChildren()) {
            subQueryContext.addRootChild(
            		createCompositeSubQueryFilterItem(subQueryAlias, subQueryContext, 
            				compositeFilter));
        }
        for (AtomicFilter atomicFilter : subQueryFilter.getAtomicChildren()) {
        	subQueryContext.addRootChild(createAtomicSubQueryFilterItem(subQueryContext, atomicFilter));
        }
        SubQueryFilter<E, SUB, F> propertiesSubQueryFilter = (SubQueryFilter<E, SUB, F>) subQueryFilter;
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
    		CompositeFilter<E, F> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return createSubQueryFilterItem(path, parentContext, 
            		(SubQueryFilter<E, SUB, F>) compositeFilter);
        }
        PrivateQueryContext subQueryContext = createQueryContext(compositeFilter.isAnd());
        for (CompositeFilter<E, F> child : compositeFilter.getCompositeChildren()) {
        	subQueryContext.addRootChild(
            		createCompositeSubQueryFilterItem(path, subQueryContext, child));
        }
        for (AtomicFilter atomicChild : compositeFilter.getAtomicChildren()) {
        	subQueryContext.addRootChild(
        			createAtomicSubQueryFilterItem(subQueryContext, atomicChild));
        }
        PrivateFilterItemTree rootFilterItem = subQueryContext.getRootFilterItem();
        parentContext.addSubQueryContext(path, subQueryContext);
        return rootFilterItem;
    }

    @Override
    public F resolve() {
        return resolve(null);
    }

    @Override
    public F resolve(String resolveAlias) {
        createCompositeFilterItem(compositeFilter);
        return filter;
    }

    @Override
    public F invert() {
        return invert(null);
    }

    @Override
    public F invert(String resolveAlias) {
        if (resolveAlias != null) {
            aliasPath = resolveAlias;
        }
        if (alias == null) {
            alias = queryContext.resolveAliasPath(aliasPath, joinType);
        }
        queryContext.createFilterItem(alias + ".id", FilterType.IsNull, null);
        return filter;
    }

    private String getNextSubQueryAlias() {
        return "subQuery" + new Integer(subQueryCounter++).toString();
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> idExistsSubQuery(Class<SUB> clazz) {
        return compositeFilter.idExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> idNotExistsSubQuery(Class<SUB> clazz) {
        return compositeFilter.idNotExistsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotExistsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return compositeFilter.propertyNotExistsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> idInSubQuery(Class<SUB> clazz) {
        return compositeFilter.idInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> idNotInSubQuery(Class<SUB> clazz) {
        return compositeFilter.idNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotEqualsSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyInSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String alias) {
        return compositeFilter.propertyNotInSubQuery(clazz, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias) {
        return compositeFilter.propertyNotEqualsSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias) {
        return compositeFilter.propertyNotInSubQuery(clazz, parentAlias, alias);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesEqualsSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesNotEqualsSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotEqualsSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesInSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesNotInSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotInSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesExistSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesExistSubQuery(clazz);
    }

    @Override
    public <SUB> SubQueryFilter<E, SUB, F> propertiesNotExistSubQuery(Class<SUB> clazz) {
        return compositeFilter.propertiesNotExistSubQuery(clazz);
    }

}
