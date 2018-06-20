package skyglass.query.model.criteria;

public interface IJoinBuilder {
	
	public void addJoin(String path, String parentPath, String alias, IJoinType joinType, IPredicate onClause);

}
