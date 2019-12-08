package skyglass.query.composer;

public class FieldItem {

	private String innerPath;

	private String alias;

	private boolean addRootAlias;
	
	private boolean useAsAlias;

	public FieldItem(String alias, String innerAlias, String path, String innerPath) {
		this.alias = alias;
		this.innerPath = innerPath;
		this.addRootAlias = innerPath.equals(alias);
		this.useAsAlias = !alias.equals(innerAlias) || !path.equals(innerPath);
	}

	public String getAlias() {
		return alias;
	}

	public String getInnerPath(String rootAlias, boolean groupBy) {
		return getInnerPath(rootAlias) + (!groupBy && useAsAlias ? " AS " + alias : "");
	}

	private String getInnerPath(String rootAlias) {
		return addRootAlias ? (rootAlias + "." + alias) : innerPath;
	}

}
