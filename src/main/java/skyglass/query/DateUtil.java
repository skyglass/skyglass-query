package skyglass.query;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtil {

	private static final Map<String, Map<ZoneId, DateTimeFormatter>> formatterMap = new HashMap<>();

	public static final String DEFAULT_TIME_ZONE = "UTC";

	private static final String DISPLAY_DATE_PATTERN = "yyyy-MM-dd";

	private static final String DISPLAY_DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

	private static final String DATE_PATTERN = "yyyy-M-d";

	private static final String DATE_TIME_PATTERN = "yyyy-M-d H:m:s";

	public static DateTimeFormatter getDisplayDateFormatter() {
		return getFormatter(DISPLAY_DATE_PATTERN);
	}

	public static DateTimeFormatter getDisplayDateTimeFormatter() {
		return getFormatter(DISPLAY_DATE_TIME_PATTERN);
	}

	public static DateTimeFormatter getDateFormatter() {
		return getFormatter(DATE_PATTERN);
	}

	public static DateTimeFormatter getDateTimeFormatter() {
		return getFormatter(DATE_TIME_PATTERN);
	}

	public static DateTimeFormatter getDateFormatter(String timeZone) {
		return getFormatter(DATE_PATTERN, getTimeZone(timeZone));
	}

	private static DateTimeFormatter getFormatter(String pattern) {
		return getFormatter(pattern, getDefaultTimeZone());
	}

	private static DateTimeFormatter getFormatter(String pattern, ZoneId timeZone) {
		return formatterMap.computeIfAbsent(pattern, p -> new HashMap<>()).computeIfAbsent(timeZone, tz -> DateTimeFormatter.ofPattern(pattern).withZone(tz));
	}

	public static Date now() {
		return toDateTime(LocalDateTime.now());
	}

	public static Date toDateTime(LocalDateTime localDate) {
		return Date.from(localDate.atZone(getDefaultTimeZone()).toInstant());
	}

	public static Date toDate(LocalDate localDate) {
		return toDate(localDate, DEFAULT_TIME_ZONE);
	}

	public static Date toDate(LocalDate localDate, String timeZone) {
		return Date.from(localDate.atStartOfDay(getTimeZone(timeZone)).toInstant());
	}

	public static LocalDate fromDate(Date date) {
		return date.toInstant().atZone(getDefaultTimeZone()).toLocalDate();
	}

	public static ZoneId getDefaultTimeZone() {
		return getTimeZone(DEFAULT_TIME_ZONE);
	}

	public static ZoneId getTimeZone(String timeZone) {
		return ZoneId.of(timeZone);
	}

	public static int getDaysInPeriod(Date fromDate, Date toDate) {
		int result = (int) ChronoUnit.DAYS.between(fromDate(fromDate), fromDate(toDate));
		return result < 0 ? 0 : result;
	}

	public static int getWeeksInPeriod(Date fromDate, Date toDate) {
		int result = (int) ChronoUnit.WEEKS.between(fromDate(fromDate), fromDate(toDate));
		return result < 0 ? 0 : result;
	}

	public static Date plusWeeks(Date date, int weeks) {
		return toDate(fromDate(date).plusWeeks(weeks));
	}

	public static Date minusWeeks(Date date, int weeks) {
		return toDate(fromDate(date).minusWeeks(weeks));
	}

	public static Date plusDays(Date date, int days) {
		return toDate(fromDate(date).plusDays(days));
	}

	public static Date minusDays(Date date, int days) {
		return toDate(fromDate(date).minusDays(days));
	}

	public static Date endOfDate(Date date) {
		return toDateTime(fromDate(date).atTime(23, 59, 59));
	}

	public static String format(Date date) {
		return getDisplayDateFormatter().format(fromDate(date));
	}

	public static String format(Date date, String pattern) {
		return getFormatter(pattern).format(fromDate(date));
	}

	public static Date parse(String dateString) {
		return toDate(parseToLocalDate(dateString));
	}

	public static Date parseEndOfDate(String dateString) {
		return endOfDate(parse(dateString));
	}

	public static Date parse(String dateString, DateTimeFormatter formatter, String timeZone) {
		return toDate(parseToLocalDate(dateString, formatter), timeZone);
	}

	public static LocalDate parseToLocalDate(String dateString) {
		return parseToLocalDate(dateString, getDateFormatter());
	}

	public static LocalDate parseToLocalDate(String dateString, DateTimeFormatter formatter) {
		return LocalDate.parse(dateString, formatter);
	}

	public static LocalDateTime parseToLocalDateTime(String dateTimeString) {
		return LocalDateTime.parse(dateTimeString, getDateTimeFormatter());
	}

	public static Date parseDateTime(String dateTimeString) {
		return toDateTime(parseToLocalDateTime(dateTimeString));
	}

}
