package skyglass.query.api.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.FilterRequest;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.JpaCriteriaFilter;
import skyglass.query.model.criteria.ICriteriaQueryBuilder;
import skyglass.query.model.criteria.jpa.JpaCriteriaBuilder;

public abstract class AbstractJpaFilterBuilder {
	
    @PersistenceContext(unitName = "platform")
    protected EntityManager entityManager;

    public <T> JpaCriteriaFilter<T> jpaCriteriaFilter(IFilterRequest request, Class<T> clazz, JunctionType junctionType) {
    	JpaCriteriaFilter<T> dataFilter = new JpaCriteriaFilter<T>(clazz, junctionType, createJpaCriteriaBuilder(clazz), request);
        FilterRequest.initDataFilter(dataFilter, request);
        return dataFilter;
    }

    public <T> JpaCriteriaFilter<T> jpaCriteriaFilter(IFilterRequest request, Class<T> clazz) {
        return jpaCriteriaFilter(request, clazz, JunctionType.AND);
    }
    
    private <T> ICriteriaQueryBuilder<T, T> createJpaCriteriaBuilder(Class<T> clazz) {
    	return new JpaCriteriaBuilder<T, T>(entityManager, clazz, clazz);
    }

}
