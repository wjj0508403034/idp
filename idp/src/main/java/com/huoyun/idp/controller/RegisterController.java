package com.huoyun.idp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;

@Controller
@RequestMapping
public class RegisterController {

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/register.html", method = RequestMethod.GET)
	public String registerPage() {
		return "/register";
	}

	@RequestMapping(value = "/register", method = RequestMethod.POST)
	@ResponseBody
	public void register(@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam("repeatPassword") String repeatPassword) {

		User user = new User();
		user.setEmail(username);
		user.setPassword(password);
		
		this.userService.createUser(user);
	}
}
