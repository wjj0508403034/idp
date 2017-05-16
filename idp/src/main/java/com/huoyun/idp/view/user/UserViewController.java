package com.huoyun.idp.view.user;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.locale.LocaleService;
import com.huoyun.idp.user.UserService;

@Controller
@RequestMapping
public class UserViewController {
	private static final String View_Name_Init_Password = "/view/init_password";
	private static final String View_Name_General_Error = "/view/general_error";

	private static Logger LOGGER = LoggerFactory.getLogger(UserViewController.class);

	@Autowired
	private UserService userService;

	@Autowired
	private LocaleService localService;

	@RequestMapping(value = "/initPassword.html", method = RequestMethod.GET)
	public String initPasswordPage(@RequestParam(name = "activeCode", required = false) String activeCode,
			Model model) {
		try {
			this.userService.verifyActiveCode(activeCode);
		} catch (BusinessException ex) {
			LOGGER.error("Verify active code failed.", ex);
			String errorMessage = localService.getErrorMessage(ex.getCode());
			model.addAttribute("errorMessage", errorMessage);

			return View_Name_General_Error;
		}

		InitPasswordParam param = new InitPasswordParam();
		param.setActiveCode(activeCode);
		model.addAttribute("initPasswordForm", param);
		return View_Name_Init_Password;
	}

	@RequestMapping(value = "/set_init_password", method = RequestMethod.POST)
	public String setInitPassword(InitPasswordParam initPasswordForm, Model model) {
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);

		if (StringUtils.isEmpty(initPasswordForm.getPassword())) {
			errors.put("password", InitPasswordErrorCodes.Init_Password_Password_IsEmpty);
			return View_Name_Init_Password;
		}

		if (StringUtils.isEmpty(initPasswordForm.getRepeatPassword())) {
			errors.put("repeatPassword", InitPasswordErrorCodes.Init_Password_Repeat_Password_IsEmpty);
			return View_Name_Init_Password;
		}

		if (!StringUtils.equals(initPasswordForm.getPassword(), initPasswordForm.getRepeatPassword())) {
			errors.put("repeatPassword", InitPasswordErrorCodes.Init_Password_Password_NotMatch);
			return View_Name_Init_Password;
		}

		try {
			this.userService.initPassword(initPasswordForm);
		} catch (BusinessException ex) {
			LOGGER.error("Set init password failed.", ex);
			String errorMessage = localService.getErrorMessage(ex.getCode());
			errors.put("errorMessage", errorMessage);
			return View_Name_Init_Password;
		}

		return "redirect: /saml2/idp/sso";
	}
}
