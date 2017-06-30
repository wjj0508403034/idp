package com.huoyun.idp.user.impl;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.util.StringUtils;

import com.huoyun.idp.admin.tenant.CreateTenantParam;
import com.huoyun.idp.common.ErrorCode;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.domains.Domain;
import com.huoyun.idp.domains.DomainService;
import com.huoyun.idp.email.EmailService;
import com.huoyun.idp.email.EmailTemplate;
import com.huoyun.idp.email.EmailTemplateNames;
import com.huoyun.idp.email.impl.EmailTemplateImpl;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.exception.LocatableBusinessException;
import com.huoyun.idp.internal.api.user.CreateUserParam;
import com.huoyun.idp.internal.api.user.DeleteUserParam;
import com.huoyun.idp.internal.api.user.UpdateUserParam;
import com.huoyun.idp.saml2.Saml2ErrorCodes;
import com.huoyun.idp.tenant.Tenant;
import com.huoyun.idp.tenant.repository.TenantRepo;
import com.huoyun.idp.user.UserErrorCodes;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.ForgetPasswordHistory;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.user.repository.ForgetPasswordHistoryRepo;
import com.huoyun.idp.user.repository.UserRepo;
import com.huoyun.idp.view.user.ForgetPasswordParam;
import com.huoyun.idp.view.user.InitPasswordParam;
import com.huoyun.idp.view.user.ResetPasswordParam;

@Service
public class UserServiceImpl implements UserService {

	private static final String IDP = "IDP";

	@Autowired
	private Facade facade;

	@Override
	public User getUserByName(String username) {
		return this.facade.getService(UserRepo.class).getUserByEmail(username);
	}

	@Override
	public User login(String userName, String password)
			throws LocatableBusinessException {
		if (StringUtils.isEmpty(userName)) {
			throw new LocatableBusinessException(
					Saml2ErrorCodes.Login_UserName_IsEmpty, "username");
		}

		if (StringUtils.isEmpty(password)) {
			throw new LocatableBusinessException(
					Saml2ErrorCodes.Login_Password_IsEmpty, "password");
		}

		User user = this.facade.getService(UserRepo.class).getUserByEmail(
				userName);

		if (user == null) {
			throw new LocatableBusinessException(
					Saml2ErrorCodes.Login_UserName_IsEmpty, "username");
		}

		if (!StringUtils.equals(user.getPassword(), password)) {
			throw new LocatableBusinessException(
					Saml2ErrorCodes.Login_Password_Invalid, "password");
		}

		if (user.isLocked()) {
			throw new LocatableBusinessException(
					Saml2ErrorCodes.Account_Is_Locked, "username");
		}

		return user;
	}

	@Override
	public void createUser(User user) {
		this.facade.getService(UserRepo.class).save(user);

	}

	@Transactional
	@Override
	public void changePassword(Long userId, String oldPassword,
			String newPassword) throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserById(userId);
		if (user == null) {
			throw new BusinessException(ErrorCode.User_Not_Exists);
		}

		if (!StringUtils.equals(oldPassword, user.getPassword())) {
			throw new BusinessException(
					UserErrorCodes.Change_Password_Old_Password_Invalid);
		}

