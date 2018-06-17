package skyglass.query.model.query;

import java.util.List;

public interface IQuery {

    public String getStringResult();

    public void setParameter(String name, Object value);

    public Object getSingleResult();

    public void setFirstResult(int startPosition);

    public void setMaxResults(int maxResult);

    @SuppressWarnings("rawtypes")
    public List getResultList();

}
