package com.huoyun.idp.email;

import org.thymeleaf.TemplateEngine;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.locale.LocaleService;

public interface EmailTemplate {

	String getHtml(TemplateEngine templateEngine) throws BusinessException;

	void setVariable(String name, Object value);

	String getSubject(LocaleService localeService);
}
