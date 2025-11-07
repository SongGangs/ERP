package com.jsh.erp.datasource.vo.enums;

import java.util.Objects;

public interface BaseEnum {

	Integer getType();

	String getDesc();

	default boolean isType(Integer type) {
		return Objects.equals(getType(), type);
	}

	default boolean notType(Integer type) {
		return !isType(type);
	}
}
