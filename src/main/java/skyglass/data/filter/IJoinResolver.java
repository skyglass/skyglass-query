package skyglass.data.filter;

public interface IJoinResolver<F, T> {

    public F resolve();
    
    public F invert();
    
    public IJoinResolver<F, T> done();

    public IJoinResolver<F, T> eqProperty(String propertyName, String otherPropertyName);

    public IJoinResolver<F, T> equals(String propertyName, Object value);

    public IJoinResolver<F, T> notEquals(String propertyName, Object value);

    public IJoinResolver<F, T> and();

    public IJoinResolver<F, T> or();  

    public IJoinResolver<F, T> addJoin(String fieldName, String alias);

    public IJoinResolver<F, T> addLeftJoin(String fieldName, String alias);

}
