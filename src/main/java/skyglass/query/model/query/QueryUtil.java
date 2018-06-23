package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import skyglass.data.filter.SelectType;

/**
 * Utilities for working with searches {@link ISearchQuery}, {@link IMutableQuery}.
 */
public class QueryUtil {
    // ---------- Add ----------

    public static void addFetch(IMutableQuery search, String property) {
        if (property == null || "".equals(property))
            return; // null properties do nothing, don't bother to add them.

        List<String> fetches = search.getFetches();
        if (fetches == null) {
            fetches = new ArrayList<String>();
            search.setFetches(fetches);
        }
        fetches.add(property);
    }

    public static void addFetches(IMutableQuery search, String... properties) {
        if (properties != null) {
            for (String property : properties) {
                addFetch(search, property);
            }
        }
    }

    public static void addSelectField(IMutableQuery search, SelectField field) {
        List<SelectField> fields = search.getFields();
        if (fields == null) {
            fields = new ArrayList<SelectField>();
            search.setFields(fields);
        }
        fields.add(field);
    }

    public static void addSelectFields(IMutableQuery search, SelectField... fields) {
        if (fields != null) {
            for (SelectField field : fields) {
                addSelectField(search, field);
            }
        }
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <code>property</code> will also be used as the key for this value in the
     * map.
     */
    public static void addSelectField(IMutableQuery search, String property) {
        if (property == null)
            return; // null properties do nothing, don't bother to add them.
        addSelectField(search, new SelectField(property));
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <code>property</code> will also be used as the key for this value in the
     * map.
     */
    public static void addSelectField(IMutableQuery search, String property, SelectType operator) {
        if (property == null)
            return; // null properties do nothing, don't bother to add them.
        addSelectField(search, new SelectField(property, operator));
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <code>key</code> will be used as the key for this value in the map.
     */
    public static void addSelectField(IMutableQuery search, String property, SelectType operator, String key) {
        if (property == null || key == null)
            return; // null properties do nothing, don't bother to add them.
        addSelectField(search, new SelectField(property, operator, key));
    }

    /**
     * If this field is used with <code>resultMode == RESULT_MAP</code>, the
     * <code>key</code> will be used as the key for this value in the map.
     */
    public static void addSelectField(IMutableQuery search, String property, String key) {
        if (property == null || key == null)
            return; // null properties do nothing, don't bother to add them.
        addSelectField(search, new SelectField(property, key));
    }

    public static void addFilter(IMutableQuery search, QueryFilter filter) {
        List<QueryFilter> filters = search.getFilters();
        if (filters == null) {
            filters = new ArrayList<QueryFilter>();
            search.setFilters(filters);
        }
        filters.add(filter);
    }

    public static void addFilters(IMutableQuery search, QueryFilter... filters) {
        if (filters != null) {
            for (QueryFilter filter : filters) {
                addFilter(search, filter);
            }
        }
    }

    /**
     * Add a filter that uses the ALL operator.
     */
    public static void addFilterAll(IMutableQuery search, String property, QueryFilter filter) {
        addFilter(search, QueryFilter.all(property, filter));
    }

    /**
     * Add a filter that uses the AND operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of <code>Filter
     * </code>s can be specified.
     */
    public static void addFilterAnd(IMutableQuery search, QueryFilter... filters) {
        addFilter(search, QueryFilter.and(filters));
    }

    /**
     * Add a filter that uses the IS EMPTY operator.
     */
    public static void addFilterEmpty(IMutableQuery search, String property) {
        addFilter(search, QueryFilter.isEmpty(property));
    }

    /**
     * Add a filter that uses the == operator.
     */
    public static void addFilterEqual(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.equal(property, value));
    }

    /**
     * Add a filter that uses the >= operator.
     */
    public static void addFilterGreaterOrEqual(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.greaterOrEqual(property, value));
    }

    /**
     * Add a filter that uses the > operator.
     */
    public static void addFilterGreaterThan(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.greaterThan(property, value));
    }

    /**
     * Add a filter that uses the ILIKE operator.
     */
    public static void addFilterILike(IMutableQuery search, String property, String value) {
        addFilter(search, QueryFilter.ilike(property, value));
    }

