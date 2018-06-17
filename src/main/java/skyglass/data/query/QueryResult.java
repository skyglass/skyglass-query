package skyglass.data.query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class QueryResult<T> implements Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************

    static private final long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    private List<T> results;
    private long totalRecords = 0;

    //----------------------------------------------------------------------------------------------
    public void setResults(Collection<T> results) {
        this.results = new ArrayList<T>();

        for (T result : results) {
            this.results.add(result);
        }
    }

    //----------------------------------------------------------------------------------------------
    public void setResults(T[] results) {
        this.results = new ArrayList<T>();

        for (T result : results) {
            this.results.add(result);
        }
    }

    //----------------------------------------------------------------------------------------------
    public List<T> getResults() {
        return Collections.unmodifiableList(results);
    }

    //----------------------------------------------------------------------------------------------
    public void setTotalRecords(long totalRecords) {
        this.totalRecords = totalRecords;
    }

    //----------------------------------------------------------------------------------------------
    public long getTotalRecords() {
        return totalRecords;
    }
}
