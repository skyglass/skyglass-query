package skyglass.query.model.criteria;

import java.util.List;

public interface ITypedQuery<T> {

    public void setFirstResult(int offset);

    public void setMaxResults(int maxResults);

    public List<T> getResultList();

    public T getSingleResult();

}
