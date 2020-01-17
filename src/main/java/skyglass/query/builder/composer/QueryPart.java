package skyglass.query.builder.composer;

import org.apache.commons.lang3.StringUtils;

class QueryPart {

	private QueryPartString queryPart;
	
	private QueryParamBuilder paramBuilder;

	public QueryPart(QueryComposer root, String queryPart, boolean distinct, String delimiter, boolean wherePart) {
		if (distinct) {
			root._setDistinct(true);
		}
		this.paramBuilder = new QueryParamBuilder();
		queryPart = QueryParamProcessor.parseParams(root, this.paramBuilder, queryPart);
		this.paramBuilder.setNonEmptyParams(root, wherePart);
		this.queryPart = addDelimiter(queryPart, delimiter, wherePart);
	}

	public QueryPartString getQueryPart() {
		return queryPart;
	}
	
	public boolean hasNoEmptyParamValues() {
		return !paramBuilder.hasEmptyParamValue();
	}
	
	private QueryPartString addDelimiter(String queryPart, String delimiter, boolean wherePart) {
		String firstDelimiter = null;
		String nextDelimiter = null;
		if (wherePart) {
			firstDelimiter = "WHERE ";
			nextDelimiter = (StringUtils.isBlank(delimiter) ? "AND" : delimiter) + " ";
		}
		return new QueryPartString(firstDelimiter, nextDelimiter, queryPart, wherePart);
	}
	
}
