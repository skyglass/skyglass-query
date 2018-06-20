package skyglass.data.filter;

import skyglass.query.model.criteria.IJoinType;

public class PrivateJoin {
	
	private PrivateJoin parent;
	
	private String alias;
	
	private IJoinType joinType;

	public PrivateJoin(PrivateJoin parent, String alias, IJoinType joinType) {
		this.parent = parent;
		this.alias = alias;
		this.joinType = joinType;
	}

	public PrivateJoin getParent() {
		return parent;
	}

	public String getAlias() {
		return alias;
	}

	public IJoinType getJoinType() {
		return joinType;
	}


}
