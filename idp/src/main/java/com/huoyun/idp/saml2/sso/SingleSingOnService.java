package com.huoyun.idp.saml2.sso;

import javax.servlet.http.HttpServletRequest;

import org.springframework.ui.Model;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.user.entity.User;

public interface SingleSingOnService {

	boolean isSignOn(HttpServletRequest req);
	
	void processLogin(HttpServletRequest req,String samlRequest,String relayState, Model model) throws BusinessException;
	
	void processLogin(HttpServletRequest req,String samlRequest,String relayState, Model model, User user) throws BusinessException;
}
