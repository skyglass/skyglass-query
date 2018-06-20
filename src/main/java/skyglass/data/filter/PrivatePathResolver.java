package skyglass.data.filter;

import java.util.HashMap;
import java.util.Map;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPathResolver;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;

public class PrivatePathResolver implements IPathResolver {
	
    private Map<String, PrivateJoin> joins = new HashMap<>();
    
    private IJoinBuilder joinBuilder;
    
    private IJoinType joinType;
    
    public PrivatePathResolver(IJoinBuilder joinBuilder, IJoinType joinType) {
    	this.joinBuilder = joinBuilder;
    	this.joinType = joinType;
    }
    
    @Override
    public String resolvePropertyPath(String associationPath) {
        return resolvePropertyPath(associationPath, joinType);
    }
    
    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType) {
        return resolvePropertyPath(associationPath, joinType, null);
    }

    @Override
    public String resolvePropertyPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return createAliases(associationPath, joinType, onClause, false);
    }

    @Override
    public String resolveAliasPath(String associationPath) {
        return resolveAliasPath(associationPath, joinType);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType) {
        return resolveAliasPath(associationPath, joinType, null);
    }

    @Override
    public String resolveAliasPath(String associationPath, IJoinType joinType, IPredicate onClause) {
        return createAliases(associationPath, joinType, onClause, true);
    }
    
	@Override
	public IJoinType getJoinType() {
		return joinType;
	}

	@Override
	public void setJoinType(IJoinType joinType) {
		this.joinType = joinType;		
	}
    
    private String createAliases(String expression, IJoinType joinType, IPredicate onClause, boolean forceLast) {
        String original = expression;
        expression = IQueryBuilder.normalizeFieldName(expression, forceLast);
        String[] values1 = expression.split("\\.");
        String[] values = values1[0].split("_");
        if (values1.length == 1 && values.length == 1) {
            if (forceLast) {
                createAlias(values1[0], null, values1[0], joinType, onClause);
            }
            return original;
        }
        int i = 0;
        String currentPath = values[0];
        String currentAlias = values[0];
        createAlias(currentPath, null, values[0], joinType);
        for (String value : values) {
            if (i > 0) {
                if (i == values.length - 1 && values1.length == 1) {
                    createAlias(currentPath + "." + value, currentPath, currentAlias + "_" + value, joinType, onClause);
                } else {
                    createAlias(currentPath + "." + value, currentPath, currentAlias + "_" + value, joinType);
                }
                currentAlias = currentAlias + "_" + value;
                currentPath = currentPath + "." + value;
            }
            i++;
        }
        if (values1.length == 2) {
            if (forceLast) {
                createAlias(currentPath + "." + values1[1], currentPath, currentAlias + "_" + values1[1], joinType,
                        onClause);
                currentAlias = currentAlias + "_" + values1[1];
                return currentAlias;
            }
            String propertyName = IQueryBuilder.denormalizePropertyName(values1[1]);
            return currentAlias + "." + propertyName;
        }
        return currentAlias;
    }

    private void createAlias(String path, String parentPath, String alias, IJoinType joinType) {
        createAlias(path, parentPath, alias, joinType, null);
    }

    private void createAlias(String path, String parentPath, String alias, IJoinType joinType, IPredicate onClause) {
    	joinBuilder.addJoin(path, parentPath, alias, joinType, onClause);
        if (joins.containsKey(path)) {
            return;
        }
        PrivateJoin join = null;
        if (parentPath != null) {
            PrivateJoin parentJoin = joins.get(parentPath);
            join = new PrivateJoin(parentJoin, alias, joinType);
        } else {
            join = new PrivateJoin(null, alias, joinType);
        }
        joins.put(path, join);
    }

}
