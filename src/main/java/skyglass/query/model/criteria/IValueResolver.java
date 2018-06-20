package skyglass.query.model.criteria;

import java.util.function.Supplier;

public interface IValueResolver {
	
    public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection);

    public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName);

}
