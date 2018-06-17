package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManagerFactory;
import javax.persistence.NonUniqueResultException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import skyglass.query.metadata.MetadataHelper;
import skyglass.query.model.criteria.IQueryBuilder;

/**
 * Implementation of BaseSearchProcessor that generates Works with standard JPA.
 * 
 * A singleton instance of this class is maintained for each
 * EntityManagerFactory. This should be accessed using
 * {@link QueryProcessor#getInstanceForEntityManagerFactory(EntityManagerFactory)}.
 * 
 */
public class QueryProcessor<E, S> extends BaseQueryProcessor {
    private static Logger logger = LoggerFactory.getLogger(QueryProcessor.class);

    private IQueryBuilder<E, S> queryBuilder;
    
    public static <E, S> QueryProcessor<E, S> getInstance(IQueryBuilder<E, S> queryBuilder, MetadataHelper metadataHelper, ISearchQuery searchQuery) {
        return new QueryProcessor<E, S>(metadataHelper, queryBuilder, searchQuery);
    }

    private QueryProcessor(MetadataHelper metadataHelper, IQueryBuilder<E, S> queryBuilder, ISearchQuery searchQuery) {
        super(QLTYPE_JPQL, metadataHelper, searchQuery);
        this.queryBuilder = queryBuilder;
    }

    // --- Public Methods ---

    /**
     * Search for objects based on the search parameters in the specified
     * <code>ISearch</code> object. Uses the specified searchClass, ignoring the
     * searchClass specified on the search itself.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings("rawtypes")
    public List search() {
        if (searchClass == null || searchQuery == null)
            return null;        
        String jpql = generateQL();
        IQuery query = queryBuilder.createQuery(jpql);
        addParams(query, paramList);
        addPaging(query);
        addResultMode();

        return query.getResultList();
    }

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResult limits.
     * 
     * @see ISearchQuery
     */
    public int count(ISearchQuery search) {
        if (search == null)
            return 0;
        return count(search.getSearchClass(), search);
    }

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResult limits.
     * Uses the specified searchClass, ignoring the searchClass specified on the
     * search itself.
     * 
     * @see ISearchQuery
     */
    public int count(Class<?> searchClass, ISearchQuery search) {
        if (searchClass == null || search == null)
            return 0;

        String jpql = generateRowCountQL();
        if (jpql == null) { // special case where the query uses column
                            // operators
            return 1;
        }
        IQuery query = queryBuilder.createQuery(jpql);
        addParams(query, paramList);

        return ((Number) query.getSingleResult()).intValue();
    }

    /**
     * Returns a <code>SearchResult</code> object that includes the list of
     * results like <code>search()</code> and the total length like
     * <code>searchLength</code>. Uses the specified searchClass, ignoring the
     * searchClass specified on the search itself.
     * 
     * @see ISearchQuery
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public QueryResult searchAndCount() {
        if (searchClass == null || searchQuery == null)
            return null;

        QueryResult result = new QueryResult();
        result.setResult(search());

        if (searchQuery.getMaxResults() > 0) {
            result.setTotalCount(count(searchClass, searchQuery));
        } else {
            result.setTotalCount(result.getResult().size() + QueryUtil.calcFirstResult(searchQuery));
        }

        return result;
    }

    /**
     * Search for a single result using the given parameters. Uses the specified
     * searchClass, ignoring the searchClass specified on the search itself.
     */
    public Object searchUnique() throws NonUniqueResultException {
        if (searchQuery == null)
            return null;

        String hql = generateQL();
        IQuery query = queryBuilder.createQuery(hql);
        addParams(query, paramList);
        addResultMode();

        return query.getSingleResult();
    }

    // ---- SEARCH HELPERS ---- //

    @SuppressWarnings("rawtypes")
    private void addParams(IQuery query, List<Object> params) {
        StringBuilder debug = null;

        int i = 1;
        for (Object o : params) {
            if (logger.isDebugEnabled()) {
                if (debug == null)
                    debug = new StringBuilder();
                else
                    debug.append("\n\t");
                debug.append("p");
                debug.append(i);
                debug.append(": ");
                debug.append(InternalUtil.paramDisplayString(o));
            }
            if (o instanceof Collection) {
                setParameterList(query, "p" + Integer.toString(i++), (Collection) o);
            } else if (o instanceof Object[]) {
                setParameterList(query, "p" + Integer.toString(i++), (Object[]) o);
            } else {
                query.setParameter("p" + Integer.toString(i++), o);
            }
        }
        if (debug != null && debug.length() != 0) {
            logger.debug(debug.toString());
        }
    }

    private void setParameterList(IQuery query, String name, Object[] list) {
        for (Object o : list) {
            query.setParameter(name, o);
        }
    }

    @SuppressWarnings("rawtypes")
    private void setParameterList(IQuery query, String name, Collection list) {
        for (Object o : list) {
            query.setParameter(name, o);
        }
    }

    private void addPaging(IQuery query) {
        int firstResult = QueryUtil.calcFirstResult(searchQuery);
        if (firstResult > 0) {
            query.setFirstResult(firstResult);
        }
        if (searchQuery.getMaxResults() > 0) {
            query.setMaxResults(searchQuery.getMaxResults());
        }
    }

    private void addResultMode() {
        int resultMode = searchQuery.getResultMode();
        if (resultMode == ISearchQuery.RESULT_AUTO) {
            int count = 0;
            Iterator<Field> fieldItr = searchQuery.getFields().iterator();
            while (fieldItr.hasNext()) {
                Field field = fieldItr.next();
                if (field.getKey() != null && !field.getKey().equals("")) {
                    resultMode = ISearchQuery.RESULT_MAP;
                    break;
                }
                count++;
            }
            if (resultMode == ISearchQuery.RESULT_AUTO) {
                if (count > 1)
                    resultMode = ISearchQuery.RESULT_ARRAY;
                else
                    resultMode = ISearchQuery.RESULT_SINGLE;
            }
        }

        switch (resultMode) {
        case ISearchQuery.RESULT_ARRAY:
            // TODO: how to set result transformer on jpa query?
            // query.setResultTransformer(ARRAY_RESULT_TRANSFORMER);
            break;
        case ISearchQuery.RESULT_LIST:
            // query.setResultTransformer(Transformers.TO_LIST);
            break;
        case ISearchQuery.RESULT_MAP:
            List<String> keyList = new ArrayList<String>();
            Iterator<Field> fieldItr = searchQuery.getFields().iterator();
            while (fieldItr.hasNext()) {
                Field field = fieldItr.next();
                if (field.getKey() != null && !field.getKey().equals("")) {
                    keyList.add(field.getKey());
                } else {
                    keyList.add(field.getProperty());
                }
            }
            // query.setResultTransformer(new
            // MapResultTransformer(keyList.toArray(new String[0])));
            break;
        default: // ISearch.RESULT_SINGLE
            break;
        }
    }

    /*
     * private static final ResultTransformer ARRAY_RESULT_TRANSFORMER = new
     * ResultTransformer() { private static final long serialVersionUID = 1L;
     * 
     * public List transformList(List collection) { return collection; }
     * 
     * public Object transformTuple(Object[] tuple, String[] aliases) { return
     * tuple; } };
     */

    /*
     * private static class MapResultTransformer implements ResultTransformer {
     * private static final long serialVersionUID = 1L;
     * 
     * private String[] keys;
     * 
     * public MapResultTransformer(String[] keys) { this.keys = keys; }
     * 
     * public List transformList(List collection) { return collection; }
     * 
     * public Object transformTuple(Object[] tuple, String[] aliases) {
     * Map<String, Object> map = new HashMap<String, Object>(); for (int i = 0;
     * i < keys.length; i++) { String key = keys[i]; if (key != null) {
     * map.put(key, tuple[i]); } }
     * 
     * return map; } }
     */

}
