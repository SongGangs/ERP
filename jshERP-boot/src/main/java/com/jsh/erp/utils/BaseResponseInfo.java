package com.jsh.erp.utils;

public class BaseResponseInfo {
	public int code;
	public Object data;
	
	public BaseResponseInfo() {
		code = 400;
		data = null;
	}

	public static BaseResponseInfo success(Object data) {
		BaseResponseInfo responseInfo = new BaseResponseInfo();
		responseInfo.code = 200;
		responseInfo.data = data;
		return responseInfo;
	}
}
