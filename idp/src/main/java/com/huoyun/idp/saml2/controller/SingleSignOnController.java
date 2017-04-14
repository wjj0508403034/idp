package com.huoyun.idp.saml2.controller;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import com.sap.security.saml2.idp.api.AssertionData;
import com.sap.security.saml2.idp.api.SAML2IdPAPI;
import com.huoyun.idp.constants.EndpointsConstants;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.saml2.utils.SAML2BuilderFactory;
import com.huoyun.idp.session.SessionManager;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.utils.RequestUtil;
import com.huoyun.idp.utils.SSOJsonResult;
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

	private static final String WAITING_PATH = "/login/waiting";
	private static final String SSO_DESTINATION = "destination";
	private static final String REQUEST_LOCALE_NAME = "locale";
	public static final String LAST_FAILED_TIME = "LAST_FAILED_TIME";
	public static final String RESPONSE_ADDRESS = "ResponseAddress";
	final static int FAILED_DURATION = 5 * 60 * 1000;

	private SAML2IdPAPI saml2IdPAPI;
	// private SLOService sloService;

	{
		this.saml2IdPAPI = SAML2IdPAPI.getInstance();
		// this.sloService = SLOService.getInstance();
	}

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private SAML2BuilderFactory saml2BuilderFactory;
	
	@Autowired
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	@RequestMapping(value = "/ssojson", method = RequestMethod.GET)
	public ModelAndView ssoNonSaml2(
			@RequestParam(value = "responseAddress", required = false) String responseAddress,
			@RequestParam(value = "token", required = false) String token,
			@CookieValue(value = "PL", required = false) String persistentLogin,
			@RequestParam(value = "locale", required = false) String locale,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException {
		locale = httpRequest.getSession().getAttribute("locale").toString();
		ModelAndView mv = null;

		if (StringUtils.isNotEmpty(locale)) {
			try {
				LocaleUtils.toLocale(locale);
			} catch (IllegalArgumentException e) {
				logger.error("locale: " + locale
						+ " is invalid, remove and redirect", e);

				String requestURL = httpRequest.getRequestURL().toString();
				String queryString = httpRequest.getQueryString();
				String redirectUrl = queryString.replaceAll(
						"(&*)locale=([^&]$|[^&]*)", "");

				httpResponse.sendRedirect(requestURL + "?" + redirectUrl);
				return null;
			}
		}

		httpResponse.addHeader("MobileNativeSupport", "true");
		clearSession(token, persistentLogin, httpRequest);

		if (null != sessionManager.getIdPSession(httpRequest)) {
			mv = buildViewWtihSession(responseAddress, locale, httpRequest,
					httpResponse);

		} else {
			mv = buildLoginModelAndView(httpRequest, httpResponse, null, null,
					"", responseAddress);
		}

		return mv;
	}

	public ModelAndView buildLoginModelAndView(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String samlRequest,
			String relayState, String errorMsg, String responseAddress) {
		ModelAndView m = new ModelAndView("/login/login1");
		m.getModel().put(HTTPPostBinding.SAML_REQUEST, samlRequest);
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, relayState);
		if (StringUtils.isNotEmpty(responseAddress)) {
			m.getModel().put(RESPONSE_ADDRESS, responseAddress);
		}

		String solutionPortalAddress = "http://localhost:3002";
		m.getModel().put("portal", solutionPortalAddress);
		m.getModel().put("persistentLoginEnabled", false);
		// if (configurationService.isRememberMeEnabled()) {
		// String userAgent = httpRequest.getHeader("User-Agent");
		// if (userAgent.contains("Mobile")) {
		// m.getModel().put("rememberme",
		// configurationService.isRememberMeClickedForMobile());
		// } else {
		// m.getModel().put("rememberme",
		// configurationService.isRememberMeClieckedForDesktop());
		// }
		// m.getModel().put("persistentDays",
		// configurationService.getPersistentLoginDuration());
		// }
		m.getModel().put("messageKey", errorMsg);
		// m.getModel().put(REQUEST_LOCALE_NAME,
		// validateLocale(httpRequest.getParameter(REQUEST_LOCALE_NAME)));
		Long lastFailedTime = (Long) httpRequest.getSession().getAttribute(
				LAST_FAILED_TIME);
		Long now = System.currentTimeMillis();
		// if (lastFailedTime != null && now - lastFailedTime <=
		// UserService.FAILED_DURATION) {
		// m.getModel().put(VALIDATE_CAPTCHA, true);
		// } else {
		// m.getModel().put(VALIDATE_CAPTCHA, false);
		// httpRequest.getSession().removeAttribute(VALIDATE_CAPTCHA);
		// httpRequest.getSession().removeAttribute(LAST_FAILED_TIME);
		// }

		httpResponse.setHeader("Cache-Control", "no-cache");
		return m;
	}

	private ModelAndView buildViewWtihSession(String responseAddress,
			String locale, HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		ModelAndView mv = null;
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

	@RequestMapping(value = "/ssojson", method = RequestMethod.POST, params = { "SAMLRequest" })
	public ModelAndView sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			@RequestParam(value = "locale", required = false) String locale,
			@CookieValue(value = "PL", required = false) String persistentLogin,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException {

		IdPSession idpSession = sessionManager.getIdPSession(httpRequest);

		if (null != idpSession) {
			return loginWithIDPSession(samlRequest, relayState, httpRequest,
					idpSession, httpResponse);

		}
		return buildLoginModelAndView(httpRequest, httpResponse, samlRequest,
				relayState, null);
	}

	@RequestMapping(value = "/ssojson", method = RequestMethod.POST, params = {
			"SAMLRequest", "username", "password" })
	public ModelAndView sso(
			@RequestParam("SAMLRequest") String samlRequest,
			@RequestParam(value = "RelayState", required = false) String relayState,
			@RequestParam("username") String username,
			@RequestParam("password") String password,
			@RequestParam(value = "captcha", required = false) String captcha,
			@RequestParam(value = "locale", required = false) String locale,
			@RequestParam(value = "rememberMe", required = false) String rememberMe,
			HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException {
		logger.info("sso start...");

		locale = httpRequest.getSession().getAttribute("locale").toString();
		//relayState = syncLocal2RelayState(locale, relayState);

		httpResponse.addHeader("MobileNativeSupport", "true");
		// sessionManager.invalidateLoginSession(httpRequest);
		refreshSession(httpRequest);

		SAML2IdPConfiguration configuration = idpConfigurationFactory
				.getDefaultSAML2IdpConfiguration();

		String authnRequestXML = base64decode(samlRequest);
		SSORequestInfo ssoRequestInfo = saml2BuilderFactory.validateAuthnRequestHttpBody(
				configuration, authnRequestXML, relayState, httpRequest
						.getRequestURL().toString());

		if (StringUtils.isBlank(username)) {
//			return SSOJsonResult.buildFailure(i18nWorker.getLocalizedMessage(
//					"login.username.empty.error", httpRequest));
		}

		if (StringUtils.isBlank(password)) {
//			return SSOJsonResult.buildFailure(i18nWorker.getLocalizedMessage(
//					"login.password.empty.error", httpRequest));
		}

//		SSOJsonResult result = userService.validaCaptcha(httpRequest, username,
//				captcha);
//		if (result != null) {
//			return result;
//		}

//		UserService.LoginResult loginResult = userService.login(username,
//				password);
//		logger.debug(loginResult.getStatus().toString());
//		SSOJsonResult result = parseLoginResult(samlRequest, relayState, locale, rememberMe,
//				httpRequest, httpResponse, configuration, ssoRequestInfo
//				);
		User user = new User();
        user.setId(123456l);
		 SPSession spSession = sessionManager.saveLoginSession(httpRequest, configuration, ssoRequestInfo,
				 user);



        // log(loginResult.getUser().getId(), RequestUtil.getIpAddr(httpRequest), spSession.getSPName());
        // auditLog(loginResult.getUser().getNamedUserBinding().getUsername());
		return this.buildSSOResponseModelAndView(httpRequest, spSession, ssoRequestInfo);
		//logger.info("sso end...");
		//return result;
	}

	
	private SSOJsonResult parseLoginResult(String samlRequest, String relayState, String locale, String rememberMe,
            HttpServletRequest httpRequest, HttpServletResponse httpResponse, SAML2IdPConfiguration configuration,
            SSORequestInfo ssoRequestInfo)
            throws SAML2Exception, SAML2ConfigurationException {
        SSOJsonResult result;
        //if (loginResult.getStatus().equals(LoginStatus.Ok)) {
            //httpRequest.getSession().removeAttribute(SSOJSONAPI.VALIDATE_CAPTCHA);
            //httpRequest.getSession().removeAttribute(SSOJSONAPI.FAILED_LOGIN_NUM);
        User user = new User();
        user.setId(123456l);
            SPSession spSession = sessionManager.saveLoginSession(httpRequest, configuration, ssoRequestInfo,user
                    );

//            logger.info("Request rememberMe is" + rememberMe + ";Global setting RememberMeEnabled value is "
//                    + configurationService.isRememberMeEnabled() + ".");
//            if (StringUtils.isNotEmpty(rememberMe) && !rememberMe.equalsIgnoreCase("false")
//                    && configurationService.isRememberMeEnabled()) {
//                ssoApi.setRememberMeToken(httpResponse, loginResult);
//                logger.info("Set PL value sucessfully.");
//            }

            //logger.info(USER_LOGIN_IDP_WITH_IP_LOG_FORMAT, loginResult.getUser().getId(),
            //        loginResult.getUser().getEmail(), RequestUtil.getIpAddr(httpRequest));
            //auditLogger.info(USER_LOGIN_SUCCESSFUL_LOG_FORMAT, loginResult.getUser().getId(),
            //        loginResult.getUser().getNamedUserBinding().getUsername());
            result = buildSSOResponse(httpRequest, spSession, ssoRequestInfo, locale);
//        } else if (loginResult.getStatus().equals(LoginStatus.NeedPasswordChange)) {
//            sessionManager.saveLoginSession(httpRequest, configuration, ssoRequestInfo, loginResult.getUser());
//            logger.info(USER_PASSWORD_CHANGE_REQUIRED_LOG_FORMAT, loginResult.getUser().getId(),
//                    RequestUtil.getIpAddr(httpRequest));
//            //auditLogger.info(USER_PASSWORD_CHANGE_LOG_FORMAT,
//            //        loginResult.getUser().getNamedUserBinding().getUsername());
//            ModelAndView mv = ssoApi.buildChangePasswordModelAndView(httpRequest, samlRequest, relayState, null, null);
//            result = SSOJsonResult.buildNextPage(mv.getViewName(), mv.getModel());
//            addTokenToHeader(httpRequest, httpResponse);
//        } else {
//            result = handleFailedLogin(httpRequest, loginResult);
//        }
        return result;
    }
	
	public SSOJsonResult buildSSOResponse(HttpServletRequest httpRequest, SPSession spSession,
            SSORequestInfo ssoRequestInfo, String locale) throws SAML2Exception, SAML2ConfigurationException {
        IdPSession idpSession = sessionManager.getIdPSession(httpRequest);
        SAML2IdPConfiguration configuration = sessionManager.getIdPConfiguration(httpRequest);
        AssertionData data = new AssertionData(idpSession.getSubjectId());
        data.setSessionIndex(spSession.getSessionIndex());

        SAML2Response r = saml2IdPAPI.createSSOResponse(configuration, spSession.getSPName(), data);

        Map<String, Object> redirectParam = new HashMap<String, Object>();
        redirectParam.put(HTTPPostBinding.SAML_RESPONSE, SAML2Utils.encodeBase64AsString(r.generate()));
        redirectParam.put(HTTPPostBinding.SAML_RELAY_STATE, ssoRequestInfo.getRelayState());
        // redirectParam.put(SSO_DESTINATION, r.getDestination());
        addSessionIndexInLandscape(httpRequest, redirectParam, idpSession);
        String redirectURL = r.getDestination();
//        if (validateLocale(locale) != null) {
//            if (redirectURL.indexOf("?") != -1) {
//                redirectURL += "&locale=" + locale;
//            } else {
//                redirectURL += "?locale=" + locale;
//            }
//        }
        
        return SSOJsonResult.buildRedirect(HtmlUtils.htmlEscape(redirectURL), "POST", redirectParam);
    }
	
    private void addSessionIndexInLandscape(HttpServletRequest httpRequest, Map<String, Object> redirectParam,
            IdPSession idpSession) {
        if (httpRequest.getSession().getAttribute("session_index") != null) {
            Object sessionIndex = httpRequest.getSession().getAttribute("session_index");
            redirectParam.put("session_index", sessionIndex);
        } else {
            String sessionIndex = Integer.toHexString(idpSession.hashCode());
            httpRequest.getSession().setAttribute("session_index", sessionIndex);
            redirectParam.put("session_index", sessionIndex);
        }
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
            if(!StringUtils.isEmpty(key)){
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

	private void clearSession(String token, String persistentLogin,
			HttpServletRequest httpRequest) {
		if (StringUtils.isNotBlank(token)
				|| StringUtils.isNotBlank(persistentLogin)) {
			sessionManager.invalidateLoginSession(httpRequest);
		}
	}

	public ModelAndView buildLoginModelAndView(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, String samlRequest,
			String relayState, String errorMsg) {
		ModelAndView m = new ModelAndView("/login/login1");
		m.getModel().put(HTTPPostBinding.SAML_REQUEST, samlRequest);
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, relayState);

		// m.getModel().put("persistentLoginEnabled",
		// configurationService.isRememberMeEnabled());
		// if (configurationService.isRememberMeEnabled()) {
		// String userAgent = httpRequest.getHeader("User-Agent");
		// if (userAgent != null && userAgent.contains("Mobile")) {
		// m.getModel().put("rememberme",
		// configurationService.isRememberMeClickedForMobile());
		// } else {
		// m.getModel().put("rememberme",
		// configurationService.isRememberMeClieckedForDesktop());
		// }
		// m.getModel().put("persistentDays",
		// configurationService.getPersistentLoginDuration());
		// }
		m.getModel().put("messageKey", errorMsg);
		m.getModel().put(REQUEST_LOCALE_NAME,
				httpRequest.getParameter(REQUEST_LOCALE_NAME));
		Long lastFailedTime = (Long) httpRequest.getSession().getAttribute(
				LAST_FAILED_TIME);
		Long now = System.currentTimeMillis();
		// if (lastFailedTime != null && now - lastFailedTime <=
		// FAILED_DURATION) {
		// m.getModel().put(VALIDATE_CAPTCHA, true);
		// } else {
		// m.getModel().put(VALIDATE_CAPTCHA, false);
		// httpRequest.getSession().removeAttribute(VALIDATE_CAPTCHA);
		// httpRequest.getSession().removeAttribute(LAST_FAILED_TIME);
		// }

		httpResponse.setHeader("Cache-Control", "no-cache");
		return m;
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

		// UserService.LoginStatus loginStatus = userService
		// .checkCustomerServiceUnitTenantStatus(user);
		// if (loginStatus.equals(LoginStatus.Ok)) {
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
		// } else if (LoginStatus.NeedPasswordChange.equals(loginStatus)) {
		// persisitenceChangePasswdLog(user.getId(),
		// RequestUtil.getIpAddr(httpRequest));
		// changePasswdAuditLog(user.getNamedUserBinding().getUsername());
		// addTokenToHeader(httpRequest, httpResponse);
		// ModelAndView mv = ssoApi.buildChangePasswordModelAndView(
		// httpRequest, samlRequest, relayState, null, null);
		// mv.setViewName("/changepassword_json.jsp");
		// return mv;
		// }
		//
		// return ssoApi.buildLoginModelAndView(httpRequest, httpResponse,
		// samlRequest, relayState, "error." + loginStatus.toString());
	}

	private ModelAndView buildSSOResponseModelAndView(
			HttpServletRequest httpRequest, SPSession spSession,
			SSORequestInfo ssoRequestInfo) throws SAML2Exception,
			SAML2ConfigurationException {
		ModelAndView m = new ModelAndView(WAITING_PATH);
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
		m.getModel().put(SSO_DESTINATION, r.getDestination());
		m.getModel().put(REQUEST_LOCALE_NAME,
				httpRequest.getParameter(REQUEST_LOCALE_NAME));
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
