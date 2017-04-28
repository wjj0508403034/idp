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
import org.springframework.web.servlet.ModelAndView;

import com.sap.security.saml2.idp.api.AssertionData;
import com.sap.security.saml2.idp.api.SAML2IdPAPI;
import com.huoyun.idp.constants.EndpointsConstants;
import com.huoyun.idp.saml2.SAML2Constants;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.saml2.utils.SAML2BuilderFactory;
import com.huoyun.idp.session.SessionManager;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.utils.RequestUtil;
import com.huoyun.idp.web.ViewConstants;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.sso.SSORequestInfo;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.idp.session.SPSession;
import com.sap.security.saml2.lib.bindings.HTTPPostBinding;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.SAML2Utils;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/saml2/idp")
public class SingleSignOnController {

	private static Logger logger = LoggerFactory
			.getLogger(SingleSignOnController.class);

	private SAML2IdPAPI saml2IdPAPI = SAML2IdPAPI.getInstance();

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private SAML2BuilderFactory saml2BuilderFactory;

	@Autowired
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	@Autowired
	private UserService userService;

	@RequestMapping(value = "/sso", method = RequestMethod.GET)
	public ModelAndView ssoNonSaml2(
			@RequestParam(value = "responseAddress", required = false) String responseAddress,
			@RequestParam(value = "token", required = false) String token,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {

		sessionManager.invalidateLoginSession(httpRequest);

		if (null != sessionManager.getIdPSession(httpRequest)) {
			IdPSession idpSession = sessionManager.getIdPSession(httpRequest);
			User user = sessionManager.getUser(httpRequest);
			if (checkIPChanged(httpRequest, idpSession)) {
				logger.info(
						"User: '{}' has changed IP Address, old IP: '{}', new IP: '{}', try to logout this user",
						user.getId(), idpSession.getClientIP(),
						RequestUtil.getIpAddr(httpRequest));
			}

			return new ModelAndView(responseAddress);

		}

		ModelAndView m = new ModelAndView(ViewConstants.Login_Waitting);
		m.getModel().put(HTTPPostBinding.SAML_REQUEST, null);
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, null);
		if (StringUtils.isNotEmpty(responseAddress)) {
			m.getModel().put(SAML2Constants.RESPONSE_ADDRESS, responseAddress);
		}

		httpResponse.setHeader("Cache-Control", "no-cache");
		return m;
	}

	@RequestMapping(value = "/sso", method = RequestMethod.POST, params = { "SAMLRequest" })
	public ModelAndView sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException {
		httpResponse.setHeader("Cache-Control", "no-cache");

		IdPSession idpSession = sessionManager.getIdPSession(httpRequest);

		if (null != idpSession) {
			return loginWithIDPSession(samlRequest, relayState, httpRequest,
					idpSession, httpResponse);

		}

		return this.getLoginModelAndView(samlRequest, relayState, null);
	}

