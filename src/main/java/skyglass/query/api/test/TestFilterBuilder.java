package skyglass.query.api.test;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.FilterRequest;
import skyglass.data.filter.request.IFilterRequest;

public class TestFilterBuilder {
	
    public TestDataFilter testDataFilter(IFilterRequest request, JunctionType junctionType) {
        TestDataFilter dataFilter = new TestDataFilter(junctionType, request);
        FilterRequest.initDataFilter(dataFilter, request);
        return dataFilter;
    }

    public TestDataFilter testDataFilter(IFilterRequest request) {
        return testDataFilter(request, JunctionType.AND);
    }

}
