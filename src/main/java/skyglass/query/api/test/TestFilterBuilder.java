package skyglass.query.api.test;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.FilterRequest;
import skyglass.data.filter.request.IFilterRequest;

public class TestFilterBuilder {
	
    public static TestJpaQueryFilter testDataFilter() {
        TestJpaQueryFilter dataFilter = new TestJpaQueryFilter(JunctionType.AND, null);
        return dataFilter;
    }
	
    public static TestJpaQueryFilter testDataFilter(IFilterRequest request, JunctionType junctionType) {
        TestJpaQueryFilter dataFilter = new TestJpaQueryFilter(junctionType, request);
        FilterRequest.initDataFilter(dataFilter, request);
        return dataFilter;
    }

    public static TestJpaQueryFilter testDataFilter(IFilterRequest request) {
        return testDataFilter(request, JunctionType.AND);
    }

}
