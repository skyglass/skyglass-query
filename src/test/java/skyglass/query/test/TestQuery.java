package skyglass.query.test;

import java.util.List;

import skyglass.query.model.query.IQuery;

public class TestQuery implements IQuery {

    private String queryString;

    public TestQuery(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getStringResult() {
        return queryString;
    }

    @Override
    public void setParameter(String name, Object value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Object getSingleResult() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFirstResult(int startPosition) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setMaxResults(int maxResult) {
        // TODO Auto-generated method stub

    }

    @Override
    public List getResultList() {
        // TODO Auto-generated method stub
        return null;
    }

}
