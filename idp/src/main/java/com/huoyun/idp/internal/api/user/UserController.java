package com.huoyun.idp.internal.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.user.UserService;

@Controller
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(method = RequestMethod.POST, value = "/changePassword")
	@ResponseBody
	public void changePassword(@RequestBody ChangePasswordParam changePasswordParam) throws BusinessException {
		this.userService.changePassword(changePasswordParam.getUserId(), changePasswordParam.getOldPassword(),
				changePasswordParam.getNewPassword());
	}

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void createUser(@RequestBody CreateUserParam createUserParam) throws BusinessException {
		this.userService.createUser(createUserParam);
	}
}
