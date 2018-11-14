package skyglass.query.builder.string;

import skyglass.query.builder.OrderType;

public class QueryRequestBuilder {

	private QueryRequestDTO result = new QueryRequestDTO();

	public QueryRequestBuilder start() {
		return new QueryRequestBuilder();
	}

	public QueryRequestBuilder setOffset(int offset) {
		result.setOffset(offset);
		return this;
	}

	public QueryRequestBuilder setLimit(int limit) {
		result.setLimit(limit);
		return this;
	}

	public QueryRequestBuilder setRowsPerPage(int rowsPerPage) {
		result.setRowsPerPage(rowsPerPage);
		return this;
	}

	public QueryRequestBuilder setPageNumber(int pageNumber) {
		result.setPageNumber(pageNumber);
		return this;
	}

	public QueryRequestBuilder setSearchTerm(String searchTerm) {
		result.setSearchTerm(searchTerm);
		return this;
	}

	public QueryRequestBuilder setOrderField(String orderField) {
		result.setOrderField(orderField);
		return this;
	}

	public QueryRequestBuilder setOrderType(OrderType orderType) {
		result.setOrderType(orderType);
		return this;
	}

	public QueryRequestBuilder setLang(String lang) {
		result.setLang(lang);
		return this;
	}

	public QueryRequestDTO build() {
		return result;
	}

}
