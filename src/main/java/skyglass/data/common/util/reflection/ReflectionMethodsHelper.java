package skyglass.data.common.util.reflection;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

public class ReflectionMethodsHelper {

    public static void mergeProperties(Object source, Object target) {
        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        if (source.getClass() != target.getClass()) {
            return;
        }

        PropertyDescriptor[] targetPds = BeanUtils.getPropertyDescriptors(target.getClass());

        for (int i = 0; i < targetPds.length; ++i) {
            PropertyDescriptor targetPd = targetPds[i];
            if ((targetPd.getWriteMethod() == null))
                continue;
            PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(source.getClass(), targetPd.getName());
            if ((sourcePd == null) || (sourcePd.getReadMethod() == null))
                continue;
            try {
                Method sourceReadMethod = sourcePd.getReadMethod();
                if (!(Modifier.isPublic(sourceReadMethod.getDeclaringClass().getModifiers()))) {
                    sourceReadMethod.setAccessible(true);
                }
                Method targetReadMethod = targetPd.getReadMethod();
                if (!(Modifier.isPublic(targetReadMethod.getDeclaringClass().getModifiers()))) {
                    targetReadMethod.setAccessible(true);
                }
                Object sourceValue = sourceReadMethod.invoke(source, new Object[0]);
                Object targetValue = targetReadMethod.invoke(target, new Object[0]);
                boolean isComplexType = isComplexType(sourceValue);
                if (sourceValue != null && (targetValue == null || isIterableNotEmptyObject(sourceValue)
                        || ClassUtils.isPrimitiveOrWrapper(sourceValue.getClass()) || !isComplexType)) {
                    Method writeMethod = targetPd.getWriteMethod();
                    if (!(Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers()))) {
                        writeMethod.setAccessible(true);
                    }
                    writeMethod.invoke(target, new Object[] { sourceValue });
                } else if (sourceValue != null && isComplexType) {
                    mergeProperties(sourceValue, targetValue);
                }
            }
            catch (Throwable ex) {
                throw new RuntimeException("Could not copy properties from source to target", ex);
            }
        }
    }

    private static boolean isComplexType(Object object) {
        return !isSimpleType(object);
    }

    private static boolean isSimpleType(Object object) {
        if (object == null) {
            return true;
        }
        boolean result = true;
        PropertyDescriptor[] objectPds = BeanUtils.getPropertyDescriptors(object.getClass());

        for (int i = 0; i < objectPds.length; ++i) {
            PropertyDescriptor objectPd = objectPds[i];
            if ((objectPd.getWriteMethod() != null)) {
                result = false;
            }
        }
        return result;
    }

    private static boolean isIterableNotEmptyObject(Object object) {
        return isIterableObject(object) && getIterableSize(object) > 0;
    }

    private static boolean isIterableObject(Object object) {
        if (object instanceof Collection) {
            return true;
        }
        if (object instanceof Map) {
            return true;
        }
        if (object.getClass().isArray()) {
            return true;
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private static int getIterableSize(Object object) {
        if (object instanceof Collection) {
            return ((Collection) object).size();
        }
        if (object instanceof Map) {
            return ((Map) object).keySet().size();
        }
        Class<?> objectClass = object.getClass();
        if (objectClass.isArray()) {
            if (objectClass == byte[].class)
                return ((byte[]) object).length;
            else if (objectClass == short[].class)
                return ((short[]) object).length;
            else if (objectClass == int[].class)
                return ((int[]) object).length;
            else if (objectClass == long[].class)
                return ((long[]) object).length;
            else if (objectClass == char[].class)
                return ((char[]) object).length;
            else if (objectClass == float[].class)
                return ((float[]) object).length;
            else if (objectClass == double[].class)
                return ((double[]) object).length;
            else if (objectClass == boolean[].class)
                return ((boolean[]) object).length;
            else {
                return ((Object[]) object).length;
            }
        }
        return -1;
    }

    public static Object getFieldValue(Object obj, String name, boolean silent) {
        Object result = null;
        if (obj != null && !StringUtils.isBlank(name)) {
            try {
                Field field = findField(name, obj.getClass());
                if (field != null) {
                    boolean isAccessible = field.isAccessible();
                    field.setAccessible(true);
                    result = field.get(obj);
                    field.setAccessible(isAccessible);
                }
            }
            catch (IllegalAccessException e) {
                if (!silent) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static Field findField(String fieldName, Class<?> clazz) {
        Field field = null;
        if (fieldName != null && !fieldName.isEmpty()) {
            try {
                field = clazz.getDeclaredField(fieldName);
            }
            catch (NoSuchFieldException e) {
                if (clazz.getSuperclass() != null) {
                    field = findField(fieldName, clazz.getSuperclass());
                }
            }
        }
        return field;
    }

    public static Object getFieldValue(Object obj, String name) {
        return getFieldValue(obj, name, true);
    }

}