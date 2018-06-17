package skyglass.data.filter;

import java.util.function.Supplier;

import skyglass.query.model.criteria.IQueryBuilder;

public class DataFilterItem<T> extends PrivateFilterItem {

    protected IQueryBuilder<T, T> queryBuilder;

    @Override
    protected Supplier<Object> objectConverter(String fieldName, Object value) {
        return queryBuilder.objectConverter(getRootClass(), fieldName, value, false);
    }

    public DataFilterItem(IQueryBuilder<T, T> queryBuilder, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue) {
        this(queryBuilder, rootClass, fieldResolver, filterValue, FilterType.EQ, FilterClass.STRING);
    }

    public DataFilterItem(IQueryBuilder<T, T> queryBuilder, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue, FilterType filterType) {
        this(queryBuilder, rootClass, fieldResolver, filterValue, filterType, FilterClass.STRING);
    }

    public DataFilterItem(IQueryBuilder<T, T> queryBuilder, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue, FilterClass filterClass) {
        this(queryBuilder, rootClass, fieldResolver, filterValue, null, filterClass);
    }

    public DataFilterItem(IQueryBuilder<T, T> queryBuilder, Class<?> rootClass, FieldResolver fieldResolver,
            Object filterValue, FilterType filterType, FilterClass filterClass) {
        super(rootClass, fieldResolver, filterValue, filterType, filterClass);
        this.queryBuilder = queryBuilder;
    }

}
