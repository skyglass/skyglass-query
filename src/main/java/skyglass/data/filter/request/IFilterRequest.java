package skyglass.data.filter.request;

public interface IFilterRequest {

    public String getParameter(String paramName);

    public String[] getParameterValues(String paramName);

    public String getSearchQuery();

    public String[] filterSearchFields(String[] fields);

}
