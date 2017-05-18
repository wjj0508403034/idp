package com.huoyun.idp.locale.impl;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;

import com.huoyun.idp.locale.LocaleService;

public class LocaleServiceImpl implements LocaleService {

	private static final Logger LOGGER = LoggerFactory.getLogger(LocaleServiceImpl.class);

	private MessageSource messageSource;

	public LocaleServiceImpl(MessageSource messageSource) {
		this.messageSource = messageSource;
	}

	@Override
	public String getMessage(String key) {
		try {
			return this.messageSource.getMessage(key, null, Locale.CHINA);
		} catch (Exception ex) {
			LOGGER.warn("Not found {} string in localization file", key);
		}
		return null;
	}
	
	@Override
	public MessageSource getMessageSource(){
		return this.messageSource;
	}

	@Override
	public String getMessage(String key, Object[] objs) {
		try {
			return this.messageSource.getMessage(key, objs, Locale.CHINA);
		} catch (Exception ex) {
			LOGGER.warn("Not found {} string in localization file", key);
		}
		return null;
	}

	@Override
	public String getErrorMessage(String errorCode) {
		return this.getMessage("ErrorCode_" + errorCode);
	}

}
