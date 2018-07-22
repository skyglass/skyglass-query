package skyglass.data.filter;

public interface IJoinResolver<T> {

    public IJoinResolver<T> resolve();

    public IJoinResolver<T> resolve(String resolveAlias);

    public IJoinResolver<T> invert();

    public IJoinResolver<T> invert(String invertAlias);

    public IJoinResolver<T> eqProperty(String propertyName, String otherPropertyName);

    public IJoinResolver<T> equals(String propertyName, Object value);

    public IJoinResolver<T> notEquals(String propertyName, Object value);

    public IJoinResolver<T> and();

    public IJoinResolver<T> or();

    public <SUB> SubQueryFilter<T, SUB> idExistsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> idNotExistsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertyExistsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias);

    public <SUB> SubQueryFilter<T, SUB> idInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> idNotInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyInSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotInSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB> propertiesEqualsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertiesNotEqualsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertiesInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertiesNotInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertiesExistSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB> propertiesNotExistSubQuery(Class<SUB> clazz);

    public IJoinResolver<T> done();    

    public IJoinResolver<T> addJoin(String alias);

    public IJoinResolver<T> addLeftJoin(String alias);
    
    public IJoinResolver<T> addSubQueryJoin(String alias);

    public IJoinResolver<T> addSubQueryLeftJoin(String alias);

}
