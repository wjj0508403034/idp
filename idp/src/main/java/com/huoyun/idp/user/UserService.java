package com.huoyun.idp.user;

import com.huoyun.idp.common.BusinessException;
import com.huoyun.idp.controller.login.LoginData;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.user.entity.User;

public interface UserService {
	LoginData login(LoginParam loginParam) throws BusinessException;

	void checkBeforeLogin(String username) throws BusinessException;
	
	User getUserByName(String username);
}
