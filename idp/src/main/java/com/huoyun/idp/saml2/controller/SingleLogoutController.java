package com.huoyun.idp.saml2.controller;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.huoyun.idp.common.BusinessException;
import com.huoyun.idp.constants.EndpointsConstants;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.saml2.slo.SingleLogoutService;
import com.huoyun.idp.session.IdPSessionImpl;
import com.huoyun.idp.session.SPSessionImpl;
import com.huoyun.idp.session.SessionManager;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.web.ViewConstants;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.slo.SLOInfo;
import com.sap.security.saml2.commons.slo.SLORequestInfo;
import com.sap.security.saml2.commons.slo.SLOResponseInfo;
import com.sap.security.saml2.idp.api.SAML2IdPAPI;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.idp.session.SPSession;
import com.sap.security.saml2.lib.bindings.HTTPPostBinding;
import com.sap.security.saml2.lib.common.SAML2DataFactory;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.SAML2Utils;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.common.exceptions.SAML2ValidationFailedException;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2NameID;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutRequest;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutResponse;

@Controller
@RequestMapping("/saml2/idp")
public class SingleLogoutController {
	private static Logger LOG = LoggerFactory.getLogger(SingleLogoutController.class);

	private static final String SSO_DESTINATION = "destination";

	@Autowired
	private SessionManager sessionManager;

	@Autowired
	private SingleLogoutService sloService;

