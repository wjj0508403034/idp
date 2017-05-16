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
	private static final String View_Name_Forget_Password = "/view/forget_password";
	private static final String View_Name_Forget_Password_Info = "/view/forget_password_send_mail_success";
	private static final String View_Name_General_Error = "/view/general_error";
	private static final String View_Name_Change_Password = "/view/change_password";

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
			errors.put("password", UserViewErrorCodes.Init_Password_Password_IsEmpty);
			return View_Name_Init_Password;
		}

		if (StringUtils.isEmpty(initPasswordForm.getRepeatPassword())) {
			errors.put("repeatPassword", UserViewErrorCodes.Init_Password_Repeat_Password_IsEmpty);
			return View_Name_Init_Password;
		}

		if (!StringUtils.equals(initPasswordForm.getPassword(), initPasswordForm.getRepeatPassword())) {
			errors.put("repeatPassword", UserViewErrorCodes.Init_Password_Password_NotMatch);
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

	@RequestMapping(value = "/forgetPassword.html", method = RequestMethod.GET)
	public String forgetPasswordPage(Model model) {
		model.addAttribute("forgetPasswordParam", new ForgetPasswordParam());
		return View_Name_Forget_Password;
	}

	@RequestMapping(value = "/requestForgetPassword", method = RequestMethod.POST)
	public String requestForgetPassword(ForgetPasswordParam forgetPasswordParam, Model model) {
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);

		if (StringUtils.isEmpty(forgetPasswordParam.getEmail())) {
			errors.put("email", UserViewErrorCodes.Forget_Password_Email_IsEmpty);
			return View_Name_Forget_Password;
		}

		try {
			this.userService.requestForgetPassword(forgetPasswordParam);
		} catch (BusinessException ex) {
			LOGGER.error("Request forget password failed.", ex);
			String errorMessage = localService.getErrorMessage(ex.getCode());
			errors.put("email", errorMessage);
			return View_Name_Forget_Password;
		}

		model.addAttribute("email", forgetPasswordParam.getEmail());
		return View_Name_Forget_Password_Info;
	}

	@RequestMapping(value = "/resetPassword.html", method = RequestMethod.GET)
	public String resetPasswordPage(@RequestParam(name = "requestCode", required = false) String requestCode,
			Model model) {
		try {
			this.userService.verifyChangePasswordRequestCode(requestCode);
		} catch (BusinessException ex) {
			LOGGER.error("Verify change password request code failed.", ex);
			String errorMessage = localService.getErrorMessage(ex.getCode());
			model.addAttribute("errorMessage", errorMessage);

			return View_Name_General_Error;
		}

		ResetPasswordParam param = new ResetPasswordParam();
		param.setRequestCode(requestCode);
		model.addAttribute("resetPasswordParam", param);
		return View_Name_Change_Password;
	}

	@RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
	public String resetPassword(ResetPasswordParam resetPasswordParam, Model model) {
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);

		if (StringUtils.isEmpty(resetPasswordParam.getPassword())) {
			errors.put("password", UserViewErrorCodes.Change_Password_Password_IsEmpty);
			return View_Name_Change_Password;
		}

		if (StringUtils.isEmpty(resetPasswordParam.getRepeatPassword())) {
			errors.put("repeatPassword", UserViewErrorCodes.Change_Password_Repeat_Password_IsEmpty);
			return View_Name_Change_Password;
		}

		if (!StringUtils.equals(resetPasswordParam.getPassword(), resetPasswordParam.getRepeatPassword())) {
			errors.put("repeatPassword", UserViewErrorCodes.Change_Password_Password_NotMatch);
			return View_Name_Change_Password;
		}

		try {
			this.userService.resetPassword(resetPasswordParam);
		} catch (BusinessException ex) {
			LOGGER.error("Reset password failed.", ex);
			String errorMessage = localService.getErrorMessage(ex.getCode());
			model.addAttribute("errorMessage", errorMessage);

			return View_Name_Change_Password;
		}

		return "redirect: /saml2/idp/sso";
	}

}
