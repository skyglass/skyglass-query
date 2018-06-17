/*
 * StringUtil.java
 */
package skyglass.data.common.util;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import skyglass.data.common.model.NameableObject;

/**
 *
 * @author Ryan Smith
 */
final public class StringUtil {

    //**************************************************************************
    // CLASS
    //**************************************************************************

    final static private int NORMAL_STATE = 0;
    final static private int ESCAPE_STATE = 1;
    final static private int UNICODE_ESCAPE_STATE = 2;
    final static private int OCTAL_ESCAPE_STATE = 3;
    final static private char[] hexDigits = "0123456789ABCDEF".toCharArray();

    final static public String N_SYMBOL = new String("\n");


    final static public class CaseInsensitiveComparator
    implements Comparator<String>, Serializable {

        private static final long serialVersionUID = 1L;

        /* (non-Javadoc)
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        public int compare(String o1, String o2) {
            return StringUtil.compareIgnoreCase(o1, o2);
        }
    }

    static public int compareIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return 0;
        }
        if (str1 == null) {
            return -1;
        }
        if (str2 == null) {
            return 1;
        }
        return str1.compareToIgnoreCase(str2);
    }

    //--------------------------------------------------------------------------
    /**
     * @param obj the object of which to get the string form
     * @return null if object was null, else String.valueOf(obj)
     */
    static public String valueOfObject(Object obj) {
        return obj == null ? null : String.valueOf(obj);
    }

    //--------------------------------------------------------------------------
    /**
     * @param s the string to trim (can be null)
     * @return null if s was null or s.trim() was empty, otherwise s.trim()
     */
    static public String trimToNull(String s) {
        String trimmed = (s == null ? null : s.trim());
        return (trimmed == null || trimmed.length() == 0) ? null : trimmed;
    }

    //--------------------------------------------------------------------------
    /**
     * @param s the string to trim
     * @param length the maximum length to which to trim the given string
     * @return if s is null or shorter than/equal length, returns s,
     *           otherwise returns a string of the first length characters of s
     */
    static public String trimToLength(String s, int length) {
        return (s != null && s.length() > length ? s.substring(0, length) : s);
    }

    //--------------------------------------------------------------------------
    /**
     * @param s String to check for empty/null
     * @return true if s is empty or null
     */
    static public boolean isEmpty(String s) {
        return s == null || s.length() == 0;
    }

    //--------------------------------------------------------------------------
    /**
     * Joins a String array into a single String and it does that
     * be insuring the Strings are joined using the joinWith String. It also
     * insures that the joinWith String is not repeated.
     * Eg. Using join() on "directory/" and "/file" with "/" returns
     * "directory/file".
     */
    static public String join(String[] stringsToJoin, String joinWith) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < stringsToJoin.length; i++) {
            if (i == 0) {
                sb.append(keepTrimmingEnd(stringsToJoin[i], joinWith));
                sb.append(joinWith);
            }
            else if (i == stringsToJoin.length - 1) {
                sb.append(keepTrimmingStart(stringsToJoin[i], joinWith));
            }
            else {
                String s = keepTrimmingStart(stringsToJoin[i], joinWith);
                sb.append(keepTrimmingEnd(s, joinWith));
                sb.append(joinWith);
            }
        }
        return sb.toString();
    }

    /**
     * Joins a String array into a single String and it does that
     * be insuring the Strings are joined using the joinWith String. It also
     * insures that the joinWith String is not repeated.
     * Eg. Using join() on "directory/" and "/file" with "/" returns
     * "directory/file".
     */
    static public String join(String[] stringsToJoin, char joinWith) {
        return join(stringsToJoin, String.valueOf(joinWith));
    }

    static public String join(Collection<String> collection, String joinWith) {
        String result = "";
        if (null != collection) {
            result = keepTrimmingEnd(join(collection.toArray(new String[collection.size()]), joinWith), joinWith);
        }
        return result;
    }

    static public String joinNames(Collection<? extends NameableObject> collection, String joinWith) {
        List<String> namesOnly = new ArrayList<String>();
        for (NameableObject nameableObject : collection) {
            namesOnly.add(nameableObject.getName());
        }
        return join(namesOnly, joinWith);
    }

    static public String joinNames(Collection<? extends NameableObject> collection) {
        return joinNames(collection, ", ");
    }

    static public String join(Collection<String> collection, char joinWith) {
        return join(collection, String.valueOf(joinWith));
    }

    //--------------------------------------------------------------------------
    static private String keepTrimmingEnd(String s, String trim) {
        int endIndex = s.length();
        while (s.regionMatches(true, endIndex - trim.length(), trim, 0, trim.length())) {
            endIndex -= trim.length();
        }
        return s.substring(0, endIndex);
    }

    //--------------------------------------------------------------------------
    static private String keepTrimmingStart(String s, String trim) {
        int startIndex = 0;
        while (s.regionMatches(true, startIndex, trim, 0, trim.length())) {
            startIndex += trim.length();
        }
        return s.substring(startIndex);
    }


    //--------------------------------------------------------------------------
    /**
     * Replaces all instances of toReplace in source with replaceWith and return
     * the result.
     *
     * @param source the string to check
     * @param toReplace the substring that should be replaced
     * @param replaceWith the string the toReplace should be replaced with
     * @throws NullPointerException if toReplace is null
     * @deprecated replaced by {@link #replace()}
     */
    @Deprecated
    static public String stringReplace(String source, String toReplace, String replaceWith) {
        return replace(source, toReplace, replaceWith);
    }

    //--------------------------------------------------------------------------
    /**
     * Replaces all instances of toReplace in source with replaceWith and return
     * the result.
     *
     * @param source the string to check
     * @param toReplace the substring that should be replaced
     * @param replaceWith the string the toReplace should be replaced with
     * @throws NullPointerException if toReplace is null
     */
    static public String replace(String source, String toReplace, String replaceWith) {
        if (source == null || source.length() == 0) {
            return source;
        }

        int toReplaceLength = toReplace.length();

        StringBuffer newString = new StringBuffer();

        int lastIndex = 0;
        int index = source.indexOf(toReplace, lastIndex);

        if (index < 0) {
            return source;
        }

        while (index >= 0) {
            newString.append(source.substring(lastIndex, index));
            newString.append(replaceWith);
            lastIndex = index + toReplaceLength;
            index = source.indexOf(toReplace, lastIndex);
        }
        newString.append(source.substring(lastIndex));

        return newString.toString();
    }

    //--------------------------------------------------------------------------
    /**
     * A convenience wrapper for s.regionMatches(true,...)
     *
     * @param s The string to check
     * @param prefix The prefix to check for
     * @return true if (and only if) s begins with prefix ignoring case
     * @throws NullPointerException if either s or prefix is null
     */
    static public boolean startsWithIgnoreCase(String s, String prefix) {
        return s.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    //--------------------------------------------------------------------------
    /**
     * A convenience wrapper for s.regionMatches(true,...)
     *
     * @param s The string to check
     * @param suffix The suffix to check for
     * @return true if (and only if) s ends with suffix ignoring case
     * @throws NullPointerException if either s or suffix is null
     */
    static public boolean endsWithIgnoreCase(String s, String suffix) {
        return s.regionMatches(true, s.length() - suffix.length(), suffix, 0, suffix.length());
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the number of sequential matching characters at
     * the beginning of the two given Strings. (case-sensitive)
     *
     * @param t the first String
     * @param o the second String
     * @return the length of the common substring starting from given offsets
     * @throws NullPointerException if either String is null
     */
    static public int findMatchingPrefixLength(String t, String o) {
        return findMatchingSubstringLength(false, t, 0, o, 0);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the number of sequential matching characters at
     * the beginning of the two given Strings.
     *
     * @param ignoreCase whether or not character comparisons are case-sensitive
     * @param t the first String
     * @param o the second String
     * @return the length of the common substring starting from beginning of each string
     * @throws NullPointerException if either String is null
     */
    static public int findMatchingPrefixLength(boolean ignoreCase,  String t, String o) {
        return findMatchingSubstringLength(false, t, 0, o, 0);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the number of sequential matching characters in
     * two given Strings, starting at their respective offsets. (case-sensitive)
     *
     * @param t the first String
     * @param toffset the offset within the first String
     * @param o the second String
     * @param ooffset offset within the second String
     * @return the length of the common substring starting from given offsets
     * @throws NullPointerException if either String is null
     */
    static public int findMatchingSubstringLength(String t, int toffset, String o, int ooffset) {
        return findMatchingSubstringLength(false, t, toffset, o, ooffset);
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the number of sequential matching characters in
     * two given Strings, starting at their respective offsets.
     *
     * @param ignoreCase whether or not character comparisons are case-sensitive
     * @param t the first String
     * @param toffset the offset within the first String
     * @param o the second String
     * @param ooffset offset within the second String
     * @return the length of the common substring starting from given offsets
     * @throws NullPointerException if either String is null
     */
    static public int findMatchingSubstringLength(boolean ignoreCase, String t, int toffset, String o, int ooffset) {

        final int lastChar = Math.min(t.length() - toffset, o.length() - ooffset);
        for (int i = 0; i < lastChar; i++) {
            char c0 = t.charAt(i+toffset);
            char c1 = o.charAt(i+ooffset);
            // this is basically how String.regionMatches() works
            if (c0 != c1
                    && (!ignoreCase
                            || (Character.toLowerCase(c0) != Character.toLowerCase(c1)
                                    && (Character.toUpperCase(c0) != Character.toUpperCase(c1))))) {
                return i;
            }
        }
        return lastChar;
    }

    //--------------------------------------------------------------------------
    /**
     * Returns the substring of sequential matching characters in
     * two given Strings, starting at their respective offsets.
     *
     * @param ignoreCase whether or not character comparisons are case-sensitive
     * @param t the first String
     * @param toffset the offset within the first String
     * @param o the second String
     * @param ooffset offset within the second String
     * @return the common substring starting from given offsets,
     *            if ignoreCase is true, then the case of the result is taken from the first String
     * @throws NullPointerException if either String is null
     */
    static public String findCommonSubstring(boolean ignoreCase, String t, int toffset, String o, int ooffset) {
        int subseqLength = findMatchingSubstringLength(ignoreCase, t, toffset, o, ooffset);
        return t.substring(toffset, toffset+subseqLength);
    }

    //--------------------------------------------------------------------------
    static public String escapeJava(String s) {
        String result = null;

        if (s != null) {
            StringBuffer sb = new StringBuffer();
            synchronized (sb) {
                for (int i = 0; i < s.length(); ++i) {
                    char c = s.charAt(i);

                    if (c == '\\') {
                        sb.append("\\\\");
                    }
                    else if (c == '\b') {
                        sb.append("\\b");
                    }
                    else if (c == '\t') {
                        sb.append("\\t");
                    }
                    else if (c == '\n') {
                        sb.append("\\n");
                    }
                    else if (c == '\f') {
                        sb.append("\\f");
                    }
                    else if (c == '\r') {
                        sb.append("\\r");
                    }
                    else if (c == '"') {
                        sb.append("\\\"");
                    }
                    else if (c == '\'') {
                        sb.append("\\\'");
                    }
                    else if (c < ' ' || c > '~') {
                        sb.append('\\');
                        sb.append('u');
                        sb.append(hexDigits[(c >>> 12) & 0xF]);
                        sb.append(hexDigits[(c >>>  8) & 0xF]);
                        sb.append(hexDigits[(c >>>  4) & 0xF]);
                        sb.append(hexDigits[(c >>>  0) & 0xF]);
                    }
                    else {
                        sb.append(c);
                    }
                }

                result = sb.toString();
            }
        }
        return result;
    }

    //--------------------------------------------------------------------------
    static public String unescapeJava(String str) {
        String result = null;
        int state = NORMAL_STATE;
        char escapeChar = 0;
        int digitValue = 0;
        int digitCount = 0;

        if (str != null) {
            StringBuffer buffer = new StringBuffer(str.length());

            synchronized (buffer) {
                for (int i = 0; i < str.length(); ++i) {
                    char c = str.charAt(i);
                    switch (state) {
                    case NORMAL_STATE:
                        if (c != '\\') {
                            buffer.append(c);
                        }
                        else {
                            state = ESCAPE_STATE;
                        }
                        break;
                    case ESCAPE_STATE:
                        switch (c) {
                        case 'u':
                            state = UNICODE_ESCAPE_STATE;
                            break;
                        case '\\': case '"': case '\'':
                            buffer.append(c);
                            state = NORMAL_STATE;
                            break;
                        case 'b':
                            buffer.append('\b');
                            state = NORMAL_STATE;
                            break;
                        case 't':
                            buffer.append('\t');
                            state = NORMAL_STATE;
                            break;
                        case 'n':
                            buffer.append('\n');
                            state = NORMAL_STATE;
                            break;
                        case 'f':
                            buffer.append('\f');
                            state = NORMAL_STATE;
                            break;
                        case 'r':
                            buffer.append('\r');
                            state = NORMAL_STATE;
                            break;
                        case '0': case '1': case '2': case '3':
                            escapeChar = (char) (c - '0');
                            digitCount = 1;
                            state = OCTAL_ESCAPE_STATE;
                            break;
                        case '4': case '5': case '6': case '7':
                            escapeChar = (char) (c - '0');

                            // octal literals starting 4-7 can have up to 1 additional
                            // digit, so set the counter so that the next digit will
                            // terminate the escape

                            digitCount = 2;
                            state = OCTAL_ESCAPE_STATE;
                            break;
                        default:
                            throw new IllegalArgumentException("invalid escape");
                        }
                        break;
                    case OCTAL_ESCAPE_STATE:
                        digitValue = Character.digit(c, 8);
                        if (digitValue != -1) {
                            escapeChar = (char) (escapeChar << 3 | digitValue);
                            ++digitCount;
                        }
                        if (digitCount == 3 || digitValue == -1) {
                            buffer.append(escapeChar);
                            digitCount = 0;
                            escapeChar = 0;
                            state = NORMAL_STATE;

                            // handle terminating non-octal character

                            if (digitValue == -1) {
                                if (c != '\\') {
                                    buffer.append(c);
                                }
                                else {
                                    state = ESCAPE_STATE;
                                }
                            }
                        }
                        break;
                    case UNICODE_ESCAPE_STATE:

                        // java allows multiple 'u's

                        if (digitCount == 0 && c == 'u') {
                            break;
                        }
                        digitValue = Character.digit(c, 16);
                        if (digitValue == -1) {
                            throw new IllegalArgumentException("invalid escape");
                        }
                        escapeChar = (char) (escapeChar << 4 | digitValue);
                        ++digitCount;
                        if (digitCount == 4) {
                            buffer.append(escapeChar);
                            digitCount = 0;
                            escapeChar = 0;
                            state = NORMAL_STATE;
                        }
                        else if (digitCount > 4) {
                            throw new IllegalArgumentException("invalid escape");
                        }
                        break;
                    }
                }

                // only octal escapes can terminate at end of input

                if (state == OCTAL_ESCAPE_STATE) {
                    buffer.append(escapeChar);
                }
                else if (state != NORMAL_STATE) {
                    throw new IllegalArgumentException("invalid escape");
                }
                result = buffer.toString();
            }
        }
        return result;
    }

    public static void main(String[] args) {
        String x = UUID.randomUUID().toString();
        System.out.println(x);
        System.out.println(escapePercent(x));
    }

    //--------------------------------------------------------------------------
    /**
     * Performs percent encoding according to RFC 3986 with UTF-8 encoding.
     */
    static public String escapePercent(String str) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer bb = IO.utf8().encode(str);
        while (bb.hasRemaining()) {
            byte b = bb.get();
            if ((b >= 'A' && b <= 'Z') ||
                (b >= 'a' && b <= 'z') ||
                (b >= '0' && b <= '9') ||
                b == '-' || b == '_' || b == '.' || b == '~')
            {
                sb.append((char)b);
            }
            else {
                sb.append('%');
                sb.append(hexDigits[(b >>> 4) & 0xF]);
                sb.append(hexDigits[(b >>> 0) & 0xF]);
            }
        }
        return sb.toString();
    }

    //--------------------------------------------------------------------------
    /**
     * Filter empty values by sliding non-empty values to the front and
     * returning the first index after the end of the non-empty prefix. The
     * elements after the prefix are undefined and may not be null.
     */
    static public int filterEmpty(String[] array, int offset, int length) {
        Check.indexRange(array.length, offset, length);
        int p = offset;
        for (int i = offset, end = offset + length; i < end; i++) {
            if (!isEmpty(array[i])) {
                array[p] = array[i];
                p++;
            }
        }
        return p;
    }

    //--------------------------------------------------------------------------
    /**
     * Extracts string from Object
     */
    static public String extractString(Object obj) {
        String ret = null;
        if(obj != null){
            if (obj.getClass().equals(String.class)){
                ret = (String)obj;
            } else if (obj.getClass().equals(String[].class)){
                String [] arr = (String[])obj;
                ret = join(arr, " ").trim();
            } else {
                ret = obj.toString();
            }
        }
        return ret;
    }

    public static void replaceAll(StringBuilder builder, String from, String to){
        int index = builder.indexOf(from);
        while (index != -1)
        {
            builder.replace(index, index + from.length(), to);
            index += to.length();
            index = builder.indexOf(from, index);
        }
    }

    //**************************************************************************
    // INSTANCE
    //**************************************************************************

    //--------------------------------------------------------------------------
    private StringUtil() {
    }
}