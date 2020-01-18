package skyglass.query.composer;

public class FieldItem {

	private String innerPath;

	private String alias;

	private boolean addRootAlias;
	
	private boolean useAsAlias;
	
	private FieldItemType fieldItemType;

	public FieldItem(FieldItemType fieldItemType, String alias, String innerAlias, String path, String innerPath) {
		this.fieldItemType = fieldItemType;
		this.alias = alias;
		this.innerPath = innerPath;
		this.addRootAlias = innerPath.equals(alias);
		this.useAsAlias = !alias.equals(innerAlias) || !path.equals(innerPath);
	}

	public String getAlias() {
		return alias;
	}

	public String getInnerPath(String rootAlias, boolean groupBy) {
		String result = getInnerPath(rootAlias);
		boolean test = (rootAlias + "." + alias).equals(result);
		return result + (!groupBy && useAsAlias && !test ? " AS " + alias : "");
	}

	String getInnerPath(String rootAlias) {
		return addRootAlias ? (rootAlias + "." + alias) : innerPath;
	}
	
	public boolean hasValue() {
		return fieldItemType == FieldItemType.Search;
	}

}
