package skyglass.query.model.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A convenient fully-featured implementation of ISearch and IMutableSearch for
 * general use in Java code.
 */
public class SearchQuery implements IMutableQuery, Serializable {

    private static final long serialVersionUID = 1L;

    protected int firstResult = -1; // -1 stands for unspecified

    protected int maxResults = -1; // -1 stands for unspecified

    protected int page = -1; // -1 stands for unspecified

    protected Class<?> searchClass;

    protected List<QueryFilter> filters = new ArrayList<QueryFilter>();

    protected boolean disjunction;

    protected List<Sort> sorts = new ArrayList<Sort>();

    protected List<SelectField> fields = new ArrayList<SelectField>();

    protected boolean distinct;

    protected List<String> fetches = new ArrayList<String>();

    protected int resultMode = RESULT_AUTO;

    public SearchQuery() {

    }

    public SearchQuery(Class<?> searchClass) {
        this.searchClass = searchClass;
    }

    public SearchQuery setSearchClass(Class<?> searchClass) {
        this.searchClass = searchClass;
        return this;
    }

    public Class<?> getSearchClass() {
        return searchClass;
    }

    // Filters
    public SearchQuery addFilter(QueryFilter filter) {
        QueryUtil.addFilter(this, filter);
        return this;
    }

    public SearchQuery addFilters(QueryFilter... filters) {
        QueryUtil.addFilters(this, filters);
        return this;
    }

