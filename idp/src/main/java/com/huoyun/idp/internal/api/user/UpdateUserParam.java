package com.huoyun.idp.internal.api.user;

public class UpdateUserParam {

	private Long userId;
	private boolean locked;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public boolean isLocked() {
		return locked;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

}
