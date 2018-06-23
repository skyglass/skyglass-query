package skyglass.query.api.jpa;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import skyglass.data.filter.IDataFilter;
import skyglass.data.filter.JunctionType;
import skyglass.data.filter.request.FilterRequest;
import skyglass.data.filter.request.IFilterRequest;
import skyglass.query.api.AbstractDataFilter;
import skyglass.query.api.JpaDataFilter;
import skyglass.query.model.criteria.IQueryBuilder;
import skyglass.query.model.criteria.jpa.JpaCriteriaBuilder;

public abstract class AbstractJpaFilterBuilder {
	
    @PersistenceContext(unitName = "platform")
    protected EntityManager entityManager;

    public <T> JpaDataFilter<T> jpaCriteriaFilter(IFilterRequest request, Class<T> clazz, JunctionType junctionType) {
    	JpaDataFilter<T> dataFilter = new JpaDataFilter<T>(clazz, junctionType, createJpaCriteriaBuilder(clazz), request);
        FilterRequest.initDataFilter(dataFilter, request);
        return dataFilter;
    }

    public <T> JpaDataFilter<T> jpaCriteriaFilter(IFilterRequest request, Class<T> clazz) {
        return jpaCriteriaFilter(request, clazz, JunctionType.AND);
    }
    
    private <T> IQueryBuilder<T, T> createJpaCriteriaBuilder(Class<T> clazz) {
    	return new JpaCriteriaBuilder<T, T>(entityManager, clazz, clazz);
    }

}
