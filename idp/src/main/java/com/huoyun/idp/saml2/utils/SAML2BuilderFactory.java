package com.huoyun.idp.saml2.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huoyun.idp.saml2.controller.SingleSignOnController;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.sso.SSORequestInfo;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.SAML2ProtocolFactory;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2AuthRequest;

public class SAML2BuilderFactory {

	private static Logger logger = LoggerFactory
			.getLogger(SAML2BuilderFactory.class);

	public SSORequestInfo validateAuthnRequestHttpBody(
			SAML2IdPConfiguration configuration, String authnRequestXML,
			String relayState, String recipientUrl) throws SAML2Exception,
			SAML2ConfigurationException, SAML2ErrorResponseException {
		logger.info("validateAuthnRequestHttpBody start...");

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

		logger.info("validateAuthnRequestHttpBody end...");
		return ssoRequestInfo;
	}
	
	
	

}
