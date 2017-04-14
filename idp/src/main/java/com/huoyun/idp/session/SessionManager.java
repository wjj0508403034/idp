package com.huoyun.idp.session;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huoyun.idp.constants.EndpointsConstants;
import com.huoyun.idp.redis.RedisSessionManager;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationCustomImpl;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.huoyun.idp.user.entity.User;
import com.huoyun.idp.utils.RequestUtil;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.sso.SSORequestInfo;
import com.sap.security.saml2.idp.session.IdPSession;
import com.sap.security.saml2.idp.session.SPSession;
import com.sap.security.saml2.lib.common.SAML2IDGenerator;

public class SessionManager {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(SessionManager.class);

	private RedisSessionManager redisSessionManager;

	public SessionManager(SAML2IdPConfigurationFactory idpConfigurationFactory,
			RedisSessionManager redisSessionManager) {
		this.idpConfigurationFactory = idpConfigurationFactory;
		this.redisSessionManager = redisSessionManager;
	}

	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	/**
	 * If session exists, an ipdConfiguration will be returned, which might have
	 * all the remembered sps
	 * 
	 * @param httpRequest
	 * @return
	 */
	public SAML2IdPConfiguration getIdPConfiguration(
			HttpServletRequest httpRequest) {
		SAML2IdPConfigurationCustomImpl configuration = (SAML2IdPConfigurationCustomImpl) httpRequest
				.getSession().getAttribute(
						EndpointsConstants.SAML2_IDP_CONFIGURATION_ATTR);

		// this part is to initialize localIdp
		if (null != configuration && null == configuration.getLocalIdP()) {
			idpConfigurationFactory.getDefaultSAML2IdpConfiguration();
		}

		return configuration;
	}

	/**
	 * If session exists, an IdPSession is returned
	 * 
	 * @param httpRequest
	 * @return
	 */

	public IdPSession getIdPSession(HttpServletRequest httpRequest) {
		return (IdPSessionImpl) httpRequest.getSession().getAttribute(
				EndpointsConstants.SAML2_USER_IDP_SESS_ATTR);
	}

	private IdPSession createNewIdPSession(HttpServletRequest httpRequest,
			String username) {
		if (null != redisSessionManager) {
			LOGGER.info("Check concurrent session count of the user {}.",
					username);
			try {
				redisSessionManager.checkConcurrentSessionCount(username);
			} catch (Exception ex) {
				LOGGER.warn(
						"Check concurrent session count failure of the user "
								+ username + ".", ex);
			}
		}

		IdPSessionImpl newIdPSession = new IdPSessionImpl();
		newIdPSession.setSessionId(httpRequest.getSession().getId());
		newIdPSession.setSubjectId(username);
		newIdPSession.setClientIP(RequestUtil.getIpAddr(httpRequest));
		newIdPSession.setCreationDate(new Date());
		newIdPSession.setStatus(EndpointsConstants.IDP_STATUS_ACTIVE);

		return newIdPSession;
	}

	private SPSessionImpl createNewSPSession(String spName, String username) {
		SPSessionImpl newSPSession = new SPSessionImpl();
		newSPSession.setSessionIndex(SAML2IDGenerator.generateSPSessionIndex());
		newSPSession.setSPName(spName);
		newSPSession.setSubjectNameId(username);
		newSPSession.setStatus(EndpointsConstants.SP_STATUS_ACTIVE);
		return newSPSession;
	}

	/**
	 * Save the login Session for non saml2 logon, an new IdPConfiguration is
	 * provided
	 * 
	 * @param httpRequest
	 * @param configuration
	 * @param user
	 */
	public void saveLoginSession(HttpServletRequest httpRequest,
			SAML2IdPConfiguration configuration, User user) {
		String username = String.valueOf(user.getId());
		IdPSession idpSession = (IdPSession) httpRequest.getSession()
				.getAttribute(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR);
		if (null == idpSession) {
			idpSession = createNewIdPSession(httpRequest, username);
			httpRequest.getSession().setAttribute(
					EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);
			if (null != redisSessionManager) {
				LOGGER.info("Add idp session id to redis server.");
				redisSessionManager.addIDPSession(idpSession.getSubjectId(),
						idpSession.getSessionId());
			}
		}
		httpRequest.getSession().setAttribute(
				EndpointsConstants.SAML2_IDP_CONFIGURATION_ATTR, configuration);
		httpRequest.getSession().setAttribute(
				EndpointsConstants.SAML2_USER_SESS_ATTR, user);
	}

