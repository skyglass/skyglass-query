package skyglass.query.builder.string;

class QueryPart {

	private String queryPart;

	private boolean distinct;

	public QueryPart(String queryPart, boolean distinct) {
		this.queryPart = queryPart;
		this.distinct = distinct;
	}

	public String getQueryPart() {
		return queryPart;
	}

	public boolean isDistinct() {
		return distinct;
	}

}
