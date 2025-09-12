package com.jsh.erp.utils;

import lombok.experimental.UtilityClass;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

@UtilityClass
public class DateUtils {

	private static final long ONE_SECONDS = 1000L;
	private static final long ONE_MINUTE = 60 * ONE_SECONDS;
	private static final long ONE_HOUR = 60 * ONE_MINUTE;
	private static final long ONE_DAY_MS = 24 * ONE_HOUR;

	public static Date toDate(LocalDateTime localDateTime) {
		if (Objects.isNull(localDateTime)) {
			return null;
		}

		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	public static Date toDate(LocalDate localDate) {
		if (Objects.isNull(localDate)) {
			return null;
		}

		Instant instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
		return Date.from(instant);
	}

	public static LocalDate toLocalDate(Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	public static LocalDateTime toLocalDateTime(Date date) {
		if (date == null) {
			return null;
		}

		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * 判断两个日期是否是同一天 例：
	 * day1                  day2                  return
	 * 2021-01-02 00:00:00   2021-01-02 00:00:00 true
	 * 2021-01-02 00:00:00   2021-01-02 23:59:59   true
	 * 2021-01-02 00:00:00   2021-01-02 11:11:11   true
	 * 2021-01-02 11:11:11   2021-01-02 12:12:12   true
	 * null                  null                  true
	 * <p>
	 * 2021-01-02 23:59:59   2021-01-03 00:00:00   false
	 * 2021-01-02 00:00:00   2021-01-01 23:59:59   false
	 * 2021-01-02 00:00:00   null                  false
	 * null                  2021-01-01 23:59:59   false
	 */
	public static boolean isTheSameDay(Date day1, Date day2) {
		// 日期相等返回true
		if (Objects.equals(day1, day2)) {
			return true;
		}
		// 日期有空返回false
		if (Objects.isNull(day1) || Objects.isNull(day2)) {
			return false;
		}
		return toLocalDate(day1).equals(toLocalDate(day2));
	}


	public static boolean isTheSameDay(LocalDate day1, LocalDate day2) {
		// 日期相等返回true
		if (Objects.equals(day1, day2)) {
			return true;
		}
		// 日期有空返回false
		if (Objects.isNull(day1) || Objects.isNull(day2)) {
			return false;
		}
		return day1.equals(day2);
	}

	public static LocalDateTime plusYears(LocalDateTime localDateTime, int years) {
		return localDateTime.plusYears(years);
	}

	public static LocalDate plusYears(LocalDate localDate, int years) {
		return localDate.plusYears(years);
	}

	public static Date plusYears(Date date, int years) {
		return add(date, Calendar.YEAR, years);
	}

	public static LocalDate plusMonths(LocalDate localDate, int months) {
		return localDate.plusMonths(months);
	}

	public static LocalDateTime plusMonths(LocalDateTime localDateTime, int months) {
		return localDateTime.plusMonths(months);
	}

	public static Date plusMonths(Date date, int months) {
		return add(date, Calendar.MONTH, months);
	}

	private static Date add(Date date, int field, int amount) {
		if (amount == 0) {
			return date;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(field, amount);
		return calendar.getTime();
	}

	public static LocalDate plusDays(LocalDate localDate, int days) {
		return localDate.plusDays(days);
	}

	public static LocalDateTime plusDays(LocalDateTime localDateTime, int days) {
		return localDateTime.plusDays(days);
	}

	public static Date plusDays(Date date, int days) {
		if (days == 0) {
			return date;
		}

		return new Date(date.getTime() + days * ONE_DAY_MS);
	}

	public static LocalDateTime plusHours(LocalDateTime localDateTime, int hours) {
		return localDateTime.plusHours(hours);
	}

	public static LocalTime plusHours(LocalTime localTime, int hours) {
		return localTime.plusHours(hours);
	}

	public static Date plusHours(Date date, int hours) {
		if (hours == 0) {
			return date;
		}

		return new Date(date.getTime() + hours * ONE_HOUR);
	}

	public static LocalDateTime plusMinutes(LocalDateTime localDateTime, int minutes) {
		return localDateTime.plusMinutes(minutes);
	}

	public static LocalTime plusMinutes(LocalTime localTime, int minutes) {
		return localTime.plusMinutes(minutes);
	}

	public static Date plusMinutes(Date date, int minutes) {
		if (minutes == 0) {
			return date;
		}

		return new Date(date.getTime() + minutes * ONE_MINUTE);
	}

	public static LocalDateTime plusSeconds(LocalDateTime localDateTime, int seconds) {
		return localDateTime.plusSeconds(seconds);
	}

	public static LocalTime plusSeconds(LocalTime localTime, int seconds) {
		return localTime.plusSeconds(seconds);
	}

	public static Date plusSeconds(Date date, int seconds) {
		if (seconds == 0) {
			return date;
		}

		return new Date(date.getTime() + seconds * ONE_SECONDS);
	}

	public static LocalDateTime minusYears(LocalDateTime localDateTime, int years) {
		return plusYears(localDateTime, -years);
	}

	public static LocalDate minusYears(LocalDate localDate, int years) {
		return plusYears(localDate, -years);
	}

	public static Date minusYears(Date date, int years) {
		return plusYears(date, -years);
	}

	public static LocalDate minusMonths(LocalDate localDate, int months) {
		return plusMonths(localDate, -months);
	}

	public static LocalDateTime minusMonths(LocalDateTime localDateTime, int months) {
		return plusMonths(localDateTime, -months);
	}

	public static Date minusMonths(Date date, int months) {
		return plusMonths(date, -months);
	}

	public static LocalDate minusDays(LocalDate localDate, int days) {
		return plusDays(localDate, -days);
	}

	public static LocalDateTime minusDays(LocalDateTime localDateTime, int days) {
		return plusDays(localDateTime, -days);
	}

	public static Date minusDays(Date date, int days) {
		return plusDays(date, -days);
	}

	public static LocalDateTime minusHours(LocalDateTime localDateTime, int hours) {
		return plusHours(localDateTime, -hours);
	}

	public static LocalTime minusHours(LocalTime localTime, int hours) {
		return plusHours(localTime, -hours);
	}

	public static Date minusHours(Date date, int hours) {
		return plusHours(date, -hours);
	}

	public static LocalDateTime minusMinutes(LocalDateTime localDateTime, int minutes) {
		return plusMinutes(localDateTime, -minutes);
	}

	public static LocalTime minusMinutes(LocalTime localTime, int minutes) {
		return plusMinutes(localTime, -minutes);
	}

	public static Date minusMinutes(Date date, int minutes) {
		return plusMinutes(date, -minutes);
	}

	public static LocalDateTime minusSeconds(LocalDateTime localDateTime, int seconds) {
		return plusSeconds(localDateTime, -seconds);
	}

	public static LocalTime minusSeconds(LocalTime localTime, int seconds) {
		return plusSeconds(localTime, -seconds);
	}

	public static Date minusSeconds(Date date, int seconds) {
		return plusSeconds(date, -seconds);
	}

	public static LocalDate max(LocalDate a, LocalDate b) {
		return a.compareTo(b) > 0 ? a : b;
	}

	public static Date max(Date a, Date b) {
		return a.compareTo(b) > 0 ? a : b;
	}

	public static LocalDate min(LocalDate a, LocalDate b) {
		return a.compareTo(b) < 0 ? a : b;
	}

	public static Date min(Date a, Date b) {
		return a.compareTo(b) < 0 ? a : b;
	}

	public static Date min(Date a, Date b, Date c) {
		return min(min(a, b), c);
	}

	/**
	 * endDate - startDate 相差天数
	 *     endDate       startDate
	 * 例：2024-06-27 - 2024-06-26 = 1
	 * 例：2024-06-26 - 2024-06-27 = -1
	 * 和老服务不一致，禁止使用
	 */
	@Deprecated
	public static int getDifferenceOfDate(Date startDate, Date endDate) {
		if (startDate.equals(endDate)) {
			return 0;
		}

		return (int) (toLocalDate(endDate).toEpochDay() - toLocalDate(startDate).toEpochDay());
	}

	/**
	 * endDate - startDate 相差天数，取绝对值
	 *     endDate       startDate
	 * 例：2024-06-27 - 2024-06-26 = 1
	 * 例：2024-06-26 - 2024-06-27 = 1
	 */
	public static int getDistDates(Date startDate, Date endDate) {
		if (Objects.equals(startDate, endDate)) {
			return 0;
		}

		return Math.abs(getDifferenceOfDate(startDate, endDate));
	}

	/**
	 * endDate - startDate 相差天数，取绝对值
	 *     endDate       startDate
	 * 例：2024-06-27 - 2024-06-26 = 1
	 * 例：2024-06-26 - 2024-06-27 = 1
	 */
	public static int getDistDates(LocalDate startDate, LocalDate endDate) {
		if (Objects.equals(startDate, endDate)) {
			return 0;
		}

		return (int) Math.abs(startDate.toEpochDay() - endDate.toEpochDay());
	}

	/**
	 * 计算两个日期之间间隔的中文描述
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return 间隔描述 示例 累计11月8天, 共344天
	 */
	public static String getIntervalPeriodDesc(Date startDate, Date endDate) {
		if (startDate == null || endDate == null) {
			return "";
		}
		if (endDate.before(startDate)) {
			return "";
		}
		LocalDate startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		long years = ChronoUnit.YEARS.between(startLocalDate, endLocalDate.plusDays(1));
		long months = ChronoUnit.MONTHS.between(startLocalDate, endLocalDate.plusDays(1)) % 12;
		long days = ChronoUnit.DAYS.between(startLocalDate.plusYears(years).plusMonths(months), endLocalDate.plusDays(1));
		long totalDays = ChronoUnit.DAYS.between(startLocalDate, endLocalDate.plusDays(1));
		// 构建描述
		StringBuilder sb = new StringBuilder("累计");
		if (years > 0) {
			sb.append(years).append("年");
		}
		if (months > 0) {
			sb.append(months).append("月");
		}
		if (days > 0) {
			sb.append(days).append("天");
		}
		sb.append(", 共").append(totalDays).append("天");
		return sb.toString();
	}

	/**
	 * 获取一天开始时间，
	 */
	public static Date getDayBeginTime(Date date) {
		return toDate(toLocalDate(date));
	}

	/**
	 * 获取后几天开始时间，
	 */
	public static Date getNextDayBeginTime(Date date, int days) {
		if (Objects.isNull(date)) {
			return null;
		}
		return toDate(toLocalDate(date).plusDays(days));
	}

	/**
	 * 获取一天结束时间，
	 */
	public static Date getDayEndTime(Date date) {
		LocalDateTime endOfDay = LocalDateTime.of(toLocalDate(date), LocalTime.MAX);
		return toDate(endOfDay);
	}


	public static boolean dayBetween(Date date, Date startDate, Date endDate) {
		return date.compareTo(startDate) >= 0 && date.compareTo(endDate) <= 0;
	}

	/**
	 * 是否在日期范围内
	 */
	public static Boolean dayBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
		return !date.isBefore(startDate) && !date.isAfter(endDate);
	}

	/**
	 * 判断两个日期是否有重叠
	 * @param startDate1
	 * @param endDate1
	 * @param startDate2
	 * @param endDate2
	 * @return
	 */
	public static boolean isOverlap(Date startDate1, Date endDate1, Date startDate2, Date endDate2) {
		return !(endDate1.before(startDate2) || endDate2.before(startDate1));
	}

	/**
	 * 判断两个日期是否有重叠
	 * @param startDate1
	 * @param endDate1
	 * @param startDate2
	 * @param endDate2
	 * @return
	 */
	public static boolean isOverlap(LocalDate startDate1, LocalDate endDate1, LocalDate startDate2, LocalDate endDate2) {
		return !(endDate1.isBefore(startDate2) || endDate2.isBefore(startDate1));
	}

	/**
	 * 判断日期是否在给定范围内，只判断月、日，忽略年
	 * @param startTime 开始日期
	 * @param endTime 结束日期
	 * @param target 校验日期
	 * @return true在范围内，false不在范围内
	 */
	public static boolean isMonthDayInRange(Date startTime, Date endTime, Date target) {
		MonthDay start = toMonthDay(startTime);
		MonthDay end = toMonthDay(endTime);
		MonthDay current = toMonthDay(target);

		if (start.isBefore(end) || start.equals(end)) {
			// 正常范围：如 3月1日 到 5月31日
			return (current.equals(start) || current.equals(end)) ||
				(current.isAfter(start) && current.isBefore(end));
		} else {
			// 跨年范围：如 11月1日 到 2月28日（年底到次年初）
			return current.isAfter(start) || current.isBefore(end) ||
				current.equals(start) || current.equals(end);
		}
	}

	public static MonthDay toMonthDay(Date date) {
		return MonthDay.from(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
	}

}
