package com.huoyun.idp.user;

import com.huoyun.idp.admin.tenant.CreateTenantParam;
import com.huoyun.idp.controller.login.LoginData;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.internal.api.user.CreateUserParam;
import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.view.user.ForgetPasswordParam;
import com.huoyun.idp.view.user.InitPasswordParam;
import com.huoyun.idp.view.user.ResetPasswordParam;

public interface UserService {

	LoginData login(LoginParam loginParam) throws BusinessException;

	User getUserByName(String username);

	void createUser(User user);

	void changePassword(Long userId, String oldPassword, String newPassword) throws BusinessException;

	void createUser(CreateUserParam createUserParam) throws BusinessException;

	void createUser(Tenant tenant, CreateTenantParam tenantParam) throws BusinessException;

	void verifyActiveCode(String activeCode) throws BusinessException;

	void initPassword(InitPasswordParam initPasswordParam) throws BusinessException;

	void requestForgetPassword(ForgetPasswordParam forgetPasswordParam) throws BusinessException;

	void verifyChangePasswordRequestCode(String requestCode) throws BusinessException;

	void resetPassword(ResetPasswordParam resetPasswordParam) throws BusinessException;
}
