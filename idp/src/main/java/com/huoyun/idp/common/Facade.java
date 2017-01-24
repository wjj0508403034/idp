package com.huoyun.idp.common;

import com.huoyun.idp.user.UserInfo;

public interface Facade {

	<T> T getService(Class<T> requiredType);
	
	UserInfo getCurrentUserInfo();
}
