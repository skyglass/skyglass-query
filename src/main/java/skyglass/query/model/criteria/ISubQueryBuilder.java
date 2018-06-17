package skyglass.query.model.criteria;

public interface ISubQueryBuilder<E, S> extends IQueryBuilder<E, S> {

    public ISubquery<S> getSubQuery();

}
