package com.huoyun.idp.exception;

import java.io.Serializable;

public class BusinessExceptionResponse implements Serializable {
	private static final long serialVersionUID = 1298939312232468750L;
	private String code;
	private String message;

	public BusinessExceptionResponse(BusinessException ex) {
		this.code = ex.getCode();
		this.message = ex.getMessage();
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
}