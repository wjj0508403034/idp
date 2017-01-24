package com.huoyun.idp.common.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.user.UserInfo;

@Service
public class FacadeImpl implements Facade {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public <T> T getService(Class<T> requiredType) {
		return this.applicationContext.getBean(requiredType);
	}

	@Override
	public UserInfo getCurrentUserInfo() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			return (UserInfo) authentication.getPrincipal();
		}

		return null;
	}
}