    /**
     * Add a filter that uses the IN operator.
     */
    public static void addFilterIn(IMutableQuery search, String property, Collection<?> value) {
        addFilter(search, QueryFilter.in(property, value));
    }

    /**
     * Add a filter that uses the IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static void addFilterIn(IMutableQuery search, String property, Object... value) {
        addFilter(search, QueryFilter.in(property, value));
    }

    /**
     * Add a filter that uses the <= operator.
     */
    public static void addFilterLessOrEqual(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.lessOrEqual(property, value));
    }

    /**
     * Add a filter that uses the < operator.
     */
    public static void addFilterLessThan(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.lessThan(property, value));
    }

    /**
     * Add a filter that uses the LIKE operator.
     */
    public static void addFilterLike(IMutableQuery search, String property, String value) {
        addFilter(search, QueryFilter.like(property, value));
    }

    /**
     * Add a filter that uses the NONE operator.
     */
    public static void addFilterNone(IMutableQuery search, String property, QueryFilter filter) {
        addFilter(search, QueryFilter.none(property, filter));
    }

    /**
     * Add a filter that uses the NOT operator.
     */
    public static void addFilterNot(IMutableQuery search, QueryFilter filter) {
        addFilter(search, QueryFilter.not(filter));
    }

    /**
     * Add a filter that uses the != operator.
     */
    public static void addFilterNotEqual(IMutableQuery search, String property, Object value) {
        addFilter(search, QueryFilter.notEqual(property, value));
    }

    /**
     * Add a filter that uses the NOT IN operator.
     */
    public static void addFilterNotIn(IMutableQuery search, String property, Collection<?> value) {
        addFilter(search, QueryFilter.notIn(property, value));
    }

    /**
     * Add a filter that uses the NOT IN operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of values can be
     * specified.
     */
    public static void addFilterNotIn(IMutableQuery search, String property, Object... value) {
        addFilter(search, QueryFilter.notIn(property, value));
    }

    /**
     * Add a filter that uses the IS NOT EMPTY operator.
     */
    public static void addFilterNotEmpty(IMutableQuery search, String property) {
        addFilter(search, QueryFilter.isNotEmpty(property));
    }

    /**
     * Add a filter that uses the IS NOT NULL operator.
     */
    public static void addFilterNotNull(IMutableQuery search, String property) {
        addFilter(search, QueryFilter.isNotNull(property));
    }

    /**
     * Add a filter that uses the IS NULL operator.
     */
    public static void addFilterNull(IMutableQuery search, String property) {
        addFilter(search, QueryFilter.isNull(property));
    }

    /**
     * Add a filter that uses the OR operator.
     * 
     * <p>
     * This takes a variable number of parameters. Any number of <code>Filter
     * </code>s can be specified.
     */
    public static void addFilterOr(IMutableQuery search, QueryFilter... filters) {
        addFilter(search, QueryFilter.or(filters));
    }

    /**
     * Add a filter that uses the SOME operator.
     */
    public static void addFilterSome(IMutableQuery search, String property, QueryFilter filter) {
        addFilter(search, QueryFilter.some(property, filter));
    }

    // Sorts
    public static void addSort(IMutableQuery search, Sort sort) {
        if (sort == null)
            return;

        List<Sort> sorts = search.getSorts();
        if (sorts == null) {
            sorts = new ArrayList<Sort>();
            search.setSorts(sorts);
        }
        sorts.add(sort);
    }

    public static void addSorts(IMutableQuery search, Sort... sorts) {
        if (sorts != null) {
            for (Sort sort : sorts) {
                addSort(search, sort);
            }
        }
    }

    /**
     * Add sort by property. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public static void addSort(IMutableQuery search, String property, boolean desc) {
        addSort(search, property, desc, false);
    }

    /**
     * Add sort by property. Ascending if <code>desc == false</code>, descending
     * if <code>desc == true</code>.
     */
    public static void addSort(IMutableQuery search, String property, boolean desc, boolean ignoreCase) {
        if (property == null)
            return; // null properties do nothing, don't bother to add them.
        addSort(search, new Sort(property, desc, ignoreCase));
    }

    /**
     * Add ascending sort by property
     */
    public static void addSortAsc(IMutableQuery search, String property) {
        addSort(search, property, false, false);
    }

    /**
     * Add ascending sort by property
     */
    public static void addSortAsc(IMutableQuery search, String property, boolean ignoreCase) {
        addSort(search, property, false, ignoreCase);
    }

    /**
     * Add descending sort by property
     */
    public static void addSortDesc(IMutableQuery search, String property) {
        addSort(search, property, true, false);
    }

    /**
     * Add descending sort by property
     */
    public static void addSortDesc(IMutableQuery search, String property, boolean ignoreCase) {
        addSort(search, property, true, ignoreCase);
    }

    // ---------- Remove ----------

    public static void removeFetch(IMutableQuery search, String property) {
        if (search.getFetches() != null)
            search.getFetches().remove(property);
    }

    public static void removeField(IMutableQuery search, SelectField field) {
        if (search.getFields() != null)
            search.getFields().remove(field);
    }

    public static void removeField(IMutableQuery search, String property) {
        if (search.getFields() == null)
            return;

        Iterator<SelectField> itr = search.getFields().iterator();
        while (itr.hasNext()) {
            if (itr.next().getProperty().equals(property))
                itr.remove();
        }
    }

    public static void removeField(IMutableQuery search, String property, String key) {
        if (search.getFields() == null)
            return;

        Iterator<SelectField> itr = search.getFields().iterator();
        while (itr.hasNext()) {
            SelectField field = itr.next();
            if (field.getProperty().equals(property) && field.getKey().equals(key))
                itr.remove();
        }
    }

    public static void removeFilter(IMutableQuery search, QueryFilter filter) {
        List<QueryFilter> filters = search.getFilters();
        if (filters != null)
            filters.remove(filter);
    }

    /**
     * Remove all filters on the given property.
     */
    public static void removeFiltersOnProperty(IMutableQuery search, String property) {
        if (property == null || search.getFilters() == null)
            return;
        Iterator<QueryFilter> itr = search.getFilters().iterator();
        while (itr.hasNext()) {
            if (property.equals(itr.next().getProperty()))
                itr.remove();
        }
    }

    public static void removeSort(IMutableQuery search, Sort sort) {
        if (search.getSorts() != null)
            search.getSorts().remove(sort);
    }

    public static void removeSort(IMutableQuery search, String property) {
        if (property == null || search.getSorts() == null)
            return;
        Iterator<Sort> itr = search.getSorts().iterator();
        while (itr.hasNext()) {
            if (property.equals(itr.next().getProperty()))
                itr.remove();
        }
    }

    // ---------- Clear ----------

    public static void clear(IMutableQuery search) {
        clearFilters(search);
        clearSorts(search);
        clearFields(search);
        clearPaging(search);
        clearFetches(search);
        search.setResultMode(ISearchQuery.RESULT_AUTO);
        search.setDisjunction(false);
    }

    public static void clearFetches(IMutableQuery search) {
        if (search.getFetches() != null)
            search.getFetches().clear();
    }

    public static void clearFields(IMutableQuery search) {
        if (search.getFields() != null)
            search.getFields().clear();
    }

    public static void clearFilters(IMutableQuery search) {
        if (search.getFilters() != null)
            search.getFilters().clear();
    }

    public static void clearPaging(IMutableQuery search) {
        search.setFirstResult(-1);
        search.setPage(-1);
        search.setMaxResults(-1);
    }

    public static void clearSorts(IMutableQuery search) {
        if (search.getSorts() != null)
            search.getSorts().clear();
    }

    // ---------- Merge ----------
    /**
     * Modify the search by adding the given sorts before the current sorts in
     * the search.
     */
    public static void mergeSortsBefore(IMutableQuery search, List<Sort> sorts) {
        List<Sort> list = search.getSorts();
        if (list == null) {
            list = new ArrayList<Sort>();
            search.setSorts(list);
        }

        if (list.size() > 0) {
            // remove any sorts from the search that already sort on the same
            // property as one of the new sorts
            Iterator<Sort> itr = list.iterator();
            while (itr.hasNext()) {
                String property = itr.next().getProperty();
                if (property == null) {
                    itr.remove();
                } else {
                    for (Sort sort : sorts) {
                        if (property.equals(sort.getProperty())) {
                            itr.remove();
                            break;
                        }
                    }
                }
            }
        }

        list.addAll(0, sorts);
    }

    /**
     * Modify the search by adding the given sorts before the current sorts in
     * the search.
     */
    public static void mergeSortsBefore(IMutableQuery search, Sort... sorts) {
        mergeSortsBefore(search, Arrays.asList(sorts));
    }

    /**
     * Modify the search by adding the given sorts after the current sorts in
     * the search.
     */
    public static void mergeSortsAfter(IMutableQuery search, List<Sort> sorts) {
        List<Sort> list = search.getSorts();
        if (list == null) {
            list = new ArrayList<Sort>();
            search.setSorts(list);
        }

        int origLen = list.size();

        if (origLen > 0) {
            // don't add sorts that are already in the list
            for (Sort sort : sorts) {
                if (sort.getProperty() != null) {
                    boolean found = false;
                    for (int i = 0; i < origLen; i++) {
                        if (sort.getProperty().equals(list.get(i).getProperty())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                        list.add(sort);
                }
            }
        } else {
            list.addAll(sorts);
        }
    }

    /**
     * Modify the search by adding the given sorts after the current sorts in
     * the search.
     */
    public static void mergeSortsAfter(IMutableQuery search, Sort... sorts) {
        mergeSortsAfter(search, Arrays.asList(sorts));
    }

    /**
     * Modify the search by adding the given fetches to the current fetches in
     * the search.
     */
    public static void mergeFetches(IMutableQuery search, List<String> fetches) {
        List<String> list = search.getFetches();
        if (list == null) {
            list = new ArrayList<String>();
            search.setFetches(list);
        }

        for (String fetch : fetches) {
            if (!list.contains(fetch)) {
                list.add(fetch);
            }
        }
    }

    /**
     * Modify the search by adding the given fetches to the current fetches in
     * the search.
     */
    public static void mergeFetches(IMutableQuery search, String... fetches) {
        mergeFetches(search, Arrays.asList(fetches));
    }

    /**
     * Modify the search by adding the given filters using AND semantics
     */
    public static void mergeFiltersAnd(IMutableQuery search, List<QueryFilter> filters) {
        List<QueryFilter> list = search.getFilters();
        if (list == null) {
            list = new ArrayList<QueryFilter>();
            search.setFilters(list);
        }

        if (list.size() == 0 || !search.isDisjunction()) {
            search.setDisjunction(false);
            list.addAll(filters);
        } else {
            search.setFilters(new ArrayList<QueryFilter>());

            // add the previous filters with an OR
            QueryFilter orFilter = QueryFilter.or();
            orFilter.setValue(list);
            addFilter(search, orFilter);

            // add the new filters with AND
            search.setDisjunction(false);
            search.getFilters().addAll(filters);
        }
    }

    /**
     * Modify the search by adding the given filters using AND semantics
     */
    public static void mergeFiltersAnd(IMutableQuery search, QueryFilter... filters) {
        mergeFiltersAnd(search, Arrays.asList(filters));
    }

    /**
     * Modify the search by adding the given filters using OR semantics
     */
    public static void mergeFiltersOr(IMutableQuery search, List<QueryFilter> filters) {
        List<QueryFilter> list = search.getFilters();
        if (list == null) {
            list = new ArrayList<QueryFilter>();
            search.setFilters(list);
        }

        if (list.size() == 0 || search.isDisjunction()) {
            search.setDisjunction(true);
            list.addAll(filters);
        } else {
            search.setFilters(new ArrayList<QueryFilter>());

            // add the previous filters with an AND
            QueryFilter orFilter = QueryFilter.and();
            orFilter.setValue(list);
            addFilter(search, orFilter);

            // add the new filters with or
            search.setDisjunction(true);
            search.getFilters().addAll(filters);
        }
    }

    /**
     * Modify the search by adding the given filters using OR semantics
     */
    public static void mergeFiltersOr(IMutableQuery search, QueryFilter... filters) {
        mergeFiltersOr(search, Arrays.asList(filters));
    }

    /**
     * Modify the search by adding the given fields before the current fields in
     * the search.
     */
    public static void mergeFieldsBefore(IMutableQuery search, List<SelectField> fields) {
        List<SelectField> list = search.getFields();
        if (list == null) {
            list = new ArrayList<SelectField>();
            search.setFields(list);
        }

        list.addAll(0, fields);
    }

    /**
     * Modify the search by adding the given fields before the current fields in
     * the search.
     */
    public static void mergeFieldsBefore(IMutableQuery search, SelectField... fields) {
        mergeFieldsBefore(search, Arrays.asList(fields));
    }

    /**
     * Modify the search by adding the given fields after the current fields in
     * the search.
     */
    public static void mergeFieldsAfter(IMutableQuery search, List<SelectField> fields) {
        List<SelectField> list = search.getFields();
        if (list == null) {
            list = new ArrayList<SelectField>();
            search.setFields(list);
        }

        list.addAll(fields);
    }

    /**
     * Modify the search by adding the given fields after the current fields in
     * the search.
     */
    public static void mergeFieldsAfter(IMutableQuery search, SelectField... fields) {
        mergeFieldsAfter(search, Arrays.asList(fields));
    }

    // ---------- Other Methods ----------

    /**
     * Calculate the first result to use given the <code>firstResult</code>,
     * <code>page</code> and <code>maxResults</code> values of the search
     * object.
     * 
     * <p>
     * The calculation is as follows:
     * <ul>
     * <li>If <code>firstResult</code> is defined (i.e. > 0), use it.
     * <li>Otherwise if <code>page</code> and <code>maxResults</code> are
     * defined (i.e. > 0), use <code>page * maxResults</code>.
     * <li>Otherwise, just use 0.
     * </ul>
     */
    public static int calcFirstResult(ISearchQuery search) {
        return (search.getFirstResult() > 0) ? search.getFirstResult()
                : (search.getPage() > 0 && search.getMaxResults() > 0) ? search.getPage() * search.getMaxResults() : 0;
    }

    /**
     * Copy the contents of the source search object to the destination search
     * object, overriding any contents previously found in the destination. All
     * destination properties reference the same objects from the source
     * properties.
     */
    public static IMutableQuery shallowCopy(ISearchQuery source, IMutableQuery destination) {
        destination.setSearchClass(source.getSearchClass());
        destination.setDistinct(source.isDistinct());
        destination.setDisjunction(source.isDisjunction());
        destination.setResultMode(source.getResultMode());
        destination.setFirstResult(source.getFirstResult());
        destination.setPage(source.getPage());
        destination.setMaxResults(source.getMaxResults());
        destination.setFetches(source.getFetches());
        destination.setFields(source.getFields());
        destination.setFilters(source.getFilters());
        destination.setSorts(source.getSorts());

        return destination;
    }

    /**
     * Copy the contents of the source search object to the destination search
     * object, overriding any contents previously found in the destination. All
     * destination properties reference the same objects from the source
     * properties.
     */
    public static IMutableQuery shallowCopy(ISearchQuery source) {
        return shallowCopy(source, new SearchQuery());
    }

    /**
     * Copy the contents of the source search object to the destination search
     * object, overriding any contents previously found in the destination. All
     * collections are copied into new collections, but the items in those
     * collections are not duplicated; they still point to the same objects.
     */
    public static <T extends IMutableQuery> T copy(ISearchQuery source, T destination) {
        shallowCopy(source, destination);

        ArrayList<String> fetches = new ArrayList<String>();
        fetches.addAll(source.getFetches());
        destination.setFetches(fetches);

        ArrayList<SelectField> fields = new ArrayList<SelectField>();
        fields.addAll(source.getFields());
        destination.setFields(fields);

        ArrayList<QueryFilter> filters = new ArrayList<QueryFilter>();
        filters.addAll(source.getFilters());
        destination.setFilters(filters);

        ArrayList<Sort> sorts = new ArrayList<Sort>();
        sorts.addAll(source.getSorts());
        destination.setSorts(sorts);

        return destination;
    }

    /**
     * Copy the contents of the source search object into a new search object.
     * All collections are copied into new collections, but the items in those
     * collections are not duplicated; they still point to the same objects.
     */
    public static IMutableQuery copy(ISearchQuery source) {
        return copy(source, new SearchQuery());
    }

    /**
     * Return true if the search objects have equivalent contents.
     */
    public static boolean equals(ISearchQuery search, Object obj) {
        if (search == obj)
            return true;
        if (!(obj instanceof ISearchQuery))
            return false;
        ISearchQuery s = (ISearchQuery) obj;

        if (search.getSearchClass() == null ? s.getSearchClass() != null
                : !search.getSearchClass().equals(s.getSearchClass()))
            return false;
        if (search.isDisjunction() != s.isDisjunction() || search.getResultMode() != s.getResultMode()
                || search.getFirstResult() != s.getFirstResult() || search.getPage() != s.getPage()
                || search.getMaxResults() != s.getMaxResults())
            return false;

        if (search.getFetches() == null ? s.getFetches() != null : !search.getFetches().equals(s.getFetches()))
            return false;
        if (search.getFields() == null ? s.getFields() != null : !search.getFields().equals(s.getFields()))
            return false;
        if (search.getFilters() == null ? s.getFilters() != null : !search.getFilters().equals(s.getFilters()))
            return false;
        if (search.getSorts() == null ? s.getSorts() != null : !search.getSorts().equals(s.getSorts()))
            return false;

        return true;
    }

    /**
     * Return a hash code value for the given search.
     */
    public static int hashCode(ISearchQuery search) {
        int hash = 1;
        hash = hash * 31 + (search.getSearchClass() == null ? 0 : search.getSearchClass().hashCode());
        hash = hash * 31 + (search.getFields() == null ? 0 : search.getFields().hashCode());
        hash = hash * 31 + (search.getFilters() == null ? 0 : search.getFilters().hashCode());
        hash = hash * 31 + (search.getSorts() == null ? 0 : search.getSorts().hashCode());
        hash = hash * 31 + (search.isDisjunction() ? 1 : 0);
        hash = hash * 31 + (new Integer(search.getResultMode()).hashCode());
        hash = hash * 31 + (new Integer(search.getFirstResult()).hashCode());
        hash = hash * 31 + (new Integer(search.getPage()).hashCode());
        hash = hash * 31 + (new Integer(search.getMaxResults()).hashCode());

        return hash;
    }

    /**
     * Return a human-readable string describing the contents of the given
     * search.
     */
    public static String toString(ISearchQuery search) {
        StringBuilder sb = new StringBuilder("Search(");
        sb.append(search.getSearchClass());
        sb.append(")[first: ").append(search.getFirstResult());
        sb.append(", page: ").append(search.getPage());
        sb.append(", max: ").append(search.getMaxResults());
        sb.append("] {\n resultMode: ");

        switch (search.getResultMode()) {
        case ISearchQuery.RESULT_AUTO:
            sb.append("AUTO");
            break;
        case ISearchQuery.RESULT_ARRAY:
            sb.append("ARRAY");
            break;
        case ISearchQuery.RESULT_LIST:
            sb.append("LIST");
            break;
        case ISearchQuery.RESULT_MAP:
            sb.append("MAP");
            break;
        case ISearchQuery.RESULT_SINGLE:
            sb.append("SINGLE");
            break;
        default:
            sb.append("**INVALID RESULT MODE: (" + search.getResultMode() + ")**");
            break;
        }

        sb.append(",\n disjunction: ").append(search.isDisjunction());
        sb.append(",\n fields: { ");
        appendList(sb, search.getFields(), ", ");
        sb.append(" },\n filters: {\n  ");
        appendList(sb, search.getFilters(), ",\n  ");
        sb.append("\n },\n sorts: { ");
        appendList(sb, search.getSorts(), ", ");
        sb.append(" }\n}");

        return sb.toString();
    }

    private static void appendList(StringBuilder sb, List<?> list, String separator) {
        if (list == null) {
            sb.append("null");
            return;
        }

        boolean first = true;
        for (Object o : list) {
            if (first) {
                first = false;
            } else {
                sb.append(separator);
            }
            sb.append(o);
        }
    }

    /**
     * Visit each non-null item is a list. Each item may be replaced by the
     * visitor. The modified list is returned. If removeNulls is true, any null
     * elements will be removed from the final list.
     * 
     * <p>
     * If there are any modifications to be made to the list a new list is made
     * with the changes so that the original list remains unchanged. If no
     * changes are made, the original list is returned.
     */
    public static <T> Collection<T> walkList(Collection<T> list, ItemVisitor<T> visitor, boolean removeNulls) {
        if (list == null)
            return null;

        ArrayList<T> copy = null;

        int i = 0;
        for (T item : list) {
            T result = visitor.visit(item);
            if (result != item || (removeNulls && result == null)) {
                if (copy == null) {
                    copy = new ArrayList<T>(list.size());
                    copy.addAll(list);
                }
                copy.set(i, result);
                item = result;
            }
            i++;
        }
        if (copy != null) {
            if (removeNulls) {
                for (int j = copy.size() - 1; j >= 0; j--) {
                    if (copy.get(j) == null)
                        copy.remove(j);
                }
            }
            return copy;
        } else {
            return list;
        }
    }

    /**
     * Visitor for use with walkList()
     */
    public static class ItemVisitor<T> {
        public T visit(T item) {
            return item;
        }
    }

    /**
     * Walk through a list of filters and all the sub filters, visiting each
     * filter in the tree. A FilterVisitor is used to visit each filter. The
     * FilterVisitor may replace the Filter that is is visiting. If it does, a
     * new tree and list of Filters will be created for every part of the tree
     * that is affected, thus preserving the original tree.
     * 
     * @return if any changes have been made, the new list of Filters; if not,
     *         the original list.
     */
    public static Collection<QueryFilter> walkFilters(Collection<QueryFilter> filters, FilterVisitor visitor,
            boolean removeNulls) {
        return walkList(filters, new FilterListVisitor(visitor, removeNulls), removeNulls);
    }

    /**
     * Used in walkFilters
     */
    private static final class FilterListVisitor extends ItemVisitor<QueryFilter> {
        private FilterVisitor visitor;
        private boolean removeNulls;

        public FilterListVisitor(FilterVisitor visitor, boolean removeNulls) {
            this.visitor = visitor;
            this.removeNulls = removeNulls;
        }

        @Override
        public QueryFilter visit(QueryFilter filter) {
            return walkFilter(filter, visitor, removeNulls);
        }
    }

    /**
     * Walk a filter and all its sub filters, visiting each filter in the tree.
     * A FilterVisitor is used to visit each filter. The FilterVisitor may
     * replace the Filter that is is visiting. If it does, a new tree and will
     * be created for every part of the tree that is affected, thus preserving
     * the original tree.
     * 
     * @return if any changes have been made, the new Filter; if not, the
     *         original Filter.
     */
    @SuppressWarnings("unchecked")
    public static QueryFilter walkFilter(QueryFilter filter, FilterVisitor visitor, boolean removeNulls) {
        filter = visitor.visitBefore(filter);

        if (filter != null) {
            if (filter.isTakesSingleSubFilter()) {
                if (filter.getValue() instanceof QueryFilter) {
                    QueryFilter result = walkFilter((QueryFilter) filter.getValue(), visitor, removeNulls);
                    if (result != filter.getValue()) {
                        filter = new QueryFilter(filter.getProperty(), result, filter.getOperator());
                    }
                }
            } else if (filter.isTakesListOfSubFilters()) {
                if (filter.getValue() instanceof List) {
                    Collection<QueryFilter> result = walkFilters((List<QueryFilter>) filter.getValue(), visitor,
                            removeNulls);
                    if (result != filter.getValue()) {
                        filter = new QueryFilter(filter.getProperty(), result, filter.getOperator());
                    }
                }
            }
        }

        filter = visitor.visitAfter(filter);

        return filter;
    }

    /**
     * Visitor for use with walkFilter and walkFilters
     */
    public static class FilterVisitor {
        public QueryFilter visitBefore(QueryFilter filter) {
            return filter;
        }

        public QueryFilter visitAfter(QueryFilter filter) {
            return filter;
        }
    }

}
