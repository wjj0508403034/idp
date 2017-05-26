package com.huoyun.idp.saml2.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.huoyun.idp.constants.EndpointsConstants;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.exception.LocatableBusinessException;
import com.huoyun.idp.saml2.SAML2Constants;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.saml2.sso.SingleSingOnService;
import com.huoyun.idp.session.SessionManager;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.web.ViewConstants;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.lib.bindings.HTTPPostBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/saml2/idp")
public class SingleSignOnController {

	private static Logger logger = LoggerFactory
			.getLogger(SingleSignOnController.class);

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	@Autowired
	private UserService userService;

	@Autowired
	private SingleSingOnService singleSingOnService;

	@RequestMapping(value = "/sso", method = RequestMethod.GET)
	public String ssoNonSaml2(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, Model model) throws IOException {

		String redirectUrl = this.idpConfigurationFactory
				.getDefaultSPLocation();
		IdPSession idpSession = sessionManager.getIdPSession(httpRequest);
		if (idpSession != null) {
			User user = sessionManager.getUser(httpRequest);
			if (user != null) {
				return "redirect:" + redirectUrl;
			}
		}

		sessionManager.invalidateLoginSession(httpRequest);
		model.addAttribute(HTTPPostBinding.SAML_REQUEST, null);
		model.addAttribute(HTTPPostBinding.SAML_RELAY_STATE, null);
		model.addAttribute(SAML2Constants.RESPONSE_ADDRESS, redirectUrl);
		httpResponse.setHeader("Cache-Control", "no-cache");
		return ViewConstants.Login_Waitting;
	}

	@RequestMapping(value = "/sso", method = RequestMethod.POST, params = { "SAMLRequest" })
	public String sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Model model) throws BusinessException {
		httpResponse.setHeader("Cache-Control", "no-cache");

		if (this.singleSingOnService.isSignOn(httpRequest)) {
			User user = sessionManager.getUser(httpRequest);
			if (user != null) {
				this.singleSingOnService.processLogin(httpRequest, samlRequest,
						relayState, model);
				return ViewConstants.Login_Waitting;
			}

			sessionManager.invalidateLoginSession(httpRequest);
		}

		model.addAttribute(HTTPPostBinding.SAML_REQUEST, samlRequest);
		model.addAttribute(HTTPPostBinding.SAML_RELAY_STATE, relayState);
		return ViewConstants.Login;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST, params = {
			"SAMLRequest", "username", "password" })
	public String sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Model model) throws BusinessException {
		logger.info("Start to login, username: {}", username);
		
		refreshSession(httpRequest);
		
		httpResponse.setHeader("Cache-Control", "no-cache");
		Map<String, String> errors = new HashMap<>();
		model.addAttribute("errors", errors);
		User user = null;
		try {
			user = this.userService.login(username, password);
		} catch (LocatableBusinessException ex) {
			errors.put(ex.getPath(), ex.getCode());
			model.addAttribute("username", username);
			model.addAttribute(HTTPPostBinding.SAML_REQUEST, samlRequest);
			model.addAttribute(HTTPPostBinding.SAML_RELAY_STATE, relayState);
			return ViewConstants.Login;
		}

		if (StringUtils.isEmpty(samlRequest)) {
			SAML2IdPConfiguration defaultIdpConfig = idpConfigurationFactory
					.getDefaultSAML2IdpConfiguration();
			sessionManager
					.saveLoginSession(httpRequest, defaultIdpConfig, user);
			return "redirect:"
					+ this.idpConfigurationFactory.getDefaultSPLocation();
		}

		this.singleSingOnService.processLogin(httpRequest, samlRequest,
				relayState, model, user);

		return ViewConstants.Login_Waitting;
	}

	private void refreshSession(HttpServletRequest httpRequest) {
		logger.info("refreshSession start...");

		Map<String, Object> oriAttributes = new HashMap<String, Object>();
		HttpSession session = httpRequest.getSession();
		if (null == session) {
			return;
		}

		Enumeration<String> keys = session.getAttributeNames();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (!StringUtils.isEmpty(key)) {
				if (key.equals(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR)) {
					continue;
				}
				Object value = session.getAttribute(key);
				oriAttributes.put(key, value);
			}

		}
		sessionManager.invalidateLoginSession(httpRequest);
		session = httpRequest.getSession(true);
		Set<Map.Entry<String, Object>> entrys = oriAttributes.entrySet();
		for (Map.Entry<String, Object> entry : entrys) {
			session.setAttribute(entry.getKey(), entry.getValue());
		}

		logger.info("refreshSession end...");
	}
}
