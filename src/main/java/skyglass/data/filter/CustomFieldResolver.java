package skyglass.data.filter;

import skyglass.query.model.criteria.IPredicate;

public interface CustomFieldResolver {

    public IPredicate getPredicate();

}
