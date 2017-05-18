package com.huoyun.idp.locale;

import org.springframework.context.MessageSource;

public interface LocaleService {

	String getMessage(String key);

	String getErrorMessage(String errorCode);

	String getMessage(String key, Object[] objs);

	MessageSource getMessageSource();
}
