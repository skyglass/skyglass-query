package skyglass.query.model.criteria.jpa;

import java.util.List;

import javax.persistence.TypedQuery;

import skyglass.query.model.criteria.ITypedQuery;

public class JpaTypedQuery<T> implements ITypedQuery<T> {

    private TypedQuery<T> typedQuery;

    public JpaTypedQuery(TypedQuery<T> typedQuery) {
        this.typedQuery = typedQuery;
    }

    @Override
    public void setFirstResult(int offset) {
        typedQuery.setFirstResult(offset);
    }

    @Override
    public void setMaxResults(int maxResults) {
        typedQuery.setMaxResults(maxResults);
    }

    @Override
    public List<T> getResultList() {
        return typedQuery.getResultList();
    }

    @Override
    public T getSingleResult() {
        return typedQuery.getSingleResult();
    }

}
