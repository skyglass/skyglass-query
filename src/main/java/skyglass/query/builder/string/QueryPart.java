package skyglass.query.builder.string;

import org.apache.commons.lang3.StringUtils;

class QueryPart {

	private String queryPart;
	
	private QueryParamBuilder paramBuilder;

	public QueryPart(QueryComposer root, String queryPart, boolean distinct, String delimiter, boolean wherePart) {
		if (distinct) {
			root._setDistinct(true);
		}
		this.paramBuilder = new QueryParamBuilder();
		queryPart = QueryParamProcessor.parseParams(root, this.paramBuilder, queryPart);
		boolean addWhere = this.paramBuilder.setNonEmptyParams(root, wherePart);
		this.queryPart = addDelimiter(queryPart, delimiter, wherePart, addWhere);
	}

	public String getQueryPart() {
		return queryPart;
	}
	
	public boolean hasNoEmptyParamValues() {
		return !paramBuilder.hasEmptyParamValue();
	}
	
	private String addDelimiter(String queryPart, String delimiter, boolean wherePart, boolean addWhere) {
		if (wherePart && addWhere) {
			return "WHERE " + queryPart;
		}
		if (wherePart && !addWhere) {
			return (StringUtils.isBlank(delimiter) ? "AND" : delimiter) + " " + queryPart;
		}
		return queryPart;
	}

}
