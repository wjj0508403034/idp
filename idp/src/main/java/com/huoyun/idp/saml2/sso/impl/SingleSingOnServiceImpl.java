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
import com.sap.security.saml2.lib.common.SAML2ProtocolFactory;
import com.sap.security.saml2.lib.common.SAML2Utils;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2AuthRequest;
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

	@Override
	public void processLogin(HttpServletRequest req, String samlRequest,
			String relayState, Model model) throws BusinessException {
		User user = sessionManager.getUser(req);

		try {
			String authnRequestXML = SAML2Utils
					.decodeBase64AsString(samlRequest);
			SAML2IdPConfiguration idpDefaultConfig = idpConfigurationFactory
					.getDefaultSAML2IdpConfiguration();
		
			SSORequestInfo ssoRequestInfo = saml2BuilderFactory
					.validateAuthnRequestHttpBody(idpDefaultConfig, authnRequestXML,
							relayState, req.getRequestURL().toString());
			SPSession spSession = sessionManager.saveLoginSession(req,
					idpDefaultConfig, ssoRequestInfo, user);

			LoginData loginData = this.getLoginData(user);
			IdPSession idpSession = sessionManager.getIdPSession(req);
			AssertionData data = new AssertionData(idpSession.getSubjectId());
			data.setSessionIndex(spSession.getSessionIndex());
			data.setAttributes(loginData.getValue());
			
			SAML2IdPConfiguration idpConfig = sessionManager
					.getIdPConfiguration(req);
			
			SAML2Response saml2Response = saml2IdPAPI.createSSOResponse(
					idpConfig, spSession.getSPName(), data);
			String saml2ResponseString = SAML2Utils
					.encodeBase64AsString(saml2Response.generate());
			model.addAttribute(SessionIndex,
					this.getOrCreateSessionIndex(req, idpSession));
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
	
	@Override
	public void processLogin(HttpServletRequest req, String samlRequest,
			String relayState, Model model, User user) throws BusinessException {
		try {
			String authnRequestXML = SAML2Utils
					.decodeBase64AsString(samlRequest);
			SAML2IdPConfiguration idpDefaultConfig = idpConfigurationFactory
					.getDefaultSAML2IdpConfiguration();
		
			SSORequestInfo ssoRequestInfo = saml2BuilderFactory
					.validateAuthnRequestHttpBody(idpDefaultConfig, authnRequestXML,
							relayState, req.getRequestURL().toString());
			SPSession spSession = sessionManager.saveLoginSession(req,
					idpDefaultConfig, ssoRequestInfo, user);
//			SPSession spSession = sessionManager.saveLoginSession(req,
//					ssoRequestInfo);

			LoginData loginData = this.getLoginData(user);
			IdPSession idpSession = sessionManager.getIdPSession(req);
			AssertionData data = new AssertionData(idpSession.getSubjectId());
			data.setSessionIndex(spSession.getSessionIndex());
			data.setAttributes(loginData.getValue());
			
			SAML2IdPConfiguration idpConfig = sessionManager
					.getIdPConfiguration(req);
			
			SAML2Response saml2Response = saml2IdPAPI.createSSOResponse(
					idpConfig, spSession.getSPName(), data);
			String saml2ResponseString = SAML2Utils
					.encodeBase64AsString(saml2Response.generate());
			model.addAttribute(SessionIndex,
					this.getOrCreateSessionIndex(req, idpSession));
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

	private LoginData getLoginData(User user) {
		LoginData loginData = new LoginData();
		loginData.set("tenantCode", user.getTenant().getTenantCode());
		return loginData;
	}

	private String getOrCreateSessionIndex(HttpServletRequest req,
			IdPSession idpSession) {
		String sessionIndex = (String) req.getSession().getAttribute(
				SessionIndex);
		if (StringUtils.isEmpty(sessionIndex)) {
			return sessionIndex;
		}

		return Integer.toHexString(idpSession.hashCode());
	}

	public SSORequestInfo validateAuthnRequest(
			SAML2IdPConfiguration configuration, String authnRequestXML,
			String relayState, String recipientUrl) throws SAML2Exception,
			SAML2ConfigurationException, SAML2ErrorResponseException {
		LOGGER.info("validateAuthnRequestHttpBody start...");

		SAML2AuthRequest saml2AuthnRequest = null;
		try {
			saml2AuthnRequest = SAML2ProtocolFactory.getInstance()
					.createAuthnRequest(authnRequestXML);
			saml2AuthnRequest.parse();
		} catch (SAML2Exception e) {
			throw new SAML2Exception(
					"Could not parse the given authentication request XML", e);
		}
		SSORequestInfo ssoRequestInfo = new SSORequestInfo(saml2AuthnRequest);
		ssoRequestInfo.setRelayState(relayState);

		LOGGER.info("validateAuthnRequestHttpBody end...");
		return ssoRequestInfo;
	}



}
