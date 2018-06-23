package skyglass.data.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PrivateFieldResolverContext {
	
    /**
     * Regex pattern for a valid property name/path.
     */
    private static Pattern INJECTION_CHECK = Pattern.compile("^[\\w\\.]*$");
	
    private Map<String, FieldResolver> fieldResolverMap = new HashMap<String, FieldResolver>();
    
    public FieldResolver addFieldResolver(PrivateQueryContext queryContext, String fieldName) {
    	return addFieldResolver(queryContext, fieldName, FieldType.Path);
    }
    
    public FieldResolver addFieldResolver(PrivateQueryContext queryContext, 
    		String fieldName, FieldType fieldType) {
        if (!INJECTION_CHECK.matcher(fieldName).matches())
            throw new IllegalArgumentException(
                    "A property used in a Query may only contain word characters (alphabetic, numberic and underscore \"_\") and dot \".\" separators. This constraint was violated: "
                            + fieldName);
        FieldResolver fieldResolver = fieldResolverMap.get(fieldName);
        if (fieldResolver == null) {
            fieldResolver = new FieldResolver(queryContext, fieldName, fieldType);
            fieldResolverMap.put(fieldName, fieldResolver);
        }
        if (fieldResolver.getFieldType() == null && fieldType != null) {
            fieldResolver.setFieldType(fieldType);
        }
        return fieldResolver;
    }
    
    public FieldResolver addCustomFieldResolver(PrivateQueryContext queryContext, 
    		String fieldName, CustomFieldResolver customFieldResolver) {
        FieldResolver fieldResolver = addFieldResolver(queryContext, fieldName, FieldType.Criteria);
        fieldResolver.setCustomFieldResolver(customFieldResolver);
        return fieldResolver;
    }

    public FieldResolver addFieldResolver(PrivateQueryContext queryContext, 
    		String fieldName, FieldType fieldType, String expression) {
        FieldResolver fieldResolver = addFieldResolver(queryContext, fieldName, fieldType);
        fieldResolver.addResolvers(expression);
        return fieldResolver;
    }

    public FieldResolver addFieldResolvers(PrivateQueryContext queryContext, 
    		String fieldName, FieldType fieldType, String... expressions) {
        FieldResolver fieldResolver = addFieldResolver(queryContext, fieldName, fieldType);
        fieldResolver.addResolvers(expressions);
        return fieldResolver;
    }

}
