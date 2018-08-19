package skyglass.query.model.criteria;

import java.util.Date;
import java.util.function.Supplier;

import skyglass.data.filter.JunctionType;
import skyglass.data.filter.PrivateQueryContext;
import skyglass.query.metadata.MetadataHelper;

public interface IQueryBuilder<E> extends ITypeResolver {
	
	public IQueryProcessor getQueryProcessor();
	
	public String generateQueryString();
	
	public String generateCountQueryString();
	
    public <T> ITypedQuery<T> createQuery(Class<T> clazz);
	
    public ITypedQuery<E> createQuery(String queryString);
    
    public ITypedQuery<Long> createCountQuery();
	
    public PrivateQueryContext setPrivateQueryContext(
    		JunctionType junctionType, Class<E> rootClazz, IJoinType joinType);
    
    public IQueryProcessor setQueryProcessor(
    		MetadataHelper metadataHelper, PrivateQueryContext privateQueryContext);
    
    public PrivateQueryContext getPrivateQueryContext();
    
    public static boolean isMatchesSearchQuery(String result, String searchQuery) {
        return searchQuery != null && result.toLowerCase().matches(searchQuery.toLowerCase());
    }

    public static boolean areMatchSearchQuery(String searchQuery, String... results) {
        if (searchQuery == null) {
            return false;
        }
        for (String result : results) {
            if (result != null && result.toLowerCase().matches(searchQuery.toLowerCase())) {
                return true;
            }
        }
        return false;
    }    
    
    public static Supplier<Number> getNumberValue(Supplier<Object> filterValueResolver) {
        return () -> {
            Object filterValue = filterValueResolver;
            if (filterValue instanceof Number) {
                return (Number) filterValue;
            }
            if (filterValue instanceof Date) {
                return ((Date) filterValue).getTime();
            }
            throw new UnsupportedOperationException("Unsupported Number filter value: " + filterValue);
        };
    }
    
    public static String convertToRegexp(String filterString) {
        String result = filterString.replace("*", "\\*");
        result = result.replace("%", ".*");
        if (!result.endsWith(".*")) {
            result += ".*";
        }
        if (!result.startsWith(".*")) {
            result = ".*" + result;
        }

        return result;
    }

    public static String processFilterString(Object filterString) {
        String result = filterString.toString().replace("\\*", "*");
        result = result.replace('*', '%');
        if (!result.endsWith("%")) {
            result += "%";
        }
        if (!result.startsWith("%")) {
            result = "%" + result;
        }

        return result.toLowerCase();
    }

    public static String normalizeFieldName(String expression, boolean forceLast) {
        String[] values1 = expression.split("\\.");
        if (values1.length == 1) {
            return expression;
        }
        String propertyName = values1[values1.length - 1];
        int aliasCount = values1.length - 1;
        // check if it's embedded id.
        // be sure that embedded id has name "id", otherwise this logic won't
        // work
        if (values1[values1.length - 2].equals("id")) {
            propertyName = "id:" + propertyName;
            aliasCount = values1.length - 2;
        }
        if (aliasCount == 0) {
            return propertyName;
        }
        String[] values = new String[aliasCount];
        for (int i = 0; i < aliasCount; i++) {
            values[i] = values1[i];
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append("_");
        }
        sb.deleteCharAt(sb.length() - 1);
        if (forceLast) {
            sb.append("_").append(propertyName);
        } else {
            sb.append(".").append(propertyName);
        }
        return sb.toString();
    }

    // embedded id in the result has the following format: id:propertyName
    // this method replaces ":" to "."
    public static String denormalizePropertyName(String expression) {
        String[] values = expression.split(":");
        if (values.length == 1) {
            return expression;
        }
        StringBuilder sb = new StringBuilder();
        for (String value : values) {
            sb.append(value);
            sb.append(".");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String resolvePropertyName(String expression) {
        String original = expression;
        expression = normalizeFieldName(expression, false);
        String[] values1 = expression.split("\\.");
        if (values1.length == 1) {
            return original;
        }
        String propertyName = denormalizePropertyName(values1[1]);
        return values1[0] + "." + propertyName;
    }

    public static boolean hasAlias(String property) {
        String[] values1 = property.split("\\.");
        if (values1.length == 1) {
            return false;
        }
        return true;
    }

}
