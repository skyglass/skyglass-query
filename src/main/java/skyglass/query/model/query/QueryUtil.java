package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import skyglass.data.filter.PrivateCompositeFilterItem;
import skyglass.data.filter.PrivateFilterItem;

/**
 * Utilities for working with searches {@link ISearchQuery}, {@link IMutableQuery}.
 */
public class QueryUtil {

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
    public static Collection<PrivateFilterItem> walkFilters(
    		Collection<PrivateFilterItem> filters, FilterVisitor visitor,
            boolean removeNulls) {
        return walkList(filters, new FilterListVisitor(visitor, removeNulls), removeNulls);
    }

    /**
     * Used in walkFilters
     */
    private static final class FilterListVisitor extends ItemVisitor<PrivateFilterItem> {
        private FilterVisitor visitor;
        private boolean removeNulls;

        public FilterListVisitor(FilterVisitor visitor, boolean removeNulls) {
            this.visitor = visitor;
            this.removeNulls = removeNulls;
        }

        @Override
        public PrivateFilterItem visit(PrivateFilterItem filter) {
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
    public static PrivateFilterItem walkFilter(PrivateFilterItem filter, FilterVisitor visitor, boolean removeNulls) {
        filter = visitor.visitBefore(filter);

        if (filter != null) {
            if (filter instanceof PrivateCompositeFilterItem) {
            	PrivateCompositeFilterItem compositeFilter = (PrivateCompositeFilterItem)filter;
                walkFilter(
                		compositeFilter.getSingleChild(), visitor, removeNulls);
            } else if (filter.isTakesListOfSubFilters()) {
            	PrivateCompositeFilterItem compositeFilter = (PrivateCompositeFilterItem)filter;
                walkFilters(compositeFilter.getChildren(), visitor, removeNulls); 
            }
        }

        filter = visitor.visitAfter(filter);

        return filter;
    }

    /**
     * Visitor for use with walkFilter and walkFilters
     */
    public static class FilterVisitor {
        public PrivateFilterItem visitBefore(PrivateFilterItem filter) {
            return filter;
        }

        public PrivateFilterItem visitAfter(PrivateFilterItem filter) {
            return filter;
        }
    }

}
