package skyglass.query.composer;

public class FieldItem {

	private String innerPath;

	private String alias;

	private boolean addRootAlias;

	private boolean useInnerAlias;

	public FieldItem(String alias, String innerAlias, String path, String innerPath) {
		this.alias = alias;
		this.innerPath = innerPath;
		this.addRootAlias = path.equals(alias);
		this.useInnerAlias = alias.equals(innerAlias);
	}

	public String getAlias() {
		return alias;
	}

	public String getInnerSelect(String rootAlias) {
		return getInnerPath(rootAlias) + (useInnerAlias ? "" : " AS " + alias);
	}

	private String getInnerPath(String rootAlias) {
		return addRootAlias ? (rootAlias + "." + alias) : innerPath;
	}

}
