package com.huoyun.idp.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;

import com.huoyun.idp.common.BusinessException;
import com.huoyun.idp.common.ErrorCode;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.controller.login.LoginData;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.user.repository.UserRepo;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private Facade facade;

	@Override
	public LoginData login(LoginParam loginParam) throws BusinessException {
		User user = this.facade.getService(UserRepo.class).getUserByEmail(
				loginParam.getEmail());
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
}
