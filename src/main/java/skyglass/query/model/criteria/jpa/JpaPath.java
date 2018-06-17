package skyglass.query.model.criteria.jpa;

import javax.persistence.criteria.Path;

import skyglass.query.model.criteria.IExpression;
import skyglass.query.model.criteria.IPath;
import skyglass.query.model.criteria.IPredicate;

public class JpaPath<T> implements IPath<T> {

    private Path<T> path;

    public JpaPath(Path<T> path) {
        this.path = path;
    }

    public Path<T> getPath() {
        return path;
    }

    @Override
    public IPredicate in(IExpression<?>... expressions) {
        return new JpaPredicate(path.in(JpaExpression.convert(expressions)));
    }

}
