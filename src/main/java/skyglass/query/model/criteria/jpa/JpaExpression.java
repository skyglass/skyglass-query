package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Expression;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IPredicate;

public class JpaExpression<T> implements IExpression<T> {

    @SuppressWarnings("rawtypes")
    public static Expression[] convert(IExpression[] expressions) {
        Expression[] result = new Expression[expressions.length];
        for (int i = 0; i < expressions.length; i++) {
            result[i] = ((JpaExpression) expressions[i]).getExpression();
        }
        return result;
    }

    private Expression<T> expression;

    public JpaExpression(Expression<T> expression) {
        this.expression = expression;
    }

    public Expression<T> getExpression() {
        return expression;
    }

    @Override
    public IPredicate in(IExpression<?>... expressions) {
        return new JpaPredicate(expression.in(JpaExpression.convert(expressions)));
    }

}
