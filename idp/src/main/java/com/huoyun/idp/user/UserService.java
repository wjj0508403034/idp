package com.huoyun.idp.user;

import com.huoyun.idp.common.api.user.CreateUserParam;
import com.huoyun.idp.controller.login.LoginData;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.user.entity.User;

public interface UserService {
	
	LoginData login(LoginParam loginParam) throws BusinessException;

	void checkBeforeLogin(String username) throws BusinessException;
	
	User getUserByName(String username);
	
	void createUser(User user);
	
	void changePassword(Long userId,String oldPassword,String newPassword) throws BusinessException;

	void createUser(CreateUserParam createUserParam) throws BusinessException;
}
