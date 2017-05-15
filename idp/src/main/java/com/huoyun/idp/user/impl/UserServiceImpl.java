package com.huoyun.idp.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.huoyun.idp.common.ErrorCode;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.common.api.user.CreateUserParam;
import com.huoyun.idp.controller.login.LoginData;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.email.EmailService;
import com.huoyun.idp.email.EmailTemplate;
import com.huoyun.idp.email.EmailTemplateNames;
import com.huoyun.idp.email.impl.EmailTemplateImpl;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.tenant.repository.TenantRepo;
import com.huoyun.idp.user.UserErrorCodes;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.user.repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private Facade facade;

	@Override
	public LoginData login(LoginParam loginParam) throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserByEmail(loginParam.getEmail());
		if (user == null) {
			throw new BusinessException(ErrorCode.User_Not_Exists);
		}

		if (StringUtils.equals(loginParam.getPassword(), user.getPassword())) {
			throw new BusinessException(ErrorCode.User_Login_Password_Invalid);
		}

		if (!user.isActive()) {
			throw new BusinessException(ErrorCode.User_Not_Active);
		}

		if (!user.isLocked()) {
			throw new BusinessException(ErrorCode.User_Locked);
		}

		return null;
	}

	@Override
	public void checkBeforeLogin(String username) throws BusinessException {
		// TODO Auto-generated method stub

	}

	@Override
	public User getUserByName(String username) {
		return this.facade.getService(UserRepo.class).getUserByEmail(username);
	}

	@Override
	public void createUser(User user) {
		this.facade.getService(UserRepo.class).save(user);

	}

	@Transactional
	@Override
	public void changePassword(Long userId, String oldPassword, String newPassword) throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.User_Not_Exists);
		}

		if (!StringUtils.equals(oldPassword, user.getPassword())) {
			throw new BusinessException(UserErrorCodes.Change_Password_Old_Password_Invalid);
		}

		user.setPassword(newPassword);
		this.facade.getService(UserRepo.class).save(user);
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void createUser(CreateUserParam createUserParam) throws BusinessException {
		Tenant tenant = this.facade.getService(TenantRepo.class).getTenantByTenantCode(createUserParam.getTenantCode());
		if (tenant == null) {
			throw new BusinessException(UserErrorCodes.Create_User_Failed_Due_To_Tenant_Not_Exists);
		}

		boolean userExists = this.facade.getService(UserRepo.class).exists(createUserParam.getEmail());
		if (userExists) {
			throw new BusinessException(UserErrorCodes.Create_User_Failed_Due_To_User_Exists);
		}

		User user = new User();
		user.setTenant(tenant);
		user.setEmail(createUserParam.getEmail());
		this.facade.getService(UserRepo.class).save(user);

		EmailTemplate template = new EmailTemplateImpl(EmailTemplateNames.User_Set_Init_Password);
		template.setVariable("user", user);
		template.setVariable("link", "http://localhost:8080/setPassword");
		this.facade.getService(EmailService.class).send(createUserParam.getEmail(), template);
	}
}
