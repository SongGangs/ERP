package com.jsh.erp.utils;

import com.jsh.erp.datasource.vo.enums.BaseEnum;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class EnumUtils {

	/**
	 * 获取枚举类实例对象
	 *
	 * @param type  类型值
	 * @param clazz 枚举类class
	 * @param <T>   必须是实现 BaseEnum枚举
	 * @return 实例对象
	 */
	public static <T extends Enum<T> & BaseEnum> T getInstance(Integer type, Class<T> clazz) {
		if (type == null) {
			return null;
		}

		for (T enumConstant : clazz.getEnumConstants()) {
			if (type.equals(enumConstant.getType())) {
				return enumConstant;
			}
		}
		return null;
	}

	/**
	 * 获取枚举类实例对象
	 *
	 * @param types  类型值
	 * @param clazz 枚举类class
	 * @param <T>   必须是实现 BaseEnum枚举
	 * @return 实例对象
	 */
	public static <T extends Enum<T> & BaseEnum> List<T> getInstances(Collection<Integer> types, Class<T> clazz) {
		if (CollectionUtils.isEmpty(types)) {
			return Collections.emptyList();
		}
		return types.stream()
			.map(type -> getInstance(type, clazz))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	/**
	 * 获取枚举描述
	 *
	 * @param type  类型值
	 * @param clazz 枚举类class
	 * @param <T>   必须是实现 BaseEnum枚举
	 * @return 描述
	 */
	public static <T extends Enum<T> & BaseEnum> String getDesc(Integer type, Class<T> clazz) {
		T instance = getInstance(type, clazz);
		return instance != null ? instance.getDesc() : null;
	}

	/**
	 * 提取枚举对象type字段
	 *
	 * @param baseEnums 枚举对象
	 * @return type list
	 */
	public static List<Integer> extractType(BaseEnum... baseEnums) {
		if (ArrayUtils.isEmpty(baseEnums)) {
			return Collections.emptyList();
		}

		List<Integer> ret = new ArrayList<>(baseEnums.length);
		for (BaseEnum BaseEnum : baseEnums) {
			ret.add(BaseEnum.getType());
		}
		return ret;
	}

	public static List<Integer> extractType(Collection<? extends BaseEnum> baseEnums) {
		if (CollectionUtils.isEmpty(baseEnums)) {
			return Collections.emptyList();
		}

		List<Integer> ret = new ArrayList<>(baseEnums.size());
		for (BaseEnum BaseEnum : baseEnums) {
			ret.add(BaseEnum.getType());
		}
		return ret;
	}
}
