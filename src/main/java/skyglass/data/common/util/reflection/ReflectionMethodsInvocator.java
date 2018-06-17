package skyglass.data.common.util.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ReflectionMethodsInvocator {

    Map<String, Method> invocationMap = new HashMap<String, Method>();
    Map<String, Field> fieldMap = new HashMap<String, Field>();
    @SuppressWarnings("rawtypes")
    private static Map<Class, Class> primitivesToWrappers = new HashMap<Class, Class>();

    static {
        primitivesToWrappers.put(Byte.class, byte.class);
        primitivesToWrappers.put(Short.class, short.class);
        primitivesToWrappers.put(Integer.class, int.class);
        primitivesToWrappers.put(Long.class, long.class);
        primitivesToWrappers.put(Float.class, float.class);
        primitivesToWrappers.put(Double.class, double.class);
        primitivesToWrappers.put(Boolean.class, boolean.class);
        primitivesToWrappers.put(Character.class, char.class);
    }

    Object wrappedObject;

    @SuppressWarnings("rawtypes")
    Class wrappedClass;

    IMethodResolutionStrategy strategy;

    public ReflectionMethodsInvocator(Object wrappedObject) {
        this(wrappedObject, false);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public ReflectionMethodsInvocator(Object wrappedObject, boolean useUniversalNameStrategy) {
        this.wrappedObject = wrappedObject;
        this.wrappedClass = wrappedObject != null ? wrappedObject.getClass() : null;
        strategy = useUniversalNameStrategy ? new UniversalNameStrategy() : new UniqueNameStrategy();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Method findMethod(Method[] methods, Class[] params, String methodName) {

        Class[] methodParams;
        boolean paramsEquals;

        for (Method tempMethod : methods) {
            methodParams = tempMethod.getParameterTypes();
            paramsEquals = true;
            if (tempMethod.getName().equals(methodName) && methodParams.length == params.length) {
                for (int i = 0; params.length > i; i++) {
                    if (!methodParams[i].equals(params[i]) && !methodParams[i].isAssignableFrom(params[i])
                            && !(primitivesToWrappers.containsKey(params[i])
                                    && primitivesToWrappers.get(params[i]).equals(methodParams[i]))) {
                        paramsEquals = false;
                    }
                }
                if (paramsEquals) {
                    return tempMethod;
                }
            }
        }

        return null;
    }

    // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings({ "rawtypes", "unchecked", "unused" })
    protected Method findMethod(String methodName, Object... args) {
        if (methodName != null && !methodName.isEmpty()) {
            String key = strategy.evaluateKey(methodName, args);
            Method method = invocationMap.get(key);
            if (method == null) {
                Class[] classes = new Class[args.length];
                for (int i = 0; i < args.length; ++i) {
                    classes[i] = args[i].getClass();
                }
                try {
                    method = wrappedClass.getDeclaredMethod(methodName, classes);
                    invocationMap.put(key, method);
                }
                catch (NoSuchMethodException e) {
                    boolean paramSubclasses = false;
                    Method[] classMethods = wrappedClass.getDeclaredMethods();
                    method = findMethod(classMethods, classes, methodName);
                }
                if (method != null && !method.isAccessible()) {
                    method.setAccessible(true);
                }
            }
            return method;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected Field findField(String fieldName) {
        if (fieldName != null && !fieldName.isEmpty()) {
            Field field = fieldMap.get(fieldName);
            if (field == null) {
                try {
                    field = wrappedClass.getDeclaredField(fieldName);
                    fieldMap.put(fieldName, field);
                }
                catch (NoSuchFieldException e) {

                }
                if (field != null && !field.isAccessible()) {
                    field.setAccessible(true);
                }
            }
            return field;
        }
        return null;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Object invokeMethodObject(String methodName, Object... args) throws InvocationTargetException {
        Method method = findMethod(methodName, args);
        Object result = null;
        if (method != null) {
            try {
                result = method.invoke(wrappedObject, args);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean invokeMethodBoolean(String methodName, Object... args) throws InvocationTargetException {
        Object result = invokeMethodObject(methodName, args);
        return result != null && (Boolean) result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String invokeMethodString(String methodName, Object... args) throws InvocationTargetException {
        return (String) invokeMethodObject(methodName, args);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Object getFieldValue(String fieldName) {
        Field field = findField(fieldName);
        Object result = null;
        if (field != null) {
            try {
                result = field.get(wrappedObject);
            }
            catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected interface IMethodResolutionStrategy {
        public String evaluateKey(String methodName, Object... args);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected class UniqueNameStrategy implements IMethodResolutionStrategy {
        public String evaluateKey(String methodName, Object... args) {
            return methodName;
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected class UniversalNameStrategy implements IMethodResolutionStrategy {
        private final String openingBrace = "(";
        private final String keySeparator = ",";
        private final String closingBrace = ")";

        public String evaluateKey(String methodName, Object... args) {
            StringBuilder key = new StringBuilder(methodName);
            key.append(openingBrace);
            for (Object obj : args) {
                key.append(obj.getClass().getCanonicalName()).append(keySeparator);
            }
            key.delete(key.length() - keySeparator.length(), key.length());
            key.append(closingBrace);
            return key.toString();
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}
