package skyglass.data.filter;

public interface IJoinResolver<T, F> {

    public F resolve();

    public F resolve(String resolveAlias);

    public F invert();

    public F invert(String invertAlias);

    public IJoinResolver<T, F> eqProperty(String propertyName, String otherPropertyName);

    public IJoinResolver<T, F> equals(String propertyName, Object value);

    public IJoinResolver<T, F> notEquals(String propertyName, Object value);

    public IJoinResolver<T, F> and();

    public IJoinResolver<T, F> or();

    public <SUB> SubQueryFilter<T, SUB, F> idExistsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> idNotExistsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyExistsSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotExistsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias);

    public <SUB> SubQueryFilter<T, SUB, F> idInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> idNotInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyInSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyEqualsSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotEqualsSubQuery(Class<SUB> clazz, String parentAlias,
            String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyInSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertyNotInSubQuery(Class<SUB> clazz, String parentAlias, String alias);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesEqualsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotEqualsSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotInSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesExistSubQuery(Class<SUB> clazz);

    public <SUB> SubQueryFilter<T, SUB, F> propertiesNotExistSubQuery(Class<SUB> clazz);

    public IJoinResolver<T, F> done();

}
