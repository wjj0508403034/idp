package com.huoyun.idp.exception;

public class LocatableBusinessException extends BusinessException {
	private static final long serialVersionUID = 2613942628591575831L;

	private String path;

	public LocatableBusinessException(String errorCode, String path) {
		super(errorCode);
		this.path = path;
	}

	public LocatableBusinessException(String errorCode, String message,
			String path) {
		this(errorCode, path);
		this.setMessage(message);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}