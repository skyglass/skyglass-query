package skyglass.query;

import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;

import skyglass.query.builder.OrderType;
import skyglass.query.builder.QueryRequestDTO;
import skyglass.query.builder.config.Language;

public class QueryRequestUtil {

	public static boolean isPaged(QueryRequestDTO queryRequest) {
		if (queryRequest != null) {
			if (queryRequest.getPageNumber() > 0 && queryRequest.getRowsPerPage() > 0) {
				return true;
			}
			if (queryRequest.getOffset() >= 0 && queryRequest.getLimit() > 0) {
				return true;
			}
		}
		return false;
	}

	public static QueryRequestDTO createRequest(Integer pageNumber, Integer rowsPerPage, String searchTerm, String orderField, OrderType orderType, String lang) {
		QueryRequestDTO queryRequest = new QueryRequestDTO();
		queryRequest.setPageNumber(pageNumber == null ? -1 : pageNumber);
		queryRequest.setRowsPerPage(rowsPerPage == null ? -1 : rowsPerPage);
		queryRequest.setSearchTerm(searchTerm);
		queryRequest.setOrderField(orderField);
		queryRequest.setOrderType(orderType);
		queryRequest.setLang(lang);
		return queryRequest;
	}

	public static String getCurrentLanguageCode(QueryRequestDTO queryRequest) {
		return queryRequest == null || StringUtils.isBlank(queryRequest.getLang()) ? Language.DEFAULT.getLanguageCode() : queryRequest.getLang();
	}

	public static void setPaging(QueryRequestDTO queryRequest, Query query) {
		if (queryRequest != null) {
			if (queryRequest.getPageNumber() > 0 && queryRequest.getRowsPerPage() > 0) {
				query.setFirstResult((queryRequest.getPageNumber() - 1) * queryRequest.getRowsPerPage());
				query.setMaxResults(queryRequest.getRowsPerPage());
			}
			if (queryRequest.getOffset() >= 0 && queryRequest.getLimit() > 0) {
				query.setFirstResult(queryRequest.getOffset());
				query.setMaxResults(queryRequest.getLimit());
			}
		}
	}

}
