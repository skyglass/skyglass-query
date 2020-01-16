package skyglass.query.builder;

public class SearchPath {
	
	private String alias;
	
	private String path;
	
	public SearchPath(String alias, String path) {
		this.alias = alias;
		this.path = path;
	}

	public String getAlias() {
		return alias;
	}

	public String getPath() {
		return path;
	}

}
