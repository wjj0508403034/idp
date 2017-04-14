package com.huoyun.idp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.common.BusinessException;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.controller.login.LoginParam;
import com.huoyun.idp.user.UserInfo;

@Controller
@RequestMapping
public class LoginController {

	@Autowired
	private Facade facade;

	@RequestMapping(value = "/login.html", method = RequestMethod.GET)
	public String loginPage() {
		return "login/login";
	}

	@RequestMapping(value = "/user", method = RequestMethod.POST)
	@ResponseBody
	public UserInfo login(@RequestBody LoginParam loginParam)
			throws BusinessException {
		return this.facade.getCurrentUserInfo();
	}
}
