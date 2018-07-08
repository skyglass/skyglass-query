package skyglass.data.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import skyglass.query.model.criteria.IJoinBuilder;
import skyglass.query.model.criteria.IJoinType;
import skyglass.query.model.criteria.IPathResolver;
import skyglass.query.model.criteria.IPredicate;
import skyglass.query.model.criteria.IQueryBuilder;

public class PrivatePathResolver implements IPathResolver {
	
    private static final String ROOT_PATH = "";
    
    private String rootAlias = "_it";
	
    private Map<String, PrivateJoin> joins = new HashMap<>();
    
    private IJoinBuilder joinBuilder;
    
    private IJoinType joinType;
    
    private int nextAliasNum = 1;
    private int nextSubqueryNum = 1;
    
    private Map<String, AliasNode> aliases = new HashMap<String, AliasNode>();
    private List<Supplier<Object>> paramList = new ArrayList<Supplier<Object>>();
    
    public PrivatePathResolver(IJoinBuilder joinBuilder, IJoinType joinType) {
    	this.joinBuilder = joinBuilder;
    	this.joinType = joinType;
        setRootAlias();
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
    
    public void setRootAlias() {
        this.aliases.put(ROOT_PATH, new AliasNode(ROOT_PATH, rootAlias));
    }
    
    private String getRootAlias() {
        return this.aliases.get(ROOT_PATH).alias;
    }
    
    /**
     * Add value to paramList and return the named parameter string ":pX".
     */
    @SuppressWarnings("rawtypes")
	protected String registerParam(Supplier<Object> valueResolver) {
        paramList.add(valueResolver);
        return ":p" + Integer.toString(paramList.size());
    }

    /**
     * Given a full path to a property (ex. department.manager.salary), return
     * the reference to that property that uses the appropriate alias (ex.
     * a4_manager.salary).
     */
    protected String getPathRef(String path) {
        if (path == null || "".equals(path)) {
            return getRootAlias();
        }

        String[] parts = splitPath(path);

        return getAlias(parts[0], false).alias + "." + parts[1];
    }

    /**
     * Split a path into two parts. The first part will need to be aliased. The
     * second part will be a property of that alias. For example:
     * (department.manager.salary) would return [department.manager, salary].
     */
    private String[] splitPath(String path) {
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
    protected AliasNode getAlias(String path, boolean setFetch) {
        if (path == null || path.equals("")) {
            return aliases.get(ROOT_PATH);
        } else if (aliases.containsKey(path)) {
            AliasNode node = aliases.get(path);
            if (setFetch) {
                while (node.parent != null) {
                    node.fetch = true;
                    node = node.parent;
                }
            }

            return node;
        } else {
            String[] parts = splitPath(path);

            int pos = parts[1].lastIndexOf('.');

            String alias = "a" + (nextAliasNum++) + "_" + (pos == -1 ? parts[1] : parts[1].substring(pos + 1));

            AliasNode node = new AliasNode(parts[1], alias);

            // set up path recursively
            getAlias(parts[0], setFetch).addChild(node);

            node.fetch = setFetch;
            aliases.put(path, node);

            return node;
        }
    }
    
    private static final class AliasNode {
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

}
