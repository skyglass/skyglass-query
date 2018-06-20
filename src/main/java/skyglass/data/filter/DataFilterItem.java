package skyglass.data.filter;

import java.util.function.Supplier;

import skyglass.query.model.criteria.IValueResolver;

public class DataFilterItem extends PrivateFilterItem {

    protected IValueResolver valueResolver;

    @Override
    protected Supplier<Object> objectConverter(String fieldName, Object value) {
        return valueResolver.objectConverter(getRootClass(), fieldName, value, false);
    }

    public DataFilterItem(IValueResolver valueResolver, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue, FilterType filterType) {
        super(rootClass, fieldResolver, filterValue, filterType);
        this.valueResolver = valueResolver;
    }

}
