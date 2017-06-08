package com.huoyun.idp.saml2.sso.impl;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import com.huoyun.idp.common.Facade;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.saml2.SAML2Constants;
import com.huoyun.idp.saml2.Saml2ErrorCodes;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.saml2.controller.LoginData;
import com.huoyun.idp.saml2.sso.SingleSingOnService;
import com.huoyun.idp.saml2.utils.SAML2BuilderFactory;
import com.huoyun.idp.session.SessionManager;
import com.huoyun.idp.user.entity.User;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.sso.SSORequestInfo;
import com.sap.security.saml2.idp.api.AssertionData;
import com.sap.security.saml2.idp.api.SAML2IdPAPI;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.idp.session.SPSession;
import com.sap.security.saml2.lib.bindings.HTTPPostBinding;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.SAML2Utils;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2Response;

public class SingleSingOnServiceImpl implements SingleSingOnService {

	private static Logger LOGGER = LoggerFactory
			.getLogger(SingleSingOnServiceImpl.class);
	private static final String SessionIndex = "session_index";
	private Facade facade;
	private SessionManager sessionManager;
	private SAML2IdPAPI saml2IdPAPI = SAML2IdPAPI.getInstance();
	private SAML2BuilderFactory saml2BuilderFactory;
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	public SingleSingOnServiceImpl(Facade facade) {
		this.facade = facade;
		this.sessionManager = this.facade.getService(SessionManager.class);
		this.saml2BuilderFactory = this.facade
				.getService(SAML2BuilderFactory.class);
		this.idpConfigurationFactory = this.facade
				.getService(SAML2IdPConfigurationFactory.class);
	}

	@Override
	public boolean isSignOn(HttpServletRequest req) {
		return this.sessionManager.getIdPSession(req) != null;
	}

	/*
	 * User login with session
	 */
	@Override
	public void processLogin(HttpServletRequest req, String samlRequest,
			String relayState, Model model) throws BusinessException {
		LOGGER.info("User login with session...");
		User user = sessionManager.getUser(req);
		SAML2IdPConfiguration idpConfig = sessionManager
				.getIdPConfiguration(req);
		this.process(req, samlRequest, relayState, model, user, idpConfig);
		LOGGER.info("User login with session success.");
	}

	/*
	 * User first login
	 */
	@Override
	public void processLogin(HttpServletRequest req, String samlRequest,
			String relayState, Model model, User user) throws BusinessException {
		LOGGER.info("User first login ...");
		SAML2IdPConfiguration idpConfig = idpConfigurationFactory
				.getDefaultSAML2IdpConfiguration();

		this.process(req, samlRequest, relayState, model, user, idpConfig);
		LOGGER.info("User first login success.");
	}

	private void process(HttpServletRequest req, String samlRequest,
			String relayState, Model model, User user,
			SAML2IdPConfiguration idpConfig) throws BusinessException {
		try {
			SSORequestInfo ssoRequestInfo = this.validateAuthnRequest(req,
					samlRequest, relayState);

			SAML2Response saml2Response = this.createSSOResponse(req,
					idpConfig, ssoRequestInfo, user);
			String saml2ResponseString = SAML2Utils
					.encodeBase64AsString(saml2Response.generate());
			model.addAttribute(SessionIndex, this.getOrCreateSessionIndex(req));
			model.addAttribute(HTTPPostBinding.SAML_RESPONSE,
					saml2ResponseString);
			model.addAttribute(HTTPPostBinding.SAML_RELAY_STATE, relayState);
			model.addAttribute(SAML2Constants.DESTINATION,
					saml2Response.getDestination());

		} catch (Exception ex) {
			throw new BusinessException(
					Saml2ErrorCodes.Saml_login_Process_Error);
		}
	}

	private SAML2Response createSSOResponse(HttpServletRequest req,
			SAML2IdPConfiguration idpConfig, SSORequestInfo ssoRequestInfo,
			User user) throws SAML2Exception, SAML2ConfigurationException {

		SPSession spSession = sessionManager.saveLoginSession(req, idpConfig,
				ssoRequestInfo, user);

		LoginData loginData = this.getLoginData(user);
		IdPSession idpSession = sessionManager.getIdPSession(req);
		AssertionData data = new AssertionData(idpSession.getSubjectId());
		data.setSessionIndex(spSession.getSessionIndex());
		data.setAttributes(loginData.getValue());

		idpConfig = sessionManager.getIdPConfiguration(req);
		return saml2IdPAPI.createSSOResponse(idpConfig, spSession.getSPName(),
				data);
	}

	private SSORequestInfo validateAuthnRequest(HttpServletRequest req,
			String samlRequest, String relayState)
			throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException {
		String authnRequestXML = SAML2Utils.decodeBase64AsString(samlRequest);
		SAML2IdPConfiguration idpDefaultConfig = idpConfigurationFactory
				.getDefaultSAML2IdpConfiguration();

		return saml2BuilderFactory.validateAuthnRequestHttpBody(
				idpDefaultConfig, authnRequestXML, relayState, req
						.getRequestURL().toString());
	}

	private LoginData getLoginData(User user) {
		LoginData loginData = new LoginData();
		loginData.set("tenantCode", user.getTenant().getTenantCode());
		return loginData;
	}

	private String getOrCreateSessionIndex(HttpServletRequest req) {
		IdPSession idpSession = sessionManager.getIdPSession(req);
		String sessionIndex = (String) req.getSession().getAttribute(
				SessionIndex);
		if (StringUtils.isEmpty(sessionIndex)) {
			return sessionIndex;
		}

		return Integer.toHexString(idpSession.hashCode());
	}
}
