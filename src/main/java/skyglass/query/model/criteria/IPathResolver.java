package skyglass.query.model.criteria;

public interface IPathResolver {
	
    public String resolvePropertyPath(String associationPath);
	
    public String resolvePropertyPath(String associationPath, IJoinType joinType);

    public String resolvePropertyPath(String associationPath, IJoinType joinType, IPredicate onClause);

    public String resolveAliasPath(String associationPath);

    public String resolveAliasPath(String associationPath, IJoinType joinType);

    public String resolveAliasPath(String associationPath, IJoinType joinType, IPredicate onClause);
    
    public IJoinType getJoinType();
    
    public void setJoinType(IJoinType joinType);

}
