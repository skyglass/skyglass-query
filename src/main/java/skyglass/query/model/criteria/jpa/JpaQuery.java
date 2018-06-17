package skyglass.query.model.criteria.jpa;

import java.util.List;

import javax.persistence.Query;

import skyglass.query.model.query.IQuery;

public class JpaQuery implements IQuery {

    private Query query;

    public JpaQuery(Query query) {
        this.query = query;
    }

    @Override
    public void setParameter(String name, Object value) {
        query.setParameter(name, value);
    }

    @Override
    public Object getSingleResult() {
        return query.getSingleResult();
    }

    @Override
    public void setFirstResult(int startPosition) {
        query.setFirstResult(startPosition);
    }

    @Override
    public void setMaxResults(int maxResult) {
        query.setMaxResults(maxResult);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List getResultList() {
        return query.getResultList();
    }

    @Override
    public String getStringResult() {
        return query.toString();
    }

}
