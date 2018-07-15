package skyglass.query.model.criteria;

import java.util.function.Supplier;

public interface ITypeResolver {
	
    public Supplier<Object> objectConverter(Class<?> rootClass, String property, Object value, boolean isCollection);

    public Supplier<Boolean> numericFieldResolver(Class<?> rootClass, String propertyName);
    
    public boolean isCollection(Class<?> rootClass, String path);
    
    public boolean isEntity(Class<?> rootClass, String path);
    
    public boolean isId(Class<?> rootClass, String path);

}
