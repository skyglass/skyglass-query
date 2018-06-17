package skyglass.query.model.query;

import java.util.List;

/**
 * <code>IMutableSearch</code> is an extension of <code>ISearch</code> that
 * provides setters for all of the properties.
 * 
 */
public interface IMutableQuery extends ISearchQuery {
    public IMutableQuery setFirstResult(int firstResult);

    public IMutableQuery setMaxResults(int maxResults);

    public IMutableQuery setPage(int page);

    public IMutableQuery setSearchClass(Class<?> searchClass);

    public IMutableQuery setFilters(List<QueryFilter> filters);

    public IMutableQuery setDisjunction(boolean disjunction);

    public IMutableQuery setSorts(List<Sort> sorts);

    public IMutableQuery setFields(List<Field> fields);

    public IMutableQuery setDistinct(boolean distinct);

    public IMutableQuery setFetches(List<String> fetches);

    public IMutableQuery setResultMode(int resultMode);
}
