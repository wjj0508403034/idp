package com.huoyun.idp.saml2.slo.impl;

import java.util.List;

import com.huoyun.idp.saml2.slo.SingleLogoutService;
import com.huoyun.idp.saml2.utils.SAML2BuilderFactory;
import com.huoyun.idp.utils.IdPUtils;
import com.sap.security.saml2.cfg.enums.SAML2Binding;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.cfg.interfaces.read.SAML2LocalIdP;
import com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP;
import com.sap.security.saml2.commons.slo.SLORequestInfo;
import com.sap.security.saml2.idp.api.LogoutResponseData;
import com.sap.security.saml2.lib.common.SAML2Constants;
import com.sap.security.saml2.lib.common.SAML2ErrorResponseDetails;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.common.exceptions.SAML2ErrorResponseException;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2NameID;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutRequest;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutResponse;
import com.sap.tc.logging.Location;

public class SingleLogoutServiceImpl implements SingleLogoutService {

	private static final Location LOCATION = Location
			.getLocation(SingleLogoutServiceImpl.class);
	private SAML2BuilderFactory builderFactory;

	public SingleLogoutServiceImpl(SAML2BuilderFactory builderFactory) {
		this.builderFactory = builderFactory;
	}

	@Override
	public SAML2LogoutRequest createSLORequest(
			SAML2IdPConfiguration configuration, String spName,
			String relayState, SAML2NameID nameId, List<String> sessionIndexes,
			String sloRequestId) throws SAML2Exception,
			SAML2ConfigurationException {
		final String METHOD_NAME = "createSLORequestHttpBody";
		if (LOCATION.bePath()) {
			LOCATION.entering(METHOD_NAME, new Object[] { spName, relayState,
					nameId, sessionIndexes });
		}
		// Check required input parameters
		IdPUtils.checkStringArgument(METHOD_NAME, "spName", spName);
		IdPUtils.checkNameId(METHOD_NAME, nameId);
		IdPUtils.checkSessionIndexes(METHOD_NAME, sessionIndexes);

		// Get and trace configurations
		SAML2LocalIdP localIdP = IdPUtils.getLocalIdP(configuration);
		SAML2TrustedSP trustedSP = IdPUtils.getTrustedSP(configuration, spName);

		// Get SLO endpoint location
		String spSLOLocation = IdPUtils.getSLOLocation(trustedSP,
				SAML2Binding.HTTP_POST_BINDING);

		// Create the SLO request
		SAML2LogoutRequest logoutRequest = this.builderFactory
				.createLogoutRequest(localIdP, trustedSP, spSLOLocation,
						nameId.getName(), nameId.getFormat(),
						nameId.getSPNameQualifier(), nameId.getNameQualifier(),
						sessionIndexes, sloRequestId);

		// Sign the logout request
		IdPUtils.signSLOMessage(localIdP, trustedSP, logoutRequest);

		if (LOCATION.bePath()) {
			LOCATION.exiting(METHOD_NAME);
		}
		return logoutRequest;
	}

	@Override
	public SAML2LogoutResponse createSLOResponse(
			SAML2IdPConfiguration configuration, SLORequestInfo sloRequestInfo)
			throws SAML2Exception, SAML2ConfigurationException {
		final String METHOD_NAME = "createSLOResponseHttpBody";
		if (LOCATION.bePath()) {
			LOCATION.entering(METHOD_NAME, new Object[] { sloRequestInfo });
		}

		IdPUtils.checkArgument(METHOD_NAME, "sloRequestInfo", sloRequestInfo);

		// Get the issuer
		String trustedSPName = IdPUtils.getIssuer(sloRequestInfo);
		if (trustedSPName == null || trustedSPName.length() == 0) {
			throw new SAML2Exception(
					"SLO response message could not be created, because sloRequestInfo does not contain issuer name of the original request.");
		}

		LogoutResponseData logoutResponseData = new LogoutResponseData(
				sloRequestInfo.getId(),
				SAML2Constants.STATUS_CODE_TOP_LEVEL_SUCCESS, null, null,
				sloRequestInfo.getRelayState());
		return createSLOResponse(configuration, trustedSPName,
				logoutResponseData);
	}

	@Override
	public SAML2LogoutResponse createSLOResponse(
			SAML2IdPConfiguration configuration, String spName,
			LogoutResponseData logoutResponseData) throws SAML2Exception,
			SAML2ConfigurationException {
		final String METHOD_NAME = "createSLOResponseHttpBody";
		if (LOCATION.bePath()) {
			LOCATION.entering(METHOD_NAME, new Object[] { spName,
					logoutResponseData });
		}

		// Check required input parameters
		IdPUtils.checkStringArgument(METHOD_NAME, "spName", spName);
		if (logoutResponseData == null) {
			throw new IllegalArgumentException(
					"Parameter logoutResponseData is null.");
		}
		IdPUtils.checkStringArgument(METHOD_NAME, "inResponseTo",
				logoutResponseData.getInResponseTo());

		if (logoutResponseData.getStatusCode() == null) {
			logoutResponseData
					.setStatusCode(SAML2Constants.STATUS_CODE_TOP_LEVEL_SUCCESS);
		}

		// Get and trace configurations
		SAML2LocalIdP localIdP = IdPUtils.getLocalIdP(configuration);
		SAML2TrustedSP trustedSP = IdPUtils.getTrustedSP(configuration, spName);

		// Create the logout response object
		SAML2LogoutResponse logoutResponse = this.builderFactory
				.createLogoutResponse(localIdP, trustedSP,
						SAML2Binding.HTTP_POST_BINDING,
						logoutResponseData.getInResponseTo(),
						logoutResponseData.getStatusCode(),
						logoutResponseData.getSecondLevelStatusCode(),
						logoutResponseData.getStatusMessage());

		// Sign the SLO message
		try {
			IdPUtils.signSLOMessage(localIdP, trustedSP, logoutResponse);
		} catch (SAML2ConfigurationException e) {
			SAML2ErrorResponseDetails details = new SAML2ErrorResponseDetails(
					logoutResponseData.getInResponseTo(), spName,
					SAML2Constants.STATUS_CODE_TOP_LEVEL_RESPONDER, null,
					"Cannot get private key for local IdP.");
			throw new SAML2ErrorResponseException(
					"Cannot get private key for local IdP.", e, details);
		}

		if (LOCATION.bePath()) {
			LOCATION.exiting(METHOD_NAME);
		}
		return logoutResponse;
	}
}
