package com.jsh.erp.exception;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


@Getter
@Setter
public class BizException extends RuntimeException implements Serializable {

	public static final int DEFAULT_EXCEPTION_CODE = -100;

	private Integer code = null;

	private String message = null;

	private Exception wrappedEx = null;

	public BizException(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	/**
	 * @param code 异常编码
	 * @param message 异常描述
	 * @param e 异常
	 */
	public BizException(Integer code, String message, Exception e) {
		this.code = code;
		this.message = message;
		this.wrappedEx = e;
	}
}
