package skyglass.query.composer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skyglass.query.composer.config.Language;

public class QueryRequestDTO implements Serializable {

	private static final long serialVersionUID = 7870432578485726477L;

	public static final String DEFAULT_LANGUAGE = Language.DEFAULT.getLanguageCode();

	private int offset = 0;

	private int limit = -1;

	private int rowsPerPage = -1;

	private int pageNumber = -1;

	private List<String> searchTerms = new ArrayList<>();

	private String searchTerm;

	private String orderField;

	private OrderType orderType;

	private String lang;
	
	private Map<String, Object> map = new HashMap<>();

	public int getRowsPerPage() {
		return rowsPerPage;
	}

	public void setRowsPerPage(int rowsPerPage) {
		this.rowsPerPage = rowsPerPage;
	}

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public String getOrderField() {
		return orderField;
	}

	public void setOrderField(String orderField) {
		this.orderField = orderField;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public String getSearchTerm() {
		return searchTerm;
	}

	public void setSearchTerm(String searchTerm) {
		this.searchTerm = searchTerm;
	}

	public String getLang() {
		return lang;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public List<String> getSearchTerms() {
		return searchTerms;
	}

	public void setSearchTerms(List<String> searchTerms) {
		this.searchTerms = searchTerms;
	}
	
	public <T> void set(String name, T value) {
		this.map.put(name, value);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(String name) {
		return (T)this.map.get(name);
	}

}
