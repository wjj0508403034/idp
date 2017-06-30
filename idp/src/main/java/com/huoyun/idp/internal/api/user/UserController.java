package com.huoyun.idp.internal.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;

@Controller
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(method = RequestMethod.POST, value = "/changePassword")
	@ResponseBody
	public void changePassword(
			@RequestBody ChangePasswordParam changePasswordParam)
			throws BusinessException {
		this.userService.changePassword(changePasswordParam.getUserId(),
				changePasswordParam.getOldPassword(),
				changePasswordParam.getNewPassword());
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public User createUser(@RequestBody CreateUserParam createUserParam)
			throws BusinessException {
		return this.userService.createUser(createUserParam);
	}
	
	
	@RequestMapping(method = RequestMethod.PATCH)
	@ResponseBody
	public void updateUser(@RequestBody UpdateUserParam updateUserParam)
			throws BusinessException {
		this.userService.updateUser(updateUserParam);
	}

	@RequestMapping(method = RequestMethod.DELETE)
	@ResponseBody
	public void deleteUser(@RequestBody DeleteUserParam deleteUserParam)
			throws BusinessException {
		this.userService.deleteUser(deleteUserParam);
	}
}
