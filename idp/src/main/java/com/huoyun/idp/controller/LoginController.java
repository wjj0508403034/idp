package com.huoyun.idp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import com.huoyun.idp.common.Facade;

@Controller
@RequestMapping
public class LoginController {

	@Autowired
	private Facade facade;

	@RequestMapping(value = "/login.html", method = RequestMethod.GET)
	public String loginPage() {
		return "login/login";
	}
}
