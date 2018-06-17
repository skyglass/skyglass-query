package skyglass.query.model.query;

import java.util.List;

/**
 * SearchFacade provides a clean interface to the Search APIs.
 * 
 */
public interface QueryFacade {

    /**
     * Search for objects based on the search parameters in the specified
     * <code>ISearch</code> object.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings("unchecked")
    public List search(ISearchQuery search);

    /**
     * Search for objects based on the search parameters in the specified
     * <code>ISearch</code> object. Uses the specified searchClass, ignoring the
     * searchClass specified on the search itself.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings("unchecked")
    public List search(Class<?> searchClass, ISearchQuery search);

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResult limits.
     * 
     * @see ISearchQuery
     */
    public int count(ISearchQuery search);

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResult limits.
     * Uses the specified searchClass, ignoring the searchClass specified on the
     * search itself.
     * 
     * @see ISearchQuery
     */
    public int count(Class<?> searchClass, ISearchQuery search);

    /**
     * Returns a <code>SearchResult</code> object that includes the list of
     * results like <code>search()</code> and the total length like
     * <code>searchLength</code>.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings("unchecked")
    public QueryResult searchAndCount(ISearchQuery search);

    /**
     * Returns a <code>SearchResult</code> object that includes the list of
     * results like <code>search()</code> and the total length like
     * <code>searchLength</code>. Uses the specified searchClass, ignoring the
     * searchClass specified on the search itself.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings("unchecked")
    public QueryResult searchAndCount(Class<?> searchClass, ISearchQuery search);

    /**
     * Search for a single result using the given parameters.
     */
    public Object searchUnique(ISearchQuery search);

    /**
     * Search for a single result using the given parameters. Uses the specified
     * searchClass, ignoring the searchClass specified on the search itself.
     */
    public Object searchUnique(Class<?> searchClass, ISearchQuery search);

    /**
     * Generates a search filter from the given example using default options.
     */
    public QueryFilter getFilterFromExample(Object example);

    /**
     * Generates a search filter from the given example using the specified
     * options.
     */
    public QueryFilter getFilterFromExample(Object example, ExampleOptions options);

}