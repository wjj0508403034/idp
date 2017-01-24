package com.huoyun.idp.common;

public class BusinessException extends Exception {
	private static final long serialVersionUID = 4264580056776092254L;
	private String code;

	public BusinessException(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