	@Autowired
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	@RequestMapping(value = "/slo", method = RequestMethod.POST)
	public ModelAndView slo(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException, SAML2Exception, SAML2ConfigurationException, BusinessException {
		try {
			LOG.info("single logout log, POST.");
			HttpSession httpSession = httpRequest.getSession();
			if (null != httpSession) {
				LOG.info("single logout log, sessionId", httpSession.getId());
				User userInSession = (User) httpSession.getAttribute(EndpointsConstants.SAML2_USER_SESS_ATTR);
				if (null != userInSession) {
					LOG.info("single logout log, user: {}", userInSession.getEmail());
				}
			}

			Cookie[] cookies = httpRequest.getCookies();
			for (Cookie cookie : cookies) {
				LOG.info("single logout log, cookies: {} {}", cookie.getName(), cookie.getValue());
			}

			SAML2IdPConfiguration saml2IdPConfiguration = sessionManager.getIdPConfiguration(httpRequest);
			LOG.info("single logout log, SAML2IdPConfiguration: {}", saml2IdPConfiguration);
			if (null != saml2IdPConfiguration) {
				LOG.info("single logout log, LocalIdP: {}", saml2IdPConfiguration.getLocalIdP());
			}
		} catch (Exception e) {
			LOG.warn("single logout log exception. Please note that this is not a bug. It's just for log info.", e);
		}

		SLOInfo sloRequestDetails = null;
		SAML2IdPConfiguration idpConfig = sessionManager.getIdPConfiguration(httpRequest);
		if (null == idpConfig || idpConfig.getLocalIdP() == null) {
			LOG.info("No IDP found, remove cookie, logout completed. Redirect ot SP's /logout?nonSamlLogout=true");
			return directLogout(httpRequest, httpResponse);
		}

		try {
			sloRequestDetails = SAML2IdPAPI.getInstance().validateSLOMessageHttpBody(idpConfig,
					extractSAMl2POSTParams(httpRequest));
		} catch (Exception e) {
			LOG.warn("Validate SLO message http body failed.", e);
			return directLogout(httpRequest, httpResponse);
		}

		return processSLOMessage(httpRequest, httpResponse, sloRequestDetails);
	}

	public Map<String, String> extractSAMl2POSTParams(HttpServletRequest req) {

		Map<String, String> saml2PostParameters = new HashMap<String, String>();

		saml2PostParameters.put(HTTPPostBinding.SAML_REQUEST, req.getParameter(HTTPPostBinding.SAML_REQUEST));
		saml2PostParameters.put(HTTPPostBinding.SAML_RESPONSE, req.getParameter(HTTPPostBinding.SAML_RESPONSE));
		saml2PostParameters.put(HTTPPostBinding.SAML_RELAY_STATE, req.getParameter(HTTPPostBinding.SAML_RELAY_STATE));
		saml2PostParameters.put(HTTPPostBinding.SAML_ARTIFACT, req.getParameter(HTTPPostBinding.SAML_ARTIFACT));

		return saml2PostParameters;
	}

	private ModelAndView directLogout(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws BusinessException {
		logoutIdPSession(httpRequest, httpResponse);

		String domain = getRedirectDomain(httpRequest);
		return new ModelAndView("redirect:https://" + domain + "/logout?nonSamlLogout=true");
	}

	@RequestMapping(value = "/slo", method = RequestMethod.GET)
	public ModelAndView sloNonSaml2(HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws SAML2Exception, SAML2ConfigurationException, BusinessException {
		try {
			LOG.info("single logout log, GET.");
			HttpSession httpSession = httpRequest.getSession();
			if (null != httpSession) {
				LOG.info("single logout log, sessionId", httpSession.getId());
				User userInSession = (User) httpSession.getAttribute(EndpointsConstants.SAML2_USER_SESS_ATTR);
				if (null != userInSession) {
					LOG.info("single logout log, user: {}", userInSession.getEmail());
				}
			}

			Cookie[] cookies = httpRequest.getCookies();
			for (Cookie cookie : cookies) {
				LOG.info("single logout log, cookies: {} {}", cookie.getName(), cookie.getValue());
			}

			SAML2IdPConfiguration saml2IdPConfiguration = sessionManager.getIdPConfiguration(httpRequest);
			LOG.info("single logout log, SAML2IdPConfiguration: {}", saml2IdPConfiguration);
			if (null != saml2IdPConfiguration) {
				LOG.info("single logout log, LocalIdP: {}", saml2IdPConfiguration.getLocalIdP());
			}
		} catch (Exception e) {
			LOG.warn("single logout log exception. Please note that this is not a bug. It's just for log info.", e);
		}

		SAML2IdPConfiguration idpConfig = sessionManager.getIdPConfiguration(httpRequest);
		if (null == idpConfig || idpConfig.getLocalIdP() == null) {
			LOG.info("No IDP found, remove cookie, logout completed. Redirect ot SP's root");
			logoutIdPSession(httpRequest, httpResponse);

			String domain = getRedirectDomain(httpRequest);
			return new ModelAndView("redirect:https://" + domain);
		}

		return processNonSaml2SLORequest(httpRequest, httpResponse, idpConfig);

	}

	private ModelAndView processNonSaml2SLORequest(HttpServletRequest request, HttpServletResponse response,
			SAML2IdPConfiguration idpConfig) throws SAML2Exception, SAML2ConfigurationException, BusinessException {

		String redirectDomain = getRedirectDomain(request);

		IdPSessionImpl idpSession = (IdPSessionImpl) sessionManager.getIdPSession(request);
		Collection<SPSession> spSessions = idpSession.getSPSessions();
		if ((spSessions == null) || (spSessions.size() < 2)) {
			LOG.warn("There's no sp session to logout, so just send back response");
			logoutIdPSession(request, response);
			return new ModelAndView("redirect:https://" + redirectDomain);
		} else {
			SPSessionImpl nextSPSession = (SPSessionImpl) idpSession
					.getFirstSPSession(EndpointsConstants.SP_STATUS_ACTIVE);
			if (null != nextSPSession) {
				LOG.info("User '{}' is going to logout {}, not in saml way", sessionManager.getUser(request).getId(),
						nextSPSession.getSpName());
				nextSPSession.setSLORequestId(SAML2Utils.generateUUID());
				nextSPSession.setStatus(EndpointsConstants.SP_STATUS_LOGOUT_IN_PROGRESS);
				return buildSLORequestModelAndView(request, idpConfig, null, nextSPSession);
			} else {
				LOG.info("User '{}' logout completed", sessionManager.getUser(request).getId());
				logoutIdPSession(request, response);
				return new ModelAndView("redirect:https://" + redirectDomain);
			}
		}
	}

	private ModelAndView processSLOMessage(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
			SLOInfo sloDetails) throws SAML2ErrorResponseException, SAML2Exception, SAML2ConfigurationException,
			IOException, BusinessException {
		if (sloDetails instanceof SLORequestInfo) {
			SLORequestInfo sloRequestInfo = (SLORequestInfo) sloDetails;
			return processSLORequestMessage(httpRequest, httpResponse, sloRequestInfo);

		} else {
			SLOResponseInfo sloResponseInfo = (SLOResponseInfo) sloDetails;
			return processSLOResponseMessage(httpRequest, httpResponse, sloResponseInfo);

		}

	}

	private ModelAndView processSLORequestMessage(HttpServletRequest request, HttpServletResponse response,
			SLORequestInfo sloRequestInfo)
			throws SAML2ErrorResponseException, SAML2ConfigurationException, SAML2Exception {

		SAML2IdPConfiguration idpConfig = sessionManager.getIdPConfiguration(request);

		if (null == idpConfig) {
			this.logoutIdPSession(request, response);
			return buildSLOResponseModelAndView(request, idpConfigurationFactory.getDefaultSAML2IdpConfiguration(),
					sloRequestInfo);
		}
		IdPSessionImpl idpSession = (IdPSessionImpl) sessionManager.getIdPSession(request);

		checkSLORequestValidity(idpSession, sloRequestInfo);

		Collection<SPSession> spSessions = idpSession.getSPSessions();
		if ((spSessions == null) || (spSessions.size() < 2)) {
			LOG.info("Logout completed");
			logoutIdPSession(request, response);
			return buildSLOResponseModelAndView(request, idpConfig, sloRequestInfo);
		}
		return logoutSPSessions(request, response, idpConfig, sloRequestInfo, idpSession);
	}

	public void checkSLORequestValidity(IdPSession idpSession, SLORequestInfo sloRequestInfo)
			throws SAML2ValidationFailedException {
		boolean isValid = false;

		String receivedSPName = sloRequestInfo.getIssuer();

		String currentSessionIndex = null;
		SPSession spSession = idpSession.getSPSession(receivedSPName);
		if (spSession != null) {
			currentSessionIndex = spSession.getSessionIndex();

			isValid = (currentSessionIndex != null) && sloRequestInfo.getSessionIndexes().contains(currentSessionIndex);
		}

		if (!isValid) {
			// throw new
			// SAML2ValidationFailedException("Session Index doesn't match");
			LOG.warn(
					"SAML SP session index doesn't match. "
							+ "This may be caused by multiple logins at the same time in the same browser, "
							+ "which is not supported. " + "Session index in IDP session is {}. "
							+ "Session index in SLO request is {}. ",
					currentSessionIndex, sloRequestInfo.getSessionIndexes().toString());
		}
	}

	public ModelAndView buildSLOResponseModelAndView(HttpServletRequest httpRequest,
			SAML2IdPConfiguration configuration, SLORequestInfo sloRequestInfo)
			throws SAML2Exception, SAML2ConfigurationException {
		ModelAndView m = new ModelAndView(ViewConstants.Logout_Waiting);

		SAML2LogoutResponse r = sloService.createSLOResponse(configuration, sloRequestInfo);

		m.getModel().put(HTTPPostBinding.SAML_RESPONSE, SAML2Utils.encodeBase64AsString(r.generate()));
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, sloRequestInfo.getRelayState());
		m.getModel().put(SSO_DESTINATION, r.getDestination());
		return m;
	}

	public void logoutIdPSession(HttpServletRequest request, HttpServletResponse response) {
		Cookie c = new Cookie(EndpointsConstants.PERSISTENT_LOGIN_COOKIE, "");
		c.setMaxAge(0);
		response.addCookie(c);
		sessionManager.invalidateLoginSession(request);
	}

	private ModelAndView logoutSPSessions(HttpServletRequest request, HttpServletResponse response,
			SAML2IdPConfiguration idpConfig, SLORequestInfo sloRequestInfo, IdPSessionImpl idpSession)
			throws SAML2ErrorResponseException, SAML2Exception, SAML2ConfigurationException {

		String sloRequestIssuer = sloRequestInfo.getIssuer();
		String sloRequestId = sloRequestInfo.getId();

		idpSession.setSLOInitiatorRequestId(sloRequestId);
		idpSession.setSLOInitiatorRequestIssuer(sloRequestInfo.getIssuer());
		idpSession.setSLOInitiatorRelayState(sloRequestInfo.getRelayState());
		if (EndpointsConstants.IDP_STATUS_LOGOUT_IN_PROGRESS != idpSession.getStatus()) {
			idpSession.setStatus(EndpointsConstants.IDP_STATUS_LOGOUT_IN_PROGRESS);
		}

		SPSessionImpl spSession = (SPSessionImpl) idpSession.getSPSession(sloRequestIssuer);

		if (EndpointsConstants.SP_STATUS_LOGGED_OUT != spSession.getStatus()) {
			spSession.setStatus(EndpointsConstants.SP_STATUS_LOGGED_OUT);
		}
		LOG.info("User '{}' had logout {} successfully", sessionManager.getUser(request).getId(),
				spSession.getSpName());
		return logoutNextSPSession(request, response, idpConfig, sloRequestInfo, idpSession);
	}

	private ModelAndView logoutNextSPSession(HttpServletRequest request, HttpServletResponse response,
			SAML2IdPConfiguration idpConfig, SLORequestInfo sloRequestInfo, IdPSessionImpl idpSession)
			throws SAML2ErrorResponseException, SAML2Exception, SAML2ConfigurationException {

		SPSessionImpl nextSPSession = (SPSessionImpl) idpSession.getFirstSPSession(EndpointsConstants.SP_STATUS_ACTIVE);

		if (nextSPSession == null) {
			LOG.info("User '{}' logout completed", sessionManager.getUser(request).getId());
			logoutIdPSession(request, response);
			return buildSLOResponseModelAndView(request, idpConfig, sloRequestInfo);

		}
		LOG.info("User '{}' is going to logout {}", sessionManager.getUser(request).getId(), nextSPSession.getSpName());
		nextSPSession.setSLORequestId(SAML2Utils.generateUUID());
		nextSPSession.setStatus(EndpointsConstants.SP_STATUS_LOGOUT_IN_PROGRESS);
		request.getSession().setAttribute(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);
		return buildSLORequestModelAndView(request, idpConfig, null, nextSPSession);

	}

	private ModelAndView processSLOResponseMessage(HttpServletRequest request, HttpServletResponse response,
			SLOResponseInfo sloResponseInfo) throws SAML2ErrorResponseException, SAML2Exception,
			SAML2ConfigurationException, IOException, BusinessException {

		SAML2IdPConfiguration idpConfig = sessionManager.getIdPConfiguration(request);
		String sloResponseIssuer = sloResponseInfo.getIssuer();

		IdPSessionImpl idpSession = (IdPSessionImpl) sessionManager.getIdPSession(request);

		SPSessionImpl spSession = (SPSessionImpl) idpSession.getSPSession(sloResponseIssuer);

		if (null != spSession) {
			LOG.info("User '{}' had logout {} successfully", sessionManager.getUser(request).getId(),
					spSession.getSpName());
			spSession.setStatus(EndpointsConstants.SP_STATUS_LOGGED_OUT);
		}

		return processActiveSessions(request, response, idpSession, sloResponseInfo, idpConfig);

	}

	private ModelAndView processActiveSessions(HttpServletRequest request, HttpServletResponse response,
			IdPSessionImpl idpSession, SLOResponseInfo sloResponseInfo, SAML2IdPConfiguration idpConfig)
			throws SAML2ErrorResponseException, SAML2ConfigurationException, SAML2Exception, IOException,
			BusinessException {
		SPSessionImpl nextSPSession = (SPSessionImpl) idpSession.getFirstSPSession(EndpointsConstants.SP_STATUS_ACTIVE);
		if (nextSPSession == null) {
			LOG.info("User '{}' had logout from IDP successfully", sessionManager.getUser(request).getId());
			if (null != idpSession.geSLOInitiatorRequestId()) {
				SLORequestInfo sloRequestInfo = createOriginalSLORequestInfo(idpSession);
				logoutIdPSession(request, response);
				return buildSLOResponseModelAndView(request, idpConfig, sloRequestInfo);
			} else {
				logoutIdPSession(request, response);

				String domain = getRedirectDomain(request);

				return new ModelAndView("redirect:https://" + domain);
			}

		} else {
			nextSPSession.setSLORequestId(SAML2Utils.generateUUID());
			nextSPSession.setStatus(EndpointsConstants.SP_STATUS_LOGOUT_IN_PROGRESS);
			request.getSession().setAttribute(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);
			return buildSLORequestModelAndView(request, idpConfig, null, nextSPSession);

		}
	}

	public SLORequestInfo createOriginalSLORequestInfo(IdPSessionImpl idpSession) {
		// create sloRequestInfo with original data
		String id = idpSession.geSLOInitiatorRequestId();
		String issuer = idpSession.getSLOInitiatorRequestIssuer();
		String relayState = idpSession.getSLOInitiatorRelayState();

		// get spSession by issuer and read nameid data
		SPSession initiatorSPSession = idpSession.getSPSession(issuer);
		String subjectNameId = initiatorSPSession.getSubjectNameId();
		String subjectNameIdFormat = initiatorSPSession.getSubjectNameIdFormat();
		String spProvidedID = initiatorSPSession.getSubjectSPProvidedId();

		SAML2NameID nameId = SAML2DataFactory.getInstance().createSAML2NameID(subjectNameId);
		nameId.setFormat(subjectNameIdFormat);
		nameId.setSPProvidedID(spProvidedID);

		List<String> sessionIndexes = null;
		SAML2LogoutRequest logoutRequest = null;
		SLORequestInfo sloRequestInfo = new SLORequestInfo(id, issuer, nameId, sessionIndexes, logoutRequest);
		sloRequestInfo.setRelayState(relayState);

		return sloRequestInfo;
	}

	public ModelAndView buildSLORequestModelAndView(HttpServletRequest httpRequest, SAML2IdPConfiguration idpConfig,
			String relayState, SPSession spSession) throws SAML2Exception, SAML2ConfigurationException {
		ModelAndView m = new ModelAndView(ViewConstants.Logout_Waiting);
		SAML2NameID nameId = SAML2DataFactory.getInstance().createSAML2NameID(spSession.getSubjectNameId());
		nameId.setFormat(spSession.getSubjectNameIdFormat());
		nameId.setSPProvidedID(spSession.getSubjectSPProvidedId());
		SAML2LogoutRequest r = sloService.createSLORequest(idpConfig, spSession.getSPName(), relayState, nameId,
				Arrays.asList(spSession.getSessionIndex()), spSession.getSLORequestId());

		m.getModel().put(HTTPPostBinding.SAML_REQUEST, SAML2Utils.encodeBase64AsString(r.generate()));
		m.getModel().put(HTTPPostBinding.SAML_RELAY_STATE, relayState);
		m.getModel().put(SSO_DESTINATION, r.getDestination());
		return m;
	}

	private String getRedirectDomain(HttpServletRequest httpRequest) throws BusinessException {
		String referer = httpRequest.getHeader("Referer");
		if (StringUtils.isEmpty(referer)) {
			LOG.error("Getting redirect domain from header referer failed because its empty.");
			throw new BusinessException("IDP");
		}

		LOG.info("Referer header is {}.", referer);

		String redirectDomain = null;
		try {
			URI uri = new URI(referer);
			redirectDomain = uri.getHost();
		} catch (Exception e) {
			LOG.error("The referer header {} contains an invalid URI, which cannot be parsed.", referer);
			throw new BusinessException("IDP");
		}

		return redirectDomain;
	}
}
