package com.huoyun.idp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.huoyun.idp.trial.TrialService;
import com.huoyun.idp.view.user.UserViewErrorCodes;

@Controller
@RequestMapping
public class TrialContoller {

	private static final String View_Name_Trial = "/view/trial";
	private static final String View_Name_Trial_Success = "/view/trial_success";

	@Autowired
	private TrialService trialService;

	@RequestMapping(value = "/trial.html", method = RequestMethod.GET)
	public String trialPage(Model model) {
		model.addAttribute("trialParam", new TrialParam());
		return View_Name_Trial;
	}

	@RequestMapping(value = "/trial", method = RequestMethod.POST)
	public String requestTrial(TrialParam trialParam, Model model) {
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);

		if (StringUtils.isEmpty(trialParam.getCompanyName())) {
			errors.put("companyName",
					UserViewErrorCodes.Trial_Company_Name_IsEmpty);
			return View_Name_Trial;
		}
		
		if (StringUtils.isEmpty(trialParam.getContactPerson())) {
			errors.put("contactPerson",
					UserViewErrorCodes.Trial_Contact_Person_IsEmpty);
			return View_Name_Trial;
		}
		
		if (StringUtils.isEmpty(trialParam.getPhone())) {
			errors.put("phone",
					UserViewErrorCodes.Trial_Phone_IsEmpty);
			return View_Name_Trial;
		}
		
		if (StringUtils.isEmpty(trialParam.getEmail())) {
			errors.put("email",
					UserViewErrorCodes.Trial_Email_IsEmpty);
			return View_Name_Trial;
		}

		this.trialService.create(trialParam);
		
		return View_Name_Trial_Success;
	}
}
