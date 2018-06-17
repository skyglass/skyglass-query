package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import skyglass.query.api.AbstractSubQueryBuilder;
import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.ISubQueryBuilder;

public class CustomJoin<E, S, F> implements CustomJpaFilterResolver<E, S>, IJoinResolver<E, F> {

    private IJoinType joinType;

    private IBaseDataFilter<E, F> filter;

    private String aliasPath;

    private String alias;

    private boolean invert;

    private String resolveAlias;

    private CompositeFilter<E, F> compositeFilter = new CompositeFilter<E, F>(this, this, true);

    private int subQueryCounter = 1;

    public CustomJoin(IJoinType joinType, IBaseDataFilter<E, F> filter, String aliasPath) {
        this.joinType = joinType;
        this.filter = filter;
        this.aliasPath = aliasPath;
    }

    @Override
    public void addCustomFilter(IQueryBuilder<E, S> criteriaBuilder) {
        IPredicate restriction = createPredicate(criteriaBuilder);
        if (resolveAlias != null) {
            aliasPath = resolveAlias;
        }
        if (restriction != null) {
            alias = filter.resolveAliasPath(aliasPath, joinType, restriction);
        }
        if (invert) {
            if (alias == null) {
                alias = filter.resolveAliasPath(aliasPath, joinType);
            }
            criteriaBuilder.isNull(criteriaBuilder.getExpression(alias + ".id"));
        }
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

    private IPredicate createPredicate(IQueryBuilder<E, S> criteriaBuilder) {
        IPredicate predicate = addCompositePredicate(criteriaBuilder, compositeFilter);
        return predicate;
    }

    private IPredicate addAtomicPredicate(AtomicFilter atomicFilter) {
        IPredicate predicate = filter.createAtomicFilter(
                IQueryBuilder.resolvePropertyName(aliasPath + "." + atomicFilter.propertyName),
                atomicFilter.filterType, () -> atomicFilter.value);
        return predicate;
    }

    private <SUB> IPredicate addAtomicSubQueryPredicate(ISubQueryBuilder<E, SUB> criteriaBuilder,
            SubQueryFilter<E, SUB, F> subQueryFilter, AtomicFilter atomicFilter) {
        String propertyName = criteriaBuilder.resolvePropertyPath(atomicFilter.propertyName,
                subQueryFilter.getJoinType());
        IPredicate predicate = criteriaBuilder.getPredicate(propertyName, atomicFilter.filterType,
                () -> atomicFilter.value);
        return predicate;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private IPredicate addCompositePredicate(IQueryBuilder<E, S> criteriaBuilder,
            CompositeFilter<E, F> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return addSubQueryPredicate((SubQueryFilter) compositeFilter, (AbstractSubQueryBuilder) criteriaBuilder);
        }
        Collection<IPredicate> predicates = new ArrayList<>();
        for (CompositeFilter<E, F> child : compositeFilter.getCompositeChildren()) {
            predicates.add(addCompositePredicate(criteriaBuilder, child));
        }
        for (AtomicFilter atomicFilter : compositeFilter.getAtomicChildren()) {
            predicates.add(addAtomicPredicate(atomicFilter));
        }
        IPredicate result = null;
        for (IPredicate predicate : predicates) {
            if (result == null) {
                result = predicate;
            } else if (compositeFilter.isAnd()) {
                result = criteriaBuilder.and(result, predicate);
            } else {
                result = criteriaBuilder.or(result, predicate);
            }
        }
        return result;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private <SUB> IPredicate addSubQueryPredicate(SubQueryFilter<E, SUB, F> subQueryFilter,
            ISubQueryBuilder<E, SUB> criteriaBuilder) {
        String subQueryAlias = getNextSubQueryAlias();
        Collection<IPredicate> predicates = new ArrayList<>();
        for (CompositeFilter<E, F> compositeFilter : subQueryFilter.getCompositeChildren()) {
            predicates.add(addCompositeSubQueryPredicate(criteriaBuilder, subQueryFilter, compositeFilter));
        }
        for (AtomicFilter atomicFilter : subQueryFilter.getAtomicChildren()) {
            predicates.add(addAtomicSubQueryPredicate(criteriaBuilder, subQueryFilter, atomicFilter));
        }
        IPredicate result = null;
        for (IPredicate predicate : predicates) {
            if (result == null) {
                result = predicate;
            } else if (subQueryFilter.isAnd()) {
                result = criteriaBuilder.and(result, predicate);
            } else {
                result = criteriaBuilder.or(result, predicate);
            }
        }
        SubQueryFilter<E, SUB, F> propertiesSubQueryFilter = (SubQueryFilter<E, SUB, F>) subQueryFilter;
        String projectionName = null;
        String[] parentNames = new String[propertiesSubQueryFilter.getProperties().size()];
        List<IPredicate> predicateList = new ArrayList<>();
        int i = 0;
        for (SubQueryProperty property : propertiesSubQueryFilter.getProperties()) {
            String name = criteriaBuilder.resolvePropertyPath(property.getName(), property.getJoinType());
            String parentName = IQueryBuilder.resolvePropertyName(aliasPath + "." + property.getParentName());
            parentNames[i] = parentName;
            if (property.isProjection()) {
                if (property.getType() == PropertyType.Group) {
                    criteriaBuilder.getSubQuery().groupBy(criteriaBuilder.getExpression(property.getName()));
                } else if (property.getType() == PropertyType.Max) {
                    projectionName = property.getName();
                    criteriaBuilder.getSubQuery().select(
                            (IExpression) criteriaBuilder.max(criteriaBuilder.getRoot().get(property.getName())));
                } else {
                    projectionName = property.getName();
                    criteriaBuilder.getSubQuery()
                            .select((IExpression) criteriaBuilder.getRoot().get(property.getName()));
                }
                if (property.isDistinct()) {
                    criteriaBuilder.getSubQuery().distinct(true);
                }
            }
            if (property.isCorrelated()) {
                String correlatedName = null;
                if (IQueryBuilder.hasAlias(name)) {
                    correlatedName = name;
                } else {
                    correlatedName = subQueryAlias + "." + name;
                }
                predicateList.add(criteriaBuilder.equalProperty(criteriaBuilder.getExpression(property.getName()),
                        criteriaBuilder.getExpression(correlatedName)));
            }
            i++;
        }
        for (IPredicate predicate : predicateList) {
            if (result == null) {
                result = predicate;
            } else {
                result = criteriaBuilder.and(result, predicate);
            }
        }
        return getSubQueriesPropertyPredicate(projectionName, criteriaBuilder, subQueryFilter.getSubQueryType());
    }

    private <E1, SUB> IPredicate getSubQueriesPropertyPredicate(String parentName,
            ISubQueryBuilder<E1, SUB> criteriaBuilder, SubQueryType subQueryType) {
        if (subQueryType == SubQueryType.PropEx) {
            return criteriaBuilder.exists(criteriaBuilder.getSubQuery());
        } else if (subQueryType == SubQueryType.PropNotEx) {
            return criteriaBuilder.not(criteriaBuilder.exists(criteriaBuilder.getSubQuery()));
        } else if (subQueryType == SubQueryType.PropEq) {
            return criteriaBuilder.equalProperty(criteriaBuilder.getExpression(parentName),
                    criteriaBuilder.getSubQuery());
        } else if (subQueryType == SubQueryType.PropNotEq) {
            return criteriaBuilder.notEqualProperty(criteriaBuilder.getExpression(parentName),
                    criteriaBuilder.getSubQuery());
        } else if (subQueryType == SubQueryType.PropIn) {
            return criteriaBuilder.getExpression(parentName).in(criteriaBuilder.getSubQuery());
        } else if (subQueryType == SubQueryType.PropNotIn) {
            return criteriaBuilder.not(criteriaBuilder.getExpression(parentName).in(criteriaBuilder.getSubQuery()));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <SUB> IPredicate addCompositeSubQueryPredicate(ISubQueryBuilder<E, SUB> criteriaBuilder,
            SubQueryFilter<E, SUB, F> subQueryFilter, CompositeFilter<E, F> compositeFilter) {
        if (compositeFilter instanceof SubQueryFilter) {
            return addSubQueryPredicate((SubQueryFilter<E, SUB, F>) compositeFilter, criteriaBuilder);
        }
        Collection<IPredicate> predicates = new ArrayList<>();
        for (CompositeFilter<E, F> child : compositeFilter.getCompositeChildren()) {
            predicates.add(addCompositeSubQueryPredicate(criteriaBuilder, subQueryFilter, child));
        }
        for (AtomicFilter atomicChild : compositeFilter.getAtomicChildren()) {
            predicates.add(addAtomicSubQueryPredicate(criteriaBuilder, subQueryFilter, atomicChild));
        }
        IPredicate result = null;
        for (IPredicate predicate : predicates) {
            if (result == null) {
                result = predicate;
            } else if (compositeFilter.isAnd()) {
                result = criteriaBuilder.and(result, predicate);
            } else {
                result = criteriaBuilder.or(result, predicate);
            }
        }
        return result;
    }

    @Override
    public F resolve() {
        return resolve(null);
    }

    @Override
    public F resolve(String resolveAlias) {
        this.resolveAlias = resolveAlias;
        return filter.addCustomFilterResolver((CustomFilterResolver) this);
    }

    @Override
    public F invert() {
        return invert(null);
    }

    @Override
    public F invert(String resolveAlias) {
        this.invert = true;
        this.resolveAlias = resolveAlias;
        return filter.addCustomFilterResolver((CustomFilterResolver) this);
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
