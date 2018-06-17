package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Predicate;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IPredicate;

public class JpaPredicate implements IPredicate {

    private Predicate predicate;

    public JpaPredicate(Predicate predicate) {
        this.predicate = predicate;
    }

    public Predicate getPredicate() {
        return predicate;
    }

    @Override
    public IPredicate in(IExpression<?>... expressions) {
        return new JpaPredicate(predicate.in(JpaExpression.convert(expressions)));
    }

}
