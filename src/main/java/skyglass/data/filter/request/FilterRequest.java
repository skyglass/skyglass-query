package skyglass.data.filter.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import skyglass.data.common.util.StringUtil;
import skyglass.data.filter.IBaseDataFilter;
import skyglass.data.filter.OrderType;
import skyglass.query.model.criteria.IQueryBuilder;

public class FilterRequest implements IFilterRequest {

    static final private String ROWS_PER_PAGE_QUERY_PARAM = "rowsPerPage";
    static final private String PAGE_NUMBER_QUERY_PARAM = "pageNumber";
    static final private String ORDER_FIELD_QUERY_PARAM = "orderField";
    static final private String SORT_TYPE_QUERY_PARAM = "orderType";
    static final private String SEARCH_FIELDS = "searchFields";
    static final private String SEARCH_QUERY = "searchQuery";

    private Map<String, List<String>> map = new HashMap<>();

    private Map<String, String> singleValueMap = new HashMap<>();

    private String searchQuery;

    private String[] searchFields;

    public static FilterRequest init() {
        return new FilterRequest();
    }

    public String getRegexpSearchValue() {
        if (searchQuery != null) {
            searchQuery = IQueryBuilder.convertToRegexp(searchQuery);
        }
        return searchQuery;
    }

    public static <T> void initDataFilter(IBaseDataFilter<T, ?> dataFilter, IFilterRequest request) {
        if (request != null) {
            String rowsPerPageString = request.getParameter(ROWS_PER_PAGE_QUERY_PARAM);
            if (rowsPerPageString != null) {
                String pageNumberString = request.getParameter(PAGE_NUMBER_QUERY_PARAM);
                if (pageNumberString != null) {
                    dataFilter.setPaging(Integer.valueOf(rowsPerPageString), Integer.valueOf(pageNumberString));
                }
            }

            String orderField = request.getParameter(ORDER_FIELD_QUERY_PARAM);
            if (orderField != null) {
                OrderType orderType = OrderType.ASC;
                String sortTypeString = request.getParameter(SORT_TYPE_QUERY_PARAM);
                if (sortTypeString != null && sortTypeString.equalsIgnoreCase("desc")) {
                    orderType = OrderType.DESC;
                }
                dataFilter.setOrder(orderField, orderType);
            }
        }
    }

    private FilterRequest() {
        parseSearchQuery();
        parseSearchFields();
    }

    private void parseSearchQuery() {
        this.searchQuery = FilterRequestUtils.getStringParamValue(this, SEARCH_QUERY);
        if (StringUtil.isEmpty(this.searchQuery)) {
            this.searchQuery = null;
        }
    }

    private void parseSearchFields() {
        String param = FilterRequestUtils.getStringParamValue(this, SEARCH_FIELDS);
        if (param == null) {
            this.searchFields = new String[0];
        } else {
            this.searchFields = param.split(",");
        }
    }

    private boolean isSearchField(String field) {
        if (this.searchFields.length == 0) {
            return true;
        }
        for (int i = 0; i < this.searchFields.length; i++) {
            if (this.searchFields[i].equals(field)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getParameter(String paramName) {
        String result = singleValueMap.get(paramName);
        if (result != null) {
            return result;
        }
        List<String> listResult = map.get(paramName);
        if (listResult == null || listResult.isEmpty()) {
            return null;
        }
        return listResult.get(0);
    }

    @Override
    public String[] getParameterValues(String paramName) {
        List<String> result = map.get(paramName);
        if (result != null && !result.isEmpty()) {
            return result.toArray(new String[0]);
        }
        String singleResult = singleValueMap.get(paramName);
        if (singleResult != null) {
            return new String[] { singleResult };
        }
        return new String[0];
    }

    @Override
    public String getSearchQuery() {
        return searchQuery;
    }

    @Override
    public String[] filterSearchFields(String[] fields) {
        if (this.searchFields.length == 0) {
            return fields;
        }
        List<String> result = new ArrayList<String>();
        for (String field : fields) {
            if (isSearchField(field)) {
                result.add(field);
            }
        }
        return result.toArray(new String[0]);
    }

    public FilterRequest setParameter(String paramName, String paramValue) {
        singleValueMap.put(paramName, paramValue);
        List<String> listResult = map.get(paramName);
        if (listResult == null || listResult.isEmpty()) {
            listResult = new ArrayList<>();
            map.put(paramName, listResult);
        }
        listResult.add(paramValue);
        return this;
    }

    public FilterRequest setParameterValues(String paramName, String[] paramValues) {
        for (String paramValue : paramValues) {
            setParameter(paramName, paramValue);
        }
        return this;
    }

    public FilterRequest setPaging(String pageNumber, String rowsPerPage) {
        setParameter(PAGE_NUMBER_QUERY_PARAM, pageNumber);
        setParameter(ROWS_PER_PAGE_QUERY_PARAM, rowsPerPage);
        return this;
    }

    public FilterRequest setOrder(String orderField, String orderType) {
        setParameter(ORDER_FIELD_QUERY_PARAM, orderField);
        setParameter(SORT_TYPE_QUERY_PARAM, orderType);
        return this;
    }

    public FilterRequest setFilterValue(String filterField, String filterValue) {
        setParameter(filterField, filterValue);
        return this;
    }
    


}
