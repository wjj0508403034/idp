package com.huoyun.idp.common.api.user;

public class CreateUserParam {

	private String tenantCode;
	private String email;

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTenantCode() {
		return tenantCode;
	}

	public void setTenantCode(String tenantCode) {
		this.tenantCode = tenantCode;
	}

}