		user.setPassword(newPassword);
		this.facade.getService(UserRepo.class).save(user);
	}

	/*
	 * For internal api call
	 * 
	 * @see
	 * com.huoyun.idp.user.UserService#createUser(com.huoyun.idp.internal.api
	 * .user.CreateUserParam)
	 */
	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public User createUser(CreateUserParam createUserParam)
			throws BusinessException {
		Tenant tenant = this.facade.getService(TenantRepo.class)
				.getTenantByTenantCode(createUserParam.getTenantCode());
		if (tenant == null) {
			throw new BusinessException(
					UserErrorCodes.Create_User_Failed_Due_To_Tenant_Not_Exists);
		}

		this.checkUserExistsBeforeCreate(createUserParam.getEmail(),
				createUserParam.getPhone());

		User user = new User();
		user.setTenant(tenant);
		user.setEmail(createUserParam.getEmail());
		user.setUserName(createUserParam.getUserName());
		user.setPhone(createUserParam.getPhone());
		user.setActiveCode(UUID.randomUUID().toString());
		user.setActiveDate(DateTime.now());
		this.facade.getService(UserRepo.class).save(user);

		this.sendUserInitPasswordMail(user);

		return user;
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void deleteUser(DeleteUserParam deleteUserParam)
			throws BusinessException {
		Tenant tenant = this.facade.getService(TenantRepo.class)
				.getTenantByTenantCode(deleteUserParam.getTenantCode());
		if (tenant == null) {
			throw new BusinessException(
					UserErrorCodes.Delete_User_Failed_Due_To_Tenant_Not_Exists);
		}

		this.facade.getService(UserRepo.class).deleteUserByEmailAndTenant(
				deleteUserParam.getEmail(), tenant);
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public User createUser(Tenant tenant, CreateTenantParam tenantParam)
			throws BusinessException {
		this.checkUserExistsBeforeCreate(tenantParam.getEmail(),
				tenantParam.getPhone());

		User user = new User();
		user.setTenant(tenant);
		user.setEmail(tenantParam.getEmail());
		user.setPhone(tenantParam.getPhone());
		user.setUserName(tenantParam.getUserName());
		user.setActiveCode(UUID.randomUUID().toString());
		user.setActiveDate(DateTime.now());
		return this.facade.getService(UserRepo.class).save(user);
	}
	
	@Override
	public void updateUser(UpdateUserParam updateUserParam)
			throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserById(updateUserParam.getUserId());
		user.setLocked(updateUserParam.isLocked());
		this.facade.getService(UserRepo.class).save(user);
	}

	@Override
	public void verifyActiveCode(String activeCode) throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserByActiveCode(
				activeCode);

		if (user == null) {
			throw new BusinessException(UserErrorCodes.Active_Code_Not_Exists);
		}

		if (user.getActive()) {
			throw new BusinessException(UserErrorCodes.User_Already_Actived);
		}

		if (user.getActiveDate().plusDays(1).isBeforeNow()) {
			throw new BusinessException(UserErrorCodes.Active_Code_Is_Expired);
		}
	}

	@Override
	public void initPassword(InitPasswordParam initPasswordParam)
			throws BusinessException {
		this.verifyActiveCode(initPasswordParam.getActiveCode());
		User user = this.facade.getService(UserRepo.class).getUserByActiveCode(
				initPasswordParam.getActiveCode());
		user.setPassword(initPasswordParam.getPassword());
		user.setActive(true);
		this.facade.getService(UserRepo.class).save(user);
	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void requestForgetPassword(ForgetPasswordParam forgetPasswordParam)
			throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserByEmail(
				forgetPasswordParam.getEmail());
		if (user == null) {
			throw new BusinessException(UserErrorCodes.User_Not_Exists);
		}

		this.facade.getService(ForgetPasswordHistoryRepo.class)
				.deactiveRequests(user);
		ForgetPasswordHistory history = new ForgetPasswordHistory();
		history.setUser(user);
		history.setRequestCode(UUID.randomUUID().toString());
		history.setRequestDate(DateTime.now());

		this.facade.getService(ForgetPasswordHistoryRepo.class).save(history);

		String forgetPasswordLink = this.combinePath(this.getIDPDomain(),
				"resetPassword.html?requestCode=" + history.getRequestCode());
		EmailTemplate template = new EmailTemplateImpl(
				EmailTemplateNames.User_Request_Change_Password);
		template.setVariable("user", user);
		template.setVariable("history", history);
		template.setVariable("link", forgetPasswordLink);
		this.facade.getService(EmailService.class).send(user.getEmail(),
				template);
	}

	@Override
	public void verifyChangePasswordRequestCode(String requestCode)
			throws BusinessException {
		ForgetPasswordHistory history = this.facade.getService(
				ForgetPasswordHistoryRepo.class).getHistoryByRequestCode(
				requestCode);
		if (history == null) {
			throw new BusinessException(
					UserErrorCodes.Change_Password_Request_Code_Not_Exists);
		}

		if (!history.isActive()) {
			throw new BusinessException(
					UserErrorCodes.Change_Password_Request_Code_Invalid);
		}

		if (history.getRequestDate().plusDays(1).isBeforeNow()) {
			throw new BusinessException(
					UserErrorCodes.Change_Password_Request_Code_Is_Expired);
		}

	}

	@Transactional(rollbackFor = BusinessException.class)
	@Override
	public void resetPassword(ResetPasswordParam resetPasswordParam)
			throws BusinessException {
		this.verifyChangePasswordRequestCode(resetPasswordParam
				.getRequestCode());

		ForgetPasswordHistory history = this.facade.getService(
				ForgetPasswordHistoryRepo.class).getHistoryByRequestCode(
				resetPasswordParam.getRequestCode());
		history.setActive(false);
		this.facade.getService(ForgetPasswordHistoryRepo.class).save(history);
		User user = history.getUser();
		user.setPassword(resetPasswordParam.getPassword());
		this.facade.getService(UserRepo.class).save(user);
	}

	@Override
	public void sendUserInitPasswordMail(User user) throws BusinessException {
		String activeLink = this.combinePath(this.getIDPDomain(),
				"initPassword.html?activeCode=" + user.getActiveCode());
		EmailTemplate template = new EmailTemplateImpl(
				EmailTemplateNames.User_Set_Init_Password);
		template.setVariable("user", user);
		template.setVariable("link", activeLink);
		this.facade.getService(EmailService.class).send(user.getEmail(),
				template);
	}
	


	private void checkUserExistsBeforeCreate(String email, String phone)
			throws BusinessException {
		boolean userExists = this.facade.getService(UserRepo.class).exists(
				email, phone);
		if (userExists) {
			throw new BusinessException(
					UserErrorCodes.Create_User_Failed_Due_To_User_Exists);
		}
	}

	private String getIDPDomain() throws BusinessException {
		Domain domain = this.facade.getService(DomainService.class)
				.getDomainByName(IDP);
		if (domain == null) {
			throw new BusinessException(UserErrorCodes.IDP_Domain_Is_Empty);
		}

		return domain.getUrl();
	}

	private String combinePath(String firstPath, String secondPath) {
		StringBuilder builder = new StringBuilder();
		builder.append(firstPath);
		if (!StringUtils.endsWith(firstPath, "/")) {
			builder.append("/");
		}

		if (StringUtils.startsWith(secondPath, "/")) {
			builder.append(secondPath.substring(1));
		} else {
			builder.append(secondPath);
		}

		return builder.toString();
	}



}
