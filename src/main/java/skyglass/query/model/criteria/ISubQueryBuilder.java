package skyglass.query.model.criteria;

public interface ISubQueryBuilder<E, S> extends ICriteriaQueryBuilder<E, S> {

    public ISubquery<S> getSubQuery();

}
