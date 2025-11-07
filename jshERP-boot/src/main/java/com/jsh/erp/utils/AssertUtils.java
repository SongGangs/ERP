package com.jsh.erp.utils;

import com.jsh.erp.constants.ExceptionConstants;
import com.jsh.erp.exception.BizException;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@UtilityClass
public class AssertUtils {

	public static void assertTrue(boolean expect, String errorMsg) {
		if (!expect) {
			throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, errorMsg);
		}
	}

	public static void assertTrue(boolean expect, String format, Object... args) {
		if (!expect) {
			throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, String.format(format, args));
		}
	}

	public static void assertFalse(boolean expect, String errorMsg) {
		if (expect) {
			throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, errorMsg);
		}
	}

	public static void assertFalse(boolean expect, String format, Object... args) {
		if (expect) {
			throw new BizException(ExceptionConstants.DATA_READ_FAIL_CODE, String.format(format, args));
		}
	}

	public static void assertNull(Object param, String errorMsgFormat, Object... args) {
		assertTrue(param == null, errorMsgFormat, args);
	}

	public static void assertNotNull(Object param, String errorMsgFormat, Object... args) {
		assertTrue(param != null, errorMsgFormat, args);
	}

	public static void assertNotEmpty(Collection<?> param, String errorMsg) {
		assertTrue(CollectionUtils.isNotEmpty(param), errorMsg);
	}

	public static void assertNotEmpty(Collection<?> param, String format, Object... args) {
		assertTrue(CollectionUtils.isNotEmpty(param), format, args);
	}

	public static void assertNotEmpty(Map<?, ?> param, String format, Object... args) {
		assertTrue(MapUtils.isNotEmpty(param), format, args);
	}

	public static void assertNotEmpty(String param, String format, Object... args) {
		assertTrue(StringUtils.isNotEmpty(param), format, args);
	}

	public static void assertEmpty(Collection<?> param, String format, Object... args) {
		assertTrue(CollectionUtils.isEmpty(param), format, args);
	}

	public static void assertEmpty(Map<?, ?> param, String format, Object... args) {
		assertTrue(MapUtils.isEmpty(param), format, args);
	}

	public static void assertEmpty(String param, String format, Object... args) {
		assertTrue(StringUtils.isEmpty(param), format, args);
	}

	public static void assertEquals(Object a, Object b, String errorMsg) {
		assertTrue(Objects.equals(a, b), errorMsg);
	}

	public static void assertEquals(Object a, Object b, String format, Object... args) {
		assertTrue(Objects.equals(a, b), format, args);
	}

	public static void assertNotBlank(String param, String format, Object... args) {
		assertTrue(StringUtils.isNotBlank(param), format, args);
	}

	public static void assertSizeBetween(Collection<?> param, int a, int b, String format, Object... args) {
		assertTrue(param.size() >= a && param.size() <= b, format, args);
	}

	public static void assertSizeBetween(Object[] param, int a, int b, String format, Object... args) {
		assertTrue(param.length >= a && param.length <= b, format, args);
	}
}
