package skyglass.query.test;

import org.junit.Assert;
import org.junit.Test;

import skyglass.data.filter.OrderType;
import skyglass.query.api.test.TestFilterBuilder;

public class QueryStringBuilderTest {

    @Test
    public void defaultQueryTest() {
    	String result = TestFilterBuilder.testDataFilter().generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root", result);
    	
    	result = TestFilterBuilder.testDataFilter().generateCountQueryString();
    	Assert.assertEquals("select count(*) from skyglass.query.api.test.TestRootClazz _root", result);    	
    }
    
    @Test
    public void defaultDistinctQueryTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.setDistinct(true)
    			.generateQueryString();
    	Assert.assertEquals("select distinct _root from skyglass.query.api.test.TestRootClazz _root", result);
    	
    	result = TestFilterBuilder.testDataFilter()
    			.setDistinct(true)
    			.generateCountQueryString();
    	Assert.assertEquals("select count(distinct _root.id) from skyglass.query.api.test.TestRootClazz _root", result);    	
    }
    
    @Test
    public void simpleFilterTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFilter("test", "test")
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root where (_root.test = :p1)", result);
    	
    	result = TestFilterBuilder.testDataFilter()
    			.addFilter("test", "test")
    			.generateCountQueryString();
    	Assert.assertEquals("select count(*) from skyglass.query.api.test.TestRootClazz _root where (_root.test = :p1)", result);    	
    }
    
    @Test
    public void simpleNullFilterTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFilter("test", null)
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root", result);
    }
    
    @Test
    public void simpleEmptyFilterTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFilter("test", " ")
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root", result);
    }
    
    @Test
    public void multipleFilterTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFilters("test", "test1", "test2")
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root "
    			+ "where ((_root.test = :p1 OR _root.test = :p2))", result);
    	
    	result = TestFilterBuilder.testDataFilter()
    			.addFilters("test", "test1", "test2")
    			.generateCountQueryString();
    	Assert.assertEquals("select count(*) from skyglass.query.api.test.TestRootClazz _root "
    			+ "where ((_root.test = :p1 OR _root.test = :p2))", result);    	
    }
    
    @Test
    public void multipleFieldResolverTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFieldResolvers("test", "test1", "test2")
    			.addFilter("test", "test1")
    			.addOrder("test", OrderType.DESC)
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root "
    			+ "where ((_root.test1 = :p1) OR (_root.test2 = :p2)) "
    			+ "order by concat(coalesce(lower(test1)), coalesce(lower(test2))) desc", result);
    	
    	result = TestFilterBuilder.testDataFilter()
    			.addFieldResolvers("test", "test1", "test2")
    			.addFilter("test", "test1")
    			.generateCountQueryString();
    	Assert.assertEquals("select count(*) from skyglass.query.api.test.TestRootClazz _root "
    			+ "where ((_root.test1 = :p1) OR (_root.test2 = :p2))", result);    	
    }    
    
    @Test
    public void innerJoinFilterTest() {
    	String result = TestFilterBuilder.testDataFilter()
    			.addFilter("test1.test2", "test1")
    			.generateQueryString();
    	Assert.assertEquals("select _root from skyglass.query.api.test.TestRootClazz _root "
    			+ "where ((_root.test1 = :p1) OR (_root.test2 = :p2)) "
    			+ "order by concat(coalesce(lower(test1)), coalesce(lower(test2))) desc", result);	
    }

}
