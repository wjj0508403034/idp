package com.huoyun.idp.email.impl;

import java.util.Locale;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.huoyun.idp.email.EmailErrorCodes;
import com.huoyun.idp.email.EmailTemplate;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.locale.LocaleService;

public class EmailTemplateImpl implements EmailTemplate {

	private Context context = new Context(Locale.CHINA);
	private String emailName;

	public EmailTemplateImpl(String emailName) {
		this.emailName = emailName;
	}

	@Override
	public String getHtml(TemplateEngine templateEngine) throws BusinessException {
		try {
			return templateEngine.process(this.emailName, context);
		} catch (Exception ex) {
			throw new BusinessException(EmailErrorCodes.Parse_Email_Template_Failed);
		}
	}

	@Override
	public String getSubject(LocaleService localeService) {
		return localeService.getMessage("Mail_Subject_" + this.emailName);
	}

	@Override
	public void setVariable(String name, Object value) {
		this.context.setVariable(name, value);
	}

}