    /**
     * Add a filter that uses the == operator.
     */
    public SearchQuery addFilterEqual(String property, Object value) {
        QueryUtil.addFilterEqual(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the >= operator.
     */
    public SearchQuery addFilterGreaterOrEqual(String property, Object value) {
        QueryUtil.addFilterGreaterOrEqual(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the > operator.
     */
    public SearchQuery addFilterGreaterThan(String property, Object value) {
        QueryUtil.addFilterGreaterThan(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the IN operator.
     */
    public SearchQuery addFilterIn(String property, Collection<?> value) {
        QueryUtil.addFilterIn(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public SearchQuery addFilterIn(String property, Object... value) {
        QueryUtil.addFilterIn(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the NOT IN operator.
     */
    public SearchQuery addFilterNotIn(String property, Collection<?> value) {
        QueryUtil.addFilterNotIn(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the NOT IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public SearchQuery addFilterNotIn(String property, Object... value) {
        QueryUtil.addFilterNotIn(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the <= operator.
     */
    public SearchQuery addFilterLessOrEqual(String property, Object value) {
        QueryUtil.addFilterLessOrEqual(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the < operator.
     */
    public SearchQuery addFilterLessThan(String property, Object value) {
        QueryUtil.addFilterLessThan(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the LIKE operator.
     */
    public SearchQuery addFilterLike(String property, String value) {
        QueryUtil.addFilterLike(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the ILIKE operator.
     */
    public SearchQuery addFilterILike(String property, String value) {
        QueryUtil.addFilterILike(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the != operator.
     */
    public SearchQuery addFilterNotEqual(String property, Object value) {
        QueryUtil.addFilterNotEqual(this, property, value);
        return this;
    }

    /**
     * Add a filter that uses the IS NULL operator.
     */
    public SearchQuery addFilterNull(String property) {
        QueryUtil.addFilterNull(this, property);
        return this;
    }

    /**
     * Add a filter that uses the IS NOT NULL operator.
     */
    public SearchQuery addFilterNotNull(String property) {
        QueryUtil.addFilterNotNull(this, property);
        return this;
    }

    /**
     * Add a filter that uses the IS EMPTY operator.
     */
    public SearchQuery addFilterEmpty(String property) {
        QueryUtil.addFilterEmpty(this, property);
        return this;
    }

    /**
     * Add a filter that uses the IS NOT EMPTY operator.
     */
    public SearchQuery addFilterNotEmpty(String property) {
        QueryUtil.addFilterNotEmpty(this, property);
        return this;
    }

    /**
     * Add a filter that uses the AND operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of <code>Filter
     * </code>s can be specified.
     */
    public SearchQuery addFilterAnd(QueryFilter... filters) {
        QueryUtil.addFilterAnd(this, filters);
        return this;
    }

    /**
     * Add a filter that uses the OR operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of <code>Filter
     * </code>s can be specified.
     */
    public SearchQuery addFilterOr(QueryFilter... filters) {
        QueryUtil.addFilterOr(this, filters);
        return this;
    }

    /**
     * Add a filter that uses the NOT operator.
     */
    public SearchQuery addFilterNot(QueryFilter filter) {
        QueryUtil.addFilterNot(this, filter);
        return this;
    }

    /**
     * Add a filter that uses the SOME operator.
     */
    public SearchQuery addFilterSome(String property, QueryFilter filter) {
        QueryUtil.addFilterSome(this, property, filter);
        return this;
    }

    /**
     * Add a filter that uses the ALL operator.
     */
    public SearchQuery addFilterAll(String property, QueryFilter filter) {
        QueryUtil.addFilterAll(this, property, filter);
        return this;
    }

    /**
     * Add a filter that uses the NONE operator.
     */
    public SearchQuery addFilterNone(String property, QueryFilter filter) {
        QueryUtil.addFilterNone(this, property, filter);
        return this;
    }

    public void removeFilter(QueryFilter filter) {
        QueryUtil.removeFilter(this, filter);
    }

    /**
     * Remove all filters on the given property.
     */
    public void removeFiltersOnProperty(String property) {
        QueryUtil.removeFiltersOnProperty(this, property);
    }

    public void clearFilters() {
        QueryUtil.clearFilters(this);
    }

    public boolean isDisjunction() {
        return disjunction;
    }

    /**
     * Filters added to a search are "ANDed" together if this is false (default)
     * and "ORed" if it is set to true.
     */
    public SearchQuery setDisjunction(boolean disjunction) {
        this.disjunction = disjunction;
        return this;
    }

    // Sorts
    public SearchQuery addSort(Sort sort) {
        QueryUtil.addSort(this, sort);
        return this;
    }

    public SearchQuery addSorts(Sort... sorts) {
        QueryUtil.addSorts(this, sorts);
        return this;
    }

    /**
     * Add ascending sort by property
     */
    public SearchQuery addSortAsc(String property) {
        QueryUtil.addSortAsc(this, property);
        return this;
    }

    /**
     * Add ascending sort by property
     */
    public SearchQuery addSortAsc(String property, boolean ignoreCase) {
        QueryUtil.addSortAsc(this, property, ignoreCase);
        return this;
    }

    /**
     * Add descending sort by property
     */
    public SearchQuery addSortDesc(String property) {
        QueryUtil.addSortDesc(this, property);
        return this;
    }

    /**
     * Add descending sort by property
     */
    public SearchQuery addSortDesc(String property, boolean ignoreCase) {
        QueryUtil.addSortDesc(this, property, ignoreCase);
        return this;
    }

    /**
     * Add sort by property. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public SearchQuery addSort(String property, boolean desc) {
        QueryUtil.addSort(this, property, desc);
        return this;
    }

    /**
     * Add sort by property. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public SearchQuery addSort(String property, boolean desc, boolean ignoreCase) {
        QueryUtil.addSort(this, property, desc, ignoreCase);
        return this;
    }

    public void removeSort(Sort sort) {
        QueryUtil.removeSort(this, sort);
    }

    public void removeSort(String property) {
        QueryUtil.removeSort(this, property);
    }

    public void clearSorts() {
        QueryUtil.clearSorts(this);
    }

    public void removeField(SelectField field) {
        QueryUtil.removeField(this, field);
    }

    public void removeField(String property) {
        QueryUtil.removeField(this, property);
    }

    public void removeField(String property, String key) {
        QueryUtil.removeField(this, property, key);
    }

    public void clearFields() {
        QueryUtil.clearFields(this);
    }

    public boolean isDistinct() {
        return distinct;
    }

    public IMutableQuery setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    public int getResultMode() {
        return resultMode;
    }

    public SearchQuery setResultMode(int resultMode) {
        if (resultMode < 0 || resultMode > 4)
            throw new IllegalArgumentException("Result Mode ( " + resultMode + " ) is not a valid option.");
        this.resultMode = resultMode;
        return this;
    }

    // Fetches
    public SearchQuery addFetch(String property) {
        QueryUtil.addFetch(this, property);
        return this;
    }

    public SearchQuery addFetches(String... properties) {
        QueryUtil.addFetches(this, properties);
        return this;
    }

    public void removeFetch(String property) {
        QueryUtil.removeFetch(this, property);
    }

    public void clearFetches() {
        QueryUtil.clearFetches(this);
    }

    public void clear() {
        QueryUtil.clear(this);
    }

    // Paging
    public int getFirstResult() {
        return firstResult;
    }

    public SearchQuery setFirstResult(int firstResult) {
        this.firstResult = firstResult;
        return this;
    }

    public int getPage() {
        return page;
    }

    public SearchQuery setPage(int page) {
        this.page = page;
        return this;
    }

    public int getMaxResults() {
        return maxResults;
    }

    public SearchQuery setMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public void clearPaging() {
        QueryUtil.clearPaging(this);
    }

    /**
     * Create a copy of this search. All collections are copied into new
     * collections, but them items in those collections are not duplicated; they
     * still point to the same objects.
     */
    public SearchQuery copy() {
        SearchQuery dest = new SearchQuery();
        QueryUtil.copy(this, dest);
        return dest;
    }

    @Override
    public boolean equals(Object obj) {
        return QueryUtil.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return QueryUtil.hashCode(this);
    }

    @Override
    public String toString() {
        return QueryUtil.toString(this);
    }

    public List<QueryFilter> getFilters() {
        return filters;
    }

    public SearchQuery setFilters(List<QueryFilter> filters) {
        this.filters = filters;
        return this;
    }

    public List<Sort> getSorts() {
        return sorts;
    }

    public SearchQuery setSorts(List<Sort> sorts) {
        this.sorts = sorts;
        return this;
    }

    public List<SelectField> getFields() {
        return fields;
    }

    public SearchQuery setFields(List<SelectField> fields) {
        this.fields = fields;
        return this;
    }

    public List<String> getFetches() {
        return fetches;
    }

    public SearchQuery setFetches(List<String> fetches) {
        this.fetches = fetches;
        return this;
    }

}