	/**
	 * Save the login session for first sp session, an new IdPConfiguration is
	 * provided
	 * 
	 * @param httpRequest
	 * @param configuration
	 * @param ssoRequestInfo
	 * @param user
	 * @return
	 */

	public SPSession saveLoginSession(HttpServletRequest httpRequest,
			SAML2IdPConfiguration configuration, SSORequestInfo ssoRequestInfo,
			User user) {
		String spName = ssoRequestInfo.getSaml2AuthnRequest().getIssuer()
				.getName();
		String username = String.valueOf(user.getId());

		IdPSession idpSession = (IdPSession) httpRequest.getSession()
				.getAttribute(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR);
		if (null == idpSession) {
			idpSession = createNewIdPSession(httpRequest, username);
			httpRequest.getSession().setAttribute(
					EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);

			if (null != redisSessionManager) {
				LOGGER.info("Add idp session id to redis server.");
				redisSessionManager.addIDPSession(idpSession.getSubjectId(),
						idpSession.getSessionId());
			}
		}
		if (null == idpSession.getSPSession(spName)) {
			SPSession spSession = createNewSPSession(spName, username);
			((IdPSessionImpl) idpSession).setSPSession(spSession);
			((SAML2IdPConfigurationCustomImpl) configuration)
					.addSP(ssoRequestInfo.getSaml2AuthnRequest().getIssuer()
							.getName(), ssoRequestInfo.getSaml2AuthnRequest()
							.getAssertionConsumerServiceURL());
			httpRequest.getSession().setAttribute(
					EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);
		}
		((SPSessionImpl) idpSession.getSPSession(spName))
				.setStatus(EndpointsConstants.SP_STATUS_ACTIVE);
		httpRequest.getSession().setAttribute(
				EndpointsConstants.SAML2_IDP_CONFIGURATION_ATTR, configuration);
		httpRequest.getSession().setAttribute(
				EndpointsConstants.SAML2_USER_SESS_ATTR, user);

		return idpSession.getSPSession(spName);
	}

	/**
	 * Save the SP session only, this means that IdPSession already exists
	 * 
	 * @param httpRequest
	 * @param ssoRequestInfo
	 * @return
	 */

	public SPSession saveLoginSession(HttpServletRequest httpRequest,
			SSORequestInfo ssoRequestInfo) {
		String spName = ssoRequestInfo.getSaml2AuthnRequest().getIssuer()
				.getName();
		User user = (User) httpRequest.getSession().getAttribute(
				EndpointsConstants.SAML2_USER_SESS_ATTR);
		IdPSession idpSession = (IdPSession) httpRequest.getSession()
				.getAttribute(EndpointsConstants.SAML2_USER_IDP_SESS_ATTR);
		SAML2IdPConfiguration configuration = getIdPConfiguration(httpRequest);
		if (null == idpSession.getSPSession(spName)) {
			SPSession spSession = createNewSPSession(spName,
					String.valueOf(user.getId()));
			((IdPSessionImpl) idpSession).setSPSession(spSession);
			((SAML2IdPConfigurationCustomImpl) configuration)
					.addSP(ssoRequestInfo.getSaml2AuthnRequest().getIssuer()
							.getName(), ssoRequestInfo.getSaml2AuthnRequest()
							.getAssertionConsumerServiceURL());

			httpRequest.getSession().setAttribute(
					EndpointsConstants.SAML2_USER_IDP_SESS_ATTR, idpSession);
			httpRequest.getSession().setAttribute(
					EndpointsConstants.SAML2_IDP_CONFIGURATION_ATTR,
					configuration);
		}
		((SPSessionImpl) idpSession.getSPSession(spName))
				.setStatus(EndpointsConstants.SP_STATUS_ACTIVE);
		return idpSession.getSPSession(spName);
	}

	/**
	 * Invalidate all logon session objects
	 * 
	 * @param httpRequest
	 */

	public void invalidateLoginSession(HttpServletRequest httpRequest) {
		httpRequest.getSession().invalidate();
	}

	/**
	 * Return User Object in the session
	 * 
	 * @param httpRequest
	 * @return
	 */

	public User getUser(HttpServletRequest httpRequest) {
		return (User) httpRequest.getSession().getAttribute(
				EndpointsConstants.SAML2_USER_SESS_ATTR);
	}

}
