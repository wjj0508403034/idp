package com.huoyun.idp.common.api.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.user.UserService;

@Controller
@RequestMapping("/api/")
public class UserController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/changePassword")
	@ResponseBody
	public void changePassword(@RequestBody ChangePasswordParam changePasswordParam) throws BusinessException {
		this.userService.changePassword(changePasswordParam.getUserId(), changePasswordParam.getOldPassword(),
				changePasswordParam.getNewPassword());
	}
}
