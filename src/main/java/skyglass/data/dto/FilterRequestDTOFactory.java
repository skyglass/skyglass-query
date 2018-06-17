package skyglass.data.dto;

import skyglass.data.filter.request.IFilterRequest;

public class FilterRequestDTOFactory {

    public static FilterRequestDTO fromRequest(IFilterRequest request) {
        FilterRequestDTO filterRequest = new FilterRequestDTO();
        filterRequest.setOrderField(request.getParameter("orderField"));
        filterRequest.setOrderType(request.getParameter("orderType"));
        filterRequest.setPageNumber(request.getParameter("pageNumber"));
        filterRequest.setRowsPerPage(request.getParameter("rowsPerPage"));
        return filterRequest;
    }

}
