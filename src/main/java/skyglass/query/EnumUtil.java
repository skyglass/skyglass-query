package skyglass.query;

import org.apache.commons.lang3.StringUtils;

public class EnumUtil {


	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static Object getEnumInstanceObject(Object value, Class enumClass) {
		String stringValue = value == null ? null : value.toString();
		if (StringUtils.isBlank(stringValue)) {
			return null;
		}
		return Enum.valueOf(enumClass, stringValue);
	}

	public static <T extends Enum<T>> T getEnumInstance(Object value, Class<T> enumClass) {
		String stringValue = value == null ? null : value.toString();
		if (StringUtils.isBlank(stringValue)) {
			return null;
		}
		return Enum.valueOf(enumClass, value.toString());
	}

}
