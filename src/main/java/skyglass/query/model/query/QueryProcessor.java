package skyglass.query.model.query;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManagerFactory;

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
    private IQueryBuilder<E, S> queryBuilder;
    
    public static <E, S> QueryProcessor<E, S> getInstance(IQueryBuilder<E, S> queryBuilder, MetadataHelper metadataHelper) {
        return new QueryProcessor<E, S>(metadataHelper, queryBuilder);
    }

    private QueryProcessor(MetadataHelper metadataHelper, IQueryBuilder<E, S> queryBuilder) {
        super(QLTYPE_JPQL, metadataHelper, queryBuilder.getPrivateQueryContext());
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
        String jpql = generateQL();
        IQuery query = queryBuilder.createQuery(jpql);
        addResultMode();

        return query.getResultList();
    }

    /**
     * Returns the total number of results that would be returned using the
     * given <code>ISearch</code> if there were no paging or maxResult limits.
     * Uses the specified searchClass, ignoring the searchClass specified on the
     * search itself.
     * 
     * @see ISearchQuery
     */
    public int count() {

        String jpql = generateRowCountQL();
        if (jpql == null) { // special case where the query uses column
                            // operators
            return 1;
        }
        IQuery query = queryBuilder.createQuery(jpql);

        return ((Number) query.getSingleResult()).intValue();
    }
    private void addResultMode() {
        int resultMode = ISearchQuery.RESULT_MAP;

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
            Iterator<SelectField> fieldItr = queryContext.getSelectFields().iterator();
            while (fieldItr.hasNext()) {
                SelectField field = fieldItr.next();
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

}