	private ModelAndView getLoginModelAndView(String samlRequest,
			String relayState, Map<String, String> errors) {
		ModelAndView m = new ModelAndView(ViewConstants.Login);
		m.getModel().put(HTTPPostBinding.SAML_REQUEST, samlRequest);
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, relayState);
		m.getModel().put("errors", errors);
		return m;
	}

	@RequestMapping(value = "/login", method = RequestMethod.POST, params = {
			"SAMLRequest", "username", "password" })
	public ModelAndView sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			Model model) throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException {
		httpResponse.setHeader("Cache-Control", "no-cache");
		logger.info("Start to login, username: {}", username);
		Map<String, String> errors = new HashMap<>();
		if (StringUtils.isBlank(username)) {
			errors.put("username", Saml2ErrorCodes.Login_UserName_IsEmpty);
			return this.getLoginModelAndView(samlRequest, relayState, errors);
		}

		if (StringUtils.isBlank(password)) {
			errors.put("password", Saml2ErrorCodes.Login_Password_IsEmpty);
			return this.getLoginModelAndView(samlRequest, relayState, errors);
		}

		refreshSession(httpRequest);

		SAML2IdPConfiguration configuration = idpConfigurationFactory
				.getDefaultSAML2IdpConfiguration();

		String authnRequestXML = base64decode(samlRequest);
		SSORequestInfo ssoRequestInfo = saml2BuilderFactory
				.validateAuthnRequestHttpBody(configuration, authnRequestXML,
						relayState, httpRequest.getRequestURL().toString());

		User user = this.userService.getUserByName(username);
		if (user == null || !StringUtils.equals(user.getPassword(), password)) {
			errors.put("password", Saml2ErrorCodes.Login_Password_Invalid);
			return this.getLoginModelAndView(samlRequest, relayState, errors);
		}
		
		SPSession spSession = sessionManager.saveLoginSession(httpRequest,
				configuration, ssoRequestInfo, user);

		return this.buildSSOResponseModelAndView(httpRequest, spSession,
				ssoRequestInfo);
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

	private ModelAndView loginWithIDPSession(String samlRequest,
			String relayState, HttpServletRequest httpRequest,
			IdPSession idpSession, HttpServletResponse httpResponse)
			throws SAML2Exception, SAML2ConfigurationException,
			SAML2ErrorResponseException {
		SAML2IdPConfiguration configuration = sessionManager
				.getIdPConfiguration(httpRequest);
		User user = sessionManager.getUser(httpRequest);
		if (checkIPChanged(httpRequest, idpSession)) {
			logger.info(
					"User: '{}' has changed IP Address, old IP: '{}', new IP: '{}', try to logout this user",
					user.getId(), idpSession.getClientIP(),
					RequestUtil.getIpAddr(httpRequest));
		}

		String authnRequestXML = base64decode(samlRequest);
		SSORequestInfo ssoRequestInfo = saml2BuilderFactory
				.validateAuthnRequestHttpBody(configuration, authnRequestXML,
						relayState, httpRequest.getRequestURL().toString());
		SPSession spSession = sessionManager.saveLoginSession(httpRequest,
				ssoRequestInfo);
		logger.info(
				"User: '{}' had IDPSession,  successfully login IDP, client ip: '{}', from: '{}'",
				user.getId(), RequestUtil.getIpAddr(httpRequest),
				spSession.getSPName());
		return buildSSOResponseModelAndView(httpRequest, spSession,
				ssoRequestInfo);
	}

	private ModelAndView buildSSOResponseModelAndView(
			HttpServletRequest httpRequest, SPSession spSession,
			SSORequestInfo ssoRequestInfo) throws SAML2Exception,
			SAML2ConfigurationException {
		ModelAndView m = new ModelAndView(ViewConstants.Login_Waitting);
		IdPSession idpSession = sessionManager.getIdPSession(httpRequest);
		SAML2IdPConfiguration configuration = sessionManager
				.getIdPConfiguration(httpRequest);
		AssertionData data = new com.sap.security.saml2.idp.api.AssertionData(
				idpSession.getSubjectId());
		data.setSessionIndex(spSession.getSessionIndex());

		addSessionIndexInLandscape(httpRequest, m, idpSession);

		SAML2Response r = saml2IdPAPI.createSSOResponse(configuration,
				spSession.getSPName(), data);
		m.getModel().put(HTTPPostBinding.SAML_RESPONSE,
				SAML2Utils.encodeBase64AsString(r.generate()));
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE,
				ssoRequestInfo.getRelayState());
		m.getModel().put(SAML2Constants.DESTINATION, r.getDestination());
		return m;
	}

	private void addSessionIndexInLandscape(HttpServletRequest httpRequest,
			ModelAndView m, IdPSession idpSession) {
		if (httpRequest.getSession().getAttribute("session_index") != null) {
			Object sessionIndex = httpRequest.getSession().getAttribute(
					"session_index");
			m.getModel().put("session_index", sessionIndex);
		} else {
			String sessionIndex = Integer.toHexString(idpSession.hashCode());
			httpRequest.getSession()
					.setAttribute("session_index", sessionIndex);
			m.getModel().put("session_index", sessionIndex);
		}
	}

	private String base64decode(String samlRequest) throws SAML2Exception {
		try {
			String authnRequestXML = SAML2Utils
					.decodeBase64AsString(samlRequest);

			return authnRequestXML;
		} catch (SAML2Exception e) {
			logger.warn(
					"decode samlrequest failed, samlrequest: {}, exception: {}",
					samlRequest, e);
			throw e;
		}
	}

	private boolean checkIPChanged(HttpServletRequest httpRequest,
			IdPSession idpSession) {

		String clientIp = RequestUtil.getIpAddr(httpRequest);
		String ipInSession = idpSession.getClientIP();
		if (StringUtils.isEmpty(ipInSession)) {
			return false;
		}

		if (StringUtils.isEmpty(clientIp)) {
			return false;
		}
		return !ipInSession.equals(clientIp);
	}
}
