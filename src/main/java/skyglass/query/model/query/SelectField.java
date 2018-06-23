package skyglass.query.model.query;

import java.io.Serializable;

import skyglass.data.filter.SelectType;

/**
 * Used to specify field selection in <code>Search</code>.
 * 
 * @see SearchQuery
 */
public class SelectField implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Property string representing the root entity of the search. This is just
     * the empty string ("").
     */
    public static final String ROOT_ENTITY = "";

    /**
     * The property to include in the result.
     */
    protected String property;

    /**
     * The key to use for the property when using result mode
     * <code>RESULT_MAP</code>.
     */
    protected String key;

    /**
     * The operator to apply to the column: for example
     * <code>OP_COUNT, OP_SUM, OP_MAX</code>. The default is
     * <code>OP_PROPERTY</code>.
     */
    protected SelectType operator = SelectType.Property;



    public SelectField() {
    }

    public SelectField(String property) {
        this.property = property;
    }

    public SelectField(String property, String key) {
        this.property = property;
        this.key = key;
    }

    public SelectField(String property, SelectType operator) {
        this.property = property;
        this.operator = operator;
    }

    public SelectField(String property, SelectType operator, String key) {
        this.property = property;
        this.operator = operator;
        this.key = key;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public SelectType getOperator() {
        return operator;
    }

    public void setOperator(SelectType operator) {
        this.operator = operator;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + operator.getValue();
        result = prime * result + ((property == null) ? 0 : property.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        SelectField other = (SelectField) obj;
        if (key == null) {
            if (other.key != null)
                return false;
        } else if (!key.equals(other.key))
            return false;
        if (operator != other.operator)
            return false;
        if (property == null) {
            if (other.property != null)
                return false;
        } else if (!property.equals(other.property))
            return false;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        boolean parens = true;
        switch (operator) {
        case Avg:
            sb.append("AVG(");
            break;
        case Count:
            sb.append("COUNT(");
            break;
        case CountDistinct:
            sb.append("COUNT_DISTINCT(");
            break;
        case Max:
            sb.append("MAX(");
            break;
        case Min:
            sb.append("MIN(");
            break;
        case Property:
            parens = false;
            break;
        case Sum:
            sb.append("SUM(");
            break;
        default:
            sb.append("**INVALID OPERATOR: (" + operator + ")** ");
            parens = false;
            break;
        }

        if (property == null) {
            sb.append("null");
        } else {
            sb.append("`");
            sb.append(property);
            sb.append("`");
        }
        if (parens)
            sb.append(")");

        if (key != null) {
            sb.append(" as `");
            sb.append(key);
            sb.append("`");
        }

        return sb.toString();
    }
}
