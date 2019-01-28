package skyglass.query.composer;

public class FieldItem {

	private String path;

	private String alias;

	private String innerAlias;

	private boolean addRootAlias;

	private boolean useInnerAlias;

	public FieldItem(String alias, String innerAlias, String path) {
		this.alias = alias;
		this.innerAlias = innerAlias;
		this.path = path;
		this.addRootAlias = path.equals(alias);
		this.useInnerAlias = alias.equals(innerAlias);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAlias() {
		return alias;
	}

	public String getInnerSelect(String rootAlias) {
		return getPath(rootAlias) + (useInnerAlias ? "" : " AS " + alias);
	}

	private String getPath(String rootAlias) {
		return addRootAlias ? (rootAlias + "." + path) : path;
	}

}
