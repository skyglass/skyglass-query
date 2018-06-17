package skyglass.data.filter.request;

import java.util.UUID;

public class FilterRequestUtils {
    public static Boolean getBooleanParamValue(IFilterRequest request, String paramName) {
        String result = getStringParamValue(request, paramName);
        if (result != null) {
            return Boolean.valueOf(result);
        }
        return null;
    }

    public static UUID getUUIDParamValue(IFilterRequest request, String paramName) {
        String result = getStringParamValue(request, paramName);
        if (result != null) {
            return UUID.fromString(result);
        }
        return null;
    }

    public static Integer getIntegerParamValue(IFilterRequest request, String paramName) {
        return getIntegerParamValue(request, paramName, null);
    }

    public static Integer getIntegerParamValue(IFilterRequest request, String paramName, Integer defValue) {
        String result = getStringParamValue(request, paramName);
        if (result != null) {
            try {
                return Integer.parseInt(result);
            }
            catch (NumberFormatException e) {
                return defValue;
            }
        }
        return defValue;
    }

    public static String getStringParamValue(IFilterRequest request, String paramName) {
        String[] paramValues = getStringParamValues(request, paramName);
        if (paramValues != null && paramValues.length > 0) {
            return paramValues[0].trim().equals("") ? null : paramValues[0];
        }
        return null;
    }

    public static String[] getStringParamValues(IFilterRequest request, String paramName) {
        return request.getParameterValues(paramName);
    }
}
