package skyglass.data.filter;

import java.util.function.Supplier;

import skyglass.query.model.criteria.ITypeResolver;

public class DataFilterItem extends PrivateFilterItem {

    protected ITypeResolver typeResolver;

    @Override
    protected Supplier<Object> objectConverter(String fieldName, Object value, boolean isCollection) {
        return typeResolver.objectConverter(getRootClass(), fieldName, value, isCollection);
    }

    public DataFilterItem(ITypeResolver typeResolver, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue, FilterType filterType) {
        super(rootClass, fieldResolver, filterValue, filterType);
        this.typeResolver = typeResolver;
    }

}
