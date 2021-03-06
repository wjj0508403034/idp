package com.huoyun.idp.exception;

public class BusinessException extends Exception {
	private static final long serialVersionUID = 4264580056776092254L;

	private String code;
	private String message;

	public BusinessException(String errorCode) {
		super(errorCode);
		this.code = errorCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

}
