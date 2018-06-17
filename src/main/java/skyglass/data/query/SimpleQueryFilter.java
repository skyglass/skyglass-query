package skyglass.data.query;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import skyglass.data.common.util.Check;

public class SimpleQueryFilter implements QueryFilter, Serializable {

    //**********************************************************************************************
    // CLASS
    //**********************************************************************************************
    final static private long serialVersionUID = 1L;

    //**********************************************************************************************
    // INSTANCE
    //**********************************************************************************************
    final private String filterField;
    final private String filterType;
    final private String filterValue;
    final private int pageNumber;
    final private int rowsPerPage;

    //----------------------------------------------------------------------------------------------
    public SimpleQueryFilter(
        String filterField,
        String filterType,
        String filterValue,
        int pageNumber,
        int rowsPerPage)
    {
        this.filterField = (filterField == null) ? null : filterField.trim();
        this.filterType = (filterType == null) ? null : filterType.trim();
        this.filterValue = (filterValue == null) ? null : filterValue.trim();
        this.pageNumber = pageNumber;
        this.rowsPerPage = rowsPerPage;
        validate();
    }

    //----------------------------------------------------------------------------------------------
    public boolean isDescending() {
        return false;
    }

    //----------------------------------------------------------------------------------------------
    public int getRowsPerPage() {
        return rowsPerPage;
    }

    //----------------------------------------------------------------------------------------------
    public int getPageNumber() {
        return pageNumber;
    }

    //----------------------------------------------------------------------------------------------
    public String getOrderField() {
        return filterField;
    }

    //----------------------------------------------------------------------------------------------
    public List<String> getFilterValues(String name) {
        return Collections.singletonList(filterValue);
    }

    //----------------------------------------------------------------------------------------------
    public String getFilterType(String name) {
        return filterType;
    }

    //----------------------------------------------------------------------------------------------
    public Set<String> getFilterFieldNames() {
        return Collections.singleton(filterField);
    }

    //----------------------------------------------------------------------------------------------
    private void validate() {
        Check.nonNull(filterField, "filterField");
        Check.nonNull(filterType, "filterType");
        Check.nonNull(filterValue, "filterValue");
        if (pageNumber <= 0) {
            throw new IllegalArgumentException("Invalid page number.");
        }
        if (rowsPerPage <= 0) {
            throw new IllegalArgumentException("Invalid rows per page.");
        }
    }

    //----------------------------------------------------------------------------------------------
    private void readObject(java.io.ObjectInputStream in)
    throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        validate();
    }
}
