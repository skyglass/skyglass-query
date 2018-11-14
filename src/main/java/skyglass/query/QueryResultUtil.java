package skyglass.query;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;

public class QueryResultUtil {


	public static Object getSingleResult(Query query)
	    throws IllegalStateException, UnsupportedOperationException {
		if (query != null) {
			@SuppressWarnings("rawtypes")
			List results = getListResult(query);
			if (!results.isEmpty()) {
				return results.iterator().next();
			}
		}

		return null;
	}

	public static <R extends Object> R getSingleResult(TypedQuery<R> typedQuery)
	    throws IllegalStateException, UnsupportedOperationException {
		if (typedQuery != null) {
			List<R> results = getListResult(typedQuery);
			if (!results.isEmpty()) {
				return results.iterator().next();
			}
		}

		return null;
	}


	public static <R extends Object> List<R> getListResult(TypedQuery<R> typedQuery)
	    throws IllegalStateException, UnsupportedOperationException {
		if (typedQuery != null) {
			try {
				List<R> results = typedQuery.getResultList();
				if (results != null) {
					return results;
				}
			} catch (IllegalStateException ex) {
				throw ex;
			} catch (TransactionRequiredException ex) {
				throw new IllegalStateException(ex);
			} catch (PersistenceException ex) {
				throw new UnsupportedOperationException(ex);
			} catch (Exception ex) {
				throw new RuntimeException("Could not get results from query '" + typedQuery, ex);
			}
		}

		return new ArrayList<>();
	}

	@SuppressWarnings("rawtypes")
	public static List getListResult(Query query) throws IllegalStateException, UnsupportedOperationException {
		if (query != null) {
			try {
				List results = query.getResultList();
				if (results != null) {
					return results;
				}
			} catch (IllegalStateException ex) {
				throw ex;
			} catch (TransactionRequiredException ex) {
				throw new IllegalStateException(ex);
			} catch (PersistenceException ex) {
				throw new UnsupportedOperationException(ex);
			} catch (Exception ex) {
				throw new RuntimeException("Could not get results from query '" + query, ex);
			}
		}

		return new ArrayList();
	}

}
