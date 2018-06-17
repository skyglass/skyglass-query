package skyglass.data.common.util;

import java.util.Collection;
import java.util.Map;

/**
 * This class provides a collection of methods that perform common argument
 * validity checks and throws the appropriate exception if necessary. Methods
 * that check a single value return the same value to facilitate invocation of
 * super contructors.
 */
final public class Check {

    //******************************************************************************************************************
    // CLASS
    //******************************************************************************************************************

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> as a key. Values may be <code>null</code>. It is best
     * to copy a mutable map before validating as it may change during or
     * after validation.
     *
     * @param parameter
     *        the map to validate.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains a <code>null</code>
     *         key.
     */
    static public <T extends Map<?, ?>> T allKeysNonNull(T parameter) {
        nonNull(parameter, "map");
        int i = 0;
        for (Map.Entry<?, ?> e : parameter.entrySet()) {
            nonNullInternal(e, "map entry %d", i);
            nonNullInternal(e.getKey(), "map entry key %d", i);
            i++;
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> as a key. Values may be <code>null</code>. It is best
     * to copy a mutable map before validating as it may change during or
     * after validation.
     *
     * @param parameter
     *        the map to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains a <code>null</code>
     *         key.
     */
    static public <T extends Map<?, ?>> T allKeysNonNull(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            int i = 0;
            for (Map.Entry<?, ?> e : parameter.entrySet()) {
                nonNullInternal(e, "map %s entry %d", parameterName, i);
                nonNullInternal(e.getKey(), "map %s entry key %d", parameterName, i);
                i++;
            }
        }
        else {
            allKeysNonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-<code>null</code> and does not
     * contain <code>null</code>. It is best to copy a mutable collection before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the collection to validate.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code> or contains
     *         <code>null</code>.
     */
    static public <T extends Collection<?>> T allNonNull(T parameter) {
        nonNull(parameter, "collection");
        int i = 0;
        for (Object e : parameter) {
            nonNullInternal(e, "collection element %d", i);
            i++;
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> as a key or value. It is best to copy a mutable map
     * before validating as it may change during or after validation.
     *
     * @param parameter
     *        the map to validate.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains <code>null</code>
     *         keys or values.
     */
    static public <T extends Map<?, ?>> T allNonNull(T parameter) {
        nonNull(parameter, "map");
        int i = 0;
        for (Map.Entry<?, ?> e : parameter.entrySet()) {
            nonNullInternal(e, "map entry %d", i);
            nonNullInternal(e.getKey(), "map entry key %d", i);
            nonNullInternal(e.getValue(), "map entry value %d", i);
            i++;
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-<code>null</code> and does not
     * contain <code>null</code>. It is best to copy a mutable collection before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the collection to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code> or contains
     *         <code>null</code>.
     */
    static public <T extends Collection<?>> T allNonNull(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            int i = 0;
            for (Object e : parameter) {
                nonNullInternal(e, "collection %s element %d", parameterName, i);
                i++;
            }
        }
        else {
            allNonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> keys or values. It is best to copy a mutable map before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the map to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains a <code>null</code>
     *         key or value.
     */
    static public <T extends Map<?, ?>> T allNonNull(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            int i = 0;
            for (Map.Entry<?, ?> e : parameter.entrySet()) {
                nonNullInternal(e, "map %s entry %d", parameterName, i);
                nonNullInternal(e.getKey(), "map %s entry key %d", parameterName, i);
                nonNullInternal(e.getValue(), "map %s entry value %d", parameterName, i);
                i++;
            }
        }
        else {
            allNonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-<code>null</code> and does not contain
     * <code>null</code>. It is best to copy an array before validating as it
     * may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code> or contains <code>null</code>.
     */
    static public <T> T[] allNonNull(T[] parameter) {
        nonNull(parameter, "array");
        for (int i = 0; i < parameter.length; i++) {
            nonNullInternal(parameter[i], "array element %d", i);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-<code>null</code> and does not contain
     * <code>null</code>. It is best to copy an array before validating as it
     * may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code> or contains <code>null</code>.
     */
    static public <T> T[] allNonNull(T[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            for (int i = 0; i < parameter.length; i++) {
                nonNullInternal(parameter[i], "array %s element %d", parameterName, i);
            }
        }
        else {
            allNonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-<code>null</code>, does not
     * contain <code>null</code>, and only contains elements of the right type.
     * It is best to copy a mutable collection before validating as it may
     * change during or after validation.
     *
     * @param parameter
     *        the collection to validate.
     * @param type
     *        the type to validate against. Non-<code>null</code>.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code> or contains
     *         <code>null</code> or <code>type</code> is <code>null</code>.
     * @throws ClassCastException
     *         if an element is the wrong type.
     */
    static public <T extends Collection<?>> T allType(T parameter, Class<?> type) {
        nonNull(parameter, "collection");
        nonNull(type, "type");
        int i = 0;
        for (Object e : parameter) {
            nonNullInternal(e, "collection element %d", i);
            typeInternal(e, type, "collection element %d is not a %s", i, type.getName());
            i++;
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-<code>null</code>, does not
     * contain <code>null</code>, and only contains elements of the right type.
     * It is best to copy a mutable collection before validating as it may
     * change during or after validation.
     *
     * @param parameter
     *        the collection to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @param type
     *        the type to validate against. Non-<code>null</code>.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code> or contains
     *         <code>null</code> or <code>type</code> is <code>null</code>.
     * @throws ClassCastException
     *         if an element is the wrong type.
     */
    static public <T extends Collection<?>> T allType(T parameter, String parameterName, Class<?> type) {
        nonNull(type, "type");
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            nonNull(type, "type");
            int i = 0;
            for (Object e : parameter) {
                nonNullInternal(e, "collection %s element %d", parameterName, i);
                typeInternal(e, type, "collection %s element %d is not a %s", parameterName, i, type.getName());
                i++;
            }
        }
        else {
            allType(parameter, type);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> as a value. A key may be <code>null</code>. It is best
     * to copy a mutable map before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the map to validate.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains <code>null</code>
     *         values.
     */
    static public <T extends Map<?, ?>> T allValuesNonNull(T parameter) {
        nonNull(parameter, "map");
        int i = 0;
        for (Map.Entry<?, ?> e : parameter.entrySet()) {
            nonNullInternal(e, "map entry %d", i);
            nonNullInternal(e.getValue(), "map entry value %d", i);
            i++;
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-<code>null</code> and does not contain
     * <code>null</code> as a value. A key may be <code>null</code>. It is best
     * to copy a mutable map before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the map to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code> or contains <code>null</code>
     *         values.
     */
    static public <T extends Map<?, ?>> T allValuesNonNull(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            int i = 0;
            for (Map.Entry<?, ?> e : parameter.entrySet()) {
                nonNullInternal(e, "map %s entry %d", parameterName, i);
                nonNullInternal(e.getValue(), "map %s entry value %d", parameterName, i);
                i++;
            }
        }
        else {
            allKeysNonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a container size is valid.
     *
     * @param parameter
     *        the container size to validate.
     * @return the container size.
     * @throws IllegalArgumentException
     *         if the size is less than zero.
     */
    static public int containerSize(int parameter) {
        return nonNegativeInternal(parameter, "container size %d < 0", parameter);
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a container size is valid.
     *
     * @param parameter
     *        the container size to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the container size.
     * @throws IllegalArgumentException
     *         if the size is less than zero.
     */
    static public int containerSize(int parameter, String parameterName) {
        if (parameterName != null) {
            nonNegativeInternal(parameter, "container size %s value %d < 0", parameterName, parameter);
        }
        else {
            containerSize(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index is valid for a container of the given size. The
     * container size is also validated as by {@link #containerSize(int)
     * containerSize()}.
     *
     * @param containerSize
     *        the container size.
     * @param parameter
     *        the index to validate.
     * @return the index.
     * @throws IndexOutOfBoundsException
     *         if the index is outside the container.
     */
    static public int index(int containerSize, int parameter) {
        containerSize(containerSize);
        if (parameter < 0) {
            throwIndexOutOfBounds("index %d < 0", parameter);
        }
        if (parameter >= containerSize) {
            throwIndexOutOfBounds("index %d >= container size %d", parameter, containerSize);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index is valid for a given index range of a container. The
     * container size is validated as by {@link #containerSize(int)
     * containerSize()} and the index range is validated as by
     * {@link #indexRange(int, int, int) indexRange()}.
     *
     * @param containerSize
     *        the container size.
     * @param offset
     *        the offset.
     * @param length
     *        the length.
     * @param parameter
     *        the index to validate.
     * @return the index.
     * @throws IndexOutOfBoundsException
     *         if the index is outside the index range.
     */
    static public int index(int containerSize, int offset, int length, int parameter) {
        containerSize(containerSize);
        indexRange(containerSize, offset, length);
        if (parameter < offset) {
            throwIndexOutOfBounds("index %d < offset %d", parameter, offset);
        }
        if (parameter >= offset + length) {
            throwIndexOutOfBounds("index %d >= offset %d + length %d", parameter, offset, length);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index is valid for a given index range of a container. The
     * container size is validated as by {@link #containerSize(int)
     * containerSize()} and the index range is validated as by
     * {@link #indexRange(int, int, int) indexRange()}.
     *
     * @param containerSize
     *        the container size.
     * @param offset
     *        the offset.
     * @param length
     *        the length.
     * @param parameter
     *        the index to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the index.
     * @throws IndexOutOfBoundsException
     *         if the index is outside the index range.
     */
    static public int index(int containerSize, int offset, int length, int parameter, String parameterName) {
        if (parameterName != null) {
            containerSize(containerSize);
            indexRange(containerSize, offset, length);
            if (parameter < offset) {
                throwIndexOutOfBounds("index %s value %d < offset %d", parameterName, parameter, offset);
            }
            if (parameter >= offset + length) {
                throwIndexOutOfBounds("index %s value %d >= offset %d + length %d",
                    parameterName, parameter, offset, length);
            }
        }
        else {
            index(containerSize, offset, length, parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index is valid for a container of the given size. The
     * container size is also validated as by {@link #containerSize(int)
     * containerSize()}.
     *
     * @param containerSize
     *        the container size.
     * @param parameter
     *        the index to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the index.
     * @throws IndexOutOfBoundsException
     *         if the index is outside the container.
     */
    static public int index(int containerSize, int parameter, String parameterName) {
        if (parameterName != null) {
            containerSize(containerSize);
            if (parameter < 0) {
                throwIllegalArgument("index %s value %d < 0", parameterName, parameter);
            }
            if (parameter >= containerSize) {
                throwIndexOutOfBounds("index %s value %d >= container size %d",
                    parameterName, parameter, containerSize);
            }
        }
        else {
            index(containerSize, parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index range is valid for a container of given size.
     *
     * @param containerSize
     *        the container size.
     * @throws IndexOutOfBoundsException
     *         if the index range is outside the container.
     */
    static public void indexRange(int containerSize, int offset, int length) {
        if (containerSize < 0) {
            throwIllegalArgument("container size < 0");
        }
        if (offset < 0) {
            throwIndexOutOfBounds("offset %d < 0", offset);
        }
        if (length < 0) {
            throwIndexOutOfBounds("length %d < 0", offset);
        }
        if (offset > containerSize || length > containerSize - offset) {
            throwIndexOutOfBounds("offset %d + length %d > container size %d", offset, length, containerSize);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an index range is valid for a container of given size.
     *
     * @param containerSize
     *        the container size.
     * @throws IndexOutOfBoundsException
     *         if the index range is outside the container.
     */
    static public void indexRange(long containerSize, long offset, long length) {
        if (containerSize < 0) {
            throwIllegalArgument("container size < 0");
        }
        if (offset < 0) {
            throwIndexOutOfBounds("offset %d < 0", offset);
        }
        if (length < 0) {
            throwIndexOutOfBounds("offset %d < 0", offset);
        }
        if (offset > containerSize || length > containerSize - offset) {
            throwIndexOutOfBounds("offset %d + length %d > container size %d", offset, length, containerSize);
        }
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is negative.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-negative.
     */
    static public int negative(int parameter) {
        if (parameter >= 0) {
            throwIllegalArgument("value %d >= 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is negative.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-negative.
     */
    static public int negative(int parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter >= 0) {
                throwIllegalArgument("%s value %d >= 0", parameterName, parameter);
            }
        }
        else {
            negative(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is negative.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-negative.
     */
    static public long negative(long parameter) {
        if (parameter >= 0) {
            throwIllegalArgument("value %d >= 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is negative.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-negative.
     */
    static public long negative(long parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter >= 0) {
                throwIllegalArgument("%s value %d >= 0", parameterName, parameter);
            }
        }
        else {
            negative(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link CharSequence} is non-blank. Non-blank means non-
     * <code>null</code>, non-empty, and not composed entirely of whitespace. It
     * is best to copy a mutable charsequence before validating as it may change
     * during or after validation.
     *
     * @param parameter
     *        the charsequence to validate.
     * @return the charsequence.
     * @throws NullPointerException
     *         if the charsequence is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the charsequence is empty or composed entirely of whitespace.
     */
    static public <T extends CharSequence> T nonBlank(T parameter) {
        nonNull(parameter, "charsequence");
        nonEmpty(parameter, "charsequence");
        if (parameter.toString().trim().length() == 0) {
            throwIllegalArgument("charsequence is blank");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link CharSequence} is non-blank. Non-blank means non-
     * <code>null</code>, non-empty, and not composed entirely of whitespace. It
     * is best to copy a mutable charsequence before validating as it may change
     * during or after validation.
     *
     * @param parameter
     *        the charsequence to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the charsequence.
     * @throws NullPointerException
     *         if the charsequence is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the charsequence is empty or composed entirely of whitespace.
     */
    static public <T extends CharSequence> T nonBlank(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            nonEmpty(parameter, parameterName);
            if (parameter.toString().trim().length() == 0) {
                throwIllegalArgument("charsequence %s is blank", parameterName);
            }
        }
        else {
            nonBlank(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public boolean[] nonEmpty(boolean[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public boolean[] nonEmpty(boolean[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public byte[] nonEmpty(byte[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public byte[] nonEmpty(byte[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public char[] nonEmpty(char[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public char[] nonEmpty(char[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public double[] nonEmpty(double[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public double[] nonEmpty(double[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public float[] nonEmpty(float[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public float[] nonEmpty(float[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public int[] nonEmpty(int[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public int[] nonEmpty(int[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public long[] nonEmpty(long[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public long[] nonEmpty(long[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public short[] nonEmpty(short[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public short[] nonEmpty(short[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link CharSequence} is non-empty. It is best to copy a
     * mutable charsequence before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the charsequence to validate.
     * @return the charsequence.
     * @throws NullPointerException
     *         if the charsequence is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the charsequence is empty.
     */
    static public <T extends CharSequence> T nonEmpty(T parameter) {
        nonNull(parameter, "charsequence");
        if (parameter.length() == 0) {
            throwIllegalArgument("charsequence is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-empty. It is best to copy a
     * mutable collection before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the collection to validate.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the collection is empty.
     */
    static public <T extends Collection<?>> T nonEmpty(T parameter) {
        nonNull(parameter, "collection");
        if (parameter.isEmpty()) {
            throwIllegalArgument("collection is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-empty. It is best to copy a mutable map
     * before validating as it may change during or after validation.
     *
     * @param parameter
     *        the map to validate.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the map is empty.
     */
    static public <T extends Map<?, ?>> T nonEmpty(T parameter) {
        nonNull(parameter, "map");
        if (parameter.isEmpty()) {
            throwIllegalArgument("map is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link CharSequence} is non-empty. It is best to copy a
     * mutable charsequence before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the charsequence to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the charsequence.
     * @throws NullPointerException
     *         if the charsequence is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the charsequence is empty.
     */
    static public <T extends CharSequence> T nonEmpty(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length() == 0) {
                throwIllegalArgument("charsequence %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Collection} is non-empty. It is best to copy a
     * mutable collection before validating as it may change during or after
     * validation.
     *
     * @param parameter
     *        the collection to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the collection.
     * @throws NullPointerException
     *         if the collection is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the collection is empty.
     */
    static public <T extends Collection<?>> T nonEmpty(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.isEmpty()) {
                throwIllegalArgument("collection %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that a {@link Map} is non-empty. It is best to copy a mutable map
     * before validating as it may change during or after validation.
     *
     * @param parameter
     *        the map to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the map.
     * @throws NullPointerException
     *         if the map is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the map is empty.
     */
    static public <T extends Map<?, ?>> T nonEmpty(T parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.isEmpty()) {
                throwIllegalArgument("map %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public <T> T[] nonEmpty(T[] parameter) {
        nonNull(parameter, "array");
        if (parameter.length == 0) {
            throwIllegalArgument("array is empty");
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that an array is non-empty. It is best to copy an array before
     * validating as it may change during or after validation.
     *
     * @param parameter
     *        the array to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the array.
     * @throws NullPointerException
     *         if the array is <code>null</code>.
     * @throws IllegalArgumentException
     *         if the array is empty.
     */
    static public <T> T[] nonEmpty(T[] parameter, String parameterName) {
        if (parameterName != null) {
            nonNull(parameter, parameterName);
            if (parameter.length == 0) {
                throwIllegalArgument("array %s is empty", parameterName);
            }
        }
        else {
            nonEmpty(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-negative.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is negative.
     */
    static public int nonNegative(int parameter) {
        if (parameter < 0) {
            throwIllegalArgument("value %d < 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-negative.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is negative.
     */
    static public int nonNegative(int parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter < 0) {
                throwIllegalArgument("%s value %d < 0", parameterName, parameter);
            }
        }
        else {
            nonNegative(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-negative.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is negative.
     */
    static public long nonNegative(long parameter) {
        if (parameter < 0) {
            throwIllegalArgument("value %d < 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-negative.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is negative.
     */
    static public long nonNegative(long parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter < 0) {
                throwIllegalArgument("%s value %d < 0", parameterName, parameter);
            }
        }
        else {
            nonNegative(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-<code>null</code>.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws NullPointerException
     *         if the value is <code>null</code>.
     */
    static public <T> T nonNull(T parameter) {
        parameter.getClass();
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-<code>null</code>.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws NullPointerException
     *         if the value is <code>null</code>.
     */
    static public <T> T nonNull(T parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter == null) {
                throw new NullPointerException(parameterName);
            }
        }
        else {
            nonNull(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-positive.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is positive.
     */
    static public int nonPositive(int parameter) {
        if (parameter > 0) {
            throwIllegalArgument("value %d > 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-positive.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is positive.
     */
    static public int nonPositive(int parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter > 0) {
                throwIllegalArgument("%s value %d > 0", parameterName, parameter);
            }
        }
        else {
            nonPositive(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-positive.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is positive.
     */
    static public long nonPositive(long parameter) {
        if (parameter > 0) {
            throwIllegalArgument("value %d > 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-positive.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is positive.
     */
    static public long nonPositive(long parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter > 0) {
                throwIllegalArgument("%s value %d > 0", parameterName, parameter);
            }
        }
        else {
            nonPositive(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-zero.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is zero.
     */
    static public int nonZero(int parameter) {
        if (parameter == 0) {
            throwIllegalArgument("value %d == 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-zero.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is zero.
     */
    static public int nonZero(int parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter == 0) {
                throwIllegalArgument("%s value %d == 0", parameterName, parameter);
            }
        }
        else {
            nonZero(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-zero.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is zero.
     */
    static public long nonZero(long parameter) {
        if (parameter == 0) {
            throwIllegalArgument("value %d == 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is non-zero.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is zero.
     */
    static public long nonZero(long parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter == 0) {
                throwIllegalArgument("%s value %d == 0", parameterName, parameter);
            }
        }
        else {
            nonZero(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is positive.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-positive.
     */
    static public int positive(int parameter) {
        if (parameter <= 0) {
            throwIllegalArgument("value %d <= 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is positive.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-positive.
     */
    static public int positive(int parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter <= 0) {
                throwIllegalArgument("%s value %d <= 0", parameterName, parameter);
            }
        }
        else {
            positive(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is positive.
     *
     * @param parameter
     *        the value to validate.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-positive.
     */
    static public long positive(long parameter) {
        if (parameter <= 0) {
            throwIllegalArgument("value %d <= 0", parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is positive.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @return the parameter value.
     * @throws IllegalArgumentException
     *         if the value is non-positive.
     */
    static public long positive(long parameter, String parameterName) {
        if (parameterName != null) {
            if (parameter <= 0) {
                throwIllegalArgument("%s value %d <= 0", parameterName, parameter);
            }
        }
        else {
            positive(parameter);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is the right type.
     *
     * @param parameter
     *        the value to validate.
     * @param type
     *        the type to validate against. Non-<code>null</code>.
     * @return the parameter value.
     * @throws NullPointerException
     *         if the value is <code>null</code> or the <code>type</code> is
     *         <code>null</code>.
     * @throws ClassCastException
     *         if the value is the wrong type.
     */
    static public <T> T type(T parameter, Class<?> type) {
        nonNull(type, "type");
        nonNull(parameter);
        return typeInternal(
            parameter, type,
            "value %d is not a %s",
            parameter,
            type.getName());
    }

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Checks that the value is the right type.
     *
     * @param parameter
     *        the value to validate.
     * @param parameterName
     *        the name of the validated parameter.
     * @param type
     *        the type to validate against. Non-<code>null</code>.
     * @return the parameter value.
     * @throws NullPointerException
     *         if the value is <code>null</code> or the <code>type</code> is
     *         <code>null</code>.
     * @throws ClassCastException
     *         if the value is the wrong type.
     */
    static public <T> T type(T parameter, String parameterName, Class<?> type) {
        nonNull(type, "type");
        nonNull(parameter);
        if (parameterName != null) {
            typeInternal(
                parameter, type,
                "%s value %d is not a %s",
                parameterName,
                parameter,
                type.getName());
        }
        else {
            type(parameter, type);
        }
        return parameter;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private int nonNegativeInternal(int value, String message, Object... messageArguments) {
        if (value < 0) {
            throwIllegalArgument(message, messageArguments);
        }
        return value;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private <T> T nonNullInternal(T value, String message, Object... messageArguments) {
        if (value == null) {
            throw new NullPointerException(String.format(message, messageArguments));
        }
        return value;
    }

    //------------------------------------------------------------------------------------------------------------------
    static private void throwIllegalArgument(String message, Object... messageArguments) {
        throw new IllegalArgumentException(String.format(message, messageArguments));
    }

    //------------------------------------------------------------------------------------------------------------------
    static private void throwIndexOutOfBounds(String message, Object... messageArguments) {
        throw new IndexOutOfBoundsException(String.format(message, messageArguments));
    }

    //------------------------------------------------------------------------------------------------------------------
    static private <T> T typeInternal(T value, Class<?> type, String message, Object... messageArguments) {
        if (!type.isInstance(value)) {
            throw new ClassCastException(String.format(message, messageArguments));
        }
        return value;
    }

    //******************************************************************************************************************
    // INSTANCE
    //******************************************************************************************************************

    //------------------------------------------------------------------------------------------------------------------
    /**
     * Constructor is private to prevent instantiation.
     */
    private Check() {
        throw new UnsupportedOperationException();
    }
}
