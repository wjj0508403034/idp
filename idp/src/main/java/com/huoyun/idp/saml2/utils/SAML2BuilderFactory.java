package com.huoyun.idp.saml2.utils;

import java.security.PublicKey;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huoyun.idp.utils.IdPUtils;
import com.sap.security.saml2.cfg.enums.EncryptionAlgorithm;
import com.sap.security.saml2.cfg.enums.SAML2Binding;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.cfg.interfaces.read.SAML2Endpoint;
import com.sap.security.saml2.cfg.interfaces.read.SAML2LocalIdP;
import com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP;
import com.sap.security.saml2.commons.sso.SSORequestInfo;
import com.sap.security.saml2.lib.common.SAML2DataFactory;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.SAML2ProtocolFactory;
import com.sap.security.saml2.lib.common.SAML2Utils;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2EncryptedNameID;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2NameID;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2AuthRequest;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutRequest;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutResponse;

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

	public SAML2LogoutRequest createLogoutRequest(SAML2LocalIdP localIdP,
			SAML2TrustedSP trustedSP, String spSLOUrl, String nameId,
			String nameIdFormat, String nameIdSPNameQualifier,
			String nameIdNameQualifier, List<String> sessionIndexes,
			String requestId) throws SAML2Exception,
			SAML2ConfigurationException {

		// Create Name ID
		SAML2NameID userNameId = SAML2DataFactory.getInstance()
				.createSAML2NameID(nameId);
		userNameId.setFormat(nameIdFormat);
		userNameId.setSPNameQualifier(nameIdSPNameQualifier);
		userNameId.setNameQualifier(nameIdNameQualifier);

		// Create the LogoutRequest and fill its fields
		SAML2LogoutRequest logoutRequest;
		try {
			Date now = Calendar.getInstance().getTime();
			if (requestId == null) {
				requestId = SAML2Utils.generateUUID();
			}

			if (trustedSP.isToEncryptSingleLogoutSubject()) {

				// Create LogoutRequest with encrypted Name ID
				EncryptionAlgorithm encryptAlgorithm = trustedSP
						.getEncryptionAlgorithm();
				if (encryptAlgorithm == null) {
					throw new SAML2ConfigurationException(
							"Encryption algorithm cannot be read from trusted SP configuration.");
				}

				PublicKey spEncPublicKey = IdPUtils
						.getTrustedSPPublicKeyForEncryption(trustedSP);
				SAML2EncryptedNameID encNameId = userNameId.encrypt(
						spEncPublicKey, encryptAlgorithm.getName());

				logoutRequest = SAML2ProtocolFactory.getInstance()
						.createLogoutRequest(requestId, now, encNameId);

			} else {

				// Create LogoutRequest with plain Name ID
				logoutRequest = SAML2ProtocolFactory.getInstance()
						.createLogoutRequest(requestId, now, userNameId);

			}

			// Set LogoutRequest session indexes list
			logoutRequest.setSessionIndex(sessionIndexes);

			// Set LogoutRequest issuer
			SAML2NameID issuerNameId = SAML2DataFactory.getInstance()
					.createSAML2NameID(localIdP.getName());
			logoutRequest.setIssuer(issuerNameId);

			// Set LogoutRequest destination
			logoutRequest.setDestination(spSLOUrl);

		} catch (SAML2ConfigurationException e) {

			throw e;
		} catch (SAML2Exception e) {

			throw e;
		}

		return logoutRequest;
	}

	public static SAML2LogoutResponse createLogoutResponse(
			SAML2LocalIdP localIdP, String spSLOLocation, String inResponseTo,
			String statusCode, String secondLevelStatusCode,
			String statusMessage) throws SAML2Exception {

		SAML2LogoutResponse logoutResponse = SAML2ProtocolFactory.getInstance()
				.createLogoutResponse(SAML2Utils.generateUUID(), statusCode,
						new Date());

		// Set second level status code
		logoutResponse.setSecondLevelStatusCode(secondLevelStatusCode);

		// Set Issuer
		SAML2NameID nameID = SAML2DataFactory.getInstance().createSAML2NameID(
				localIdP.getName());
		logoutResponse.setIssuer(nameID);

		// Set InResponseTo
		logoutResponse.setInResponseTo(inResponseTo);

		// Set destination
		logoutResponse.setDestination(spSLOLocation);

		// Set Status Message
		logoutResponse.setStatusMessage(statusMessage);

		return logoutResponse;
	}

	public SAML2LogoutResponse createLogoutResponse(SAML2LocalIdP localIdP,
			SAML2TrustedSP trustedSP, SAML2Binding binding,
			String inResponseTo, String statusCode,
			String secondLevelStatusCode, String statusMessage)
			throws SAML2Exception, SAML2ConfigurationException {
		SAML2Endpoint spSLOEndpoint = IdPUtils.getSLOEndpoint(trustedSP,
				binding);

		String spSLOLocation = spSLOEndpoint.getResponseLocation();
		if (spSLOLocation == null || spSLOLocation.length() < 1) {
			spSLOLocation = spSLOEndpoint.getLocation();
			if (spSLOLocation == null || spSLOLocation.length() < 1) {
				throw new SAML2ConfigurationException(
						"Configured SLO endpoint for SP: "
								+ trustedSP.getName()
								+ " does not contain ResponseLocation or Location");
			}
		}

		return createLogoutResponse(localIdP, spSLOLocation, inResponseTo,
				statusCode, secondLevelStatusCode, statusMessage);
	}

}
