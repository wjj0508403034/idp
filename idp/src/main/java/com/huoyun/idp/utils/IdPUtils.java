package com.huoyun.idp.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

import com.sap.security.saml2.cfg.enums.SAML2Binding;
import com.sap.security.saml2.cfg.enums.SignatureOption;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.cfg.interfaces.read.SAML2Endpoint;
import com.sap.security.saml2.cfg.interfaces.read.SAML2LocalIdP;
import com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP;
import com.sap.security.saml2.commons.slo.SLORequestInfo;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2NameID;
import com.sap.security.saml2.lib.interfaces.bindings.RedirectPayloadDeflate;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2ProtocolToken;
import com.sap.tc.logging.Location;

public class IdPUtils {
	private static final Location LOCATION = Location
			.getLocation(IdPUtils.class);

	public static SAML2Endpoint getSLOEndpoint(SAML2TrustedSP trustedSP,
			SAML2Binding binding) throws SAML2ConfigurationException {
		List<SAML2Endpoint> sloEndpoints = trustedSP
				.getSingleLogoutEndpoints(binding);
		if (sloEndpoints == null || sloEndpoints.size() == 0) {
			throw new SAML2ConfigurationException(
					"SLO message could not be created, because there are not any SLO endpoints configured for binding: "
							+ binding);
		}

		// Get the first SLO endpoint
		SAML2Endpoint spSLOEndpoint = sloEndpoints.get(0);
		for (SAML2Endpoint endpoint : sloEndpoints) {
			if (endpoint != null && endpoint.isDefault()) {
				// get the default one
				spSLOEndpoint = endpoint;
				break;
			}
		}

		return spSLOEndpoint;
	}

	public static String getSLOLocation(SAML2TrustedSP trustedSP, SAML2Binding binding)
			throws SAML2ConfigurationException {
		// Get the endpoint
		SAML2Endpoint spSLOEndpoint = getSLOEndpoint(trustedSP, binding);

		// Get the SLO location
		String spSLOLocation = spSLOEndpoint.getLocation();
		if (spSLOLocation == null || spSLOLocation.length() == 0) {
			throw new SAML2ConfigurationException(
					"Empty location is configured in SLO endpoint for trusted SP: "
							+ trustedSP.getName());
		}

		if (LOCATION.beDebug()) {
			LOCATION.debugT("SLO endpoint location: {0}",
					new Object[] { spSLOLocation });
		}

		return spSLOLocation;
	}

	static SAML2Endpoint getACSEndpoint(SAML2TrustedSP trustedSP,
			SAML2Binding binding) throws SAML2ConfigurationException {
		List<SAML2Endpoint> acsEndpoints = trustedSP
				.getAssertionConsumerEndpoints(binding);
		if (acsEndpoints == null || acsEndpoints.size() == 0) {
			throw new SAML2ConfigurationException(
					"SSO response message could not be created, because there are not any ACS endpoints configured for SP: "
							+ trustedSP.getName() + " with binding: " + binding);
		}

		// Get the first ACS endpoint
		SAML2Endpoint spACSEndpoint = acsEndpoints.get(0);
		for (SAML2Endpoint endpoint : acsEndpoints) {
			if (endpoint != null && endpoint.isDefault()) {
				// get the default one
				spACSEndpoint = endpoint;
				break;
			}
		}

		return spACSEndpoint;
	}

	static String getACSLocation(SAML2TrustedSP trustedSP, SAML2Binding binding)
			throws SAML2ConfigurationException {
		// Get the ACS endpoint
		SAML2Endpoint spACSEndpoint = getACSEndpoint(trustedSP, binding);

		// Get the ACS response location
		String spACSLocation = spACSEndpoint.getResponseLocation();
		if (spACSLocation == null || spACSLocation.length() == 0) {
			// Get the ACS response location
			spACSLocation = spACSEndpoint.getLocation();
			if (spACSLocation == null || spACSLocation.length() == 0) {
				throw new SAML2ConfigurationException(
						"Neither response location, nor location is configured in ACS endpoint for trusted SP: "
								+ trustedSP.getName());
			}
		}

		if (LOCATION.beDebug()) {
			LOCATION.debugT("ACS endpoint location: {0}",
					new Object[] { spACSLocation });
		}

		return spACSLocation;
	}

	static void signSLORedirectPayload(SAML2LocalIdP localIdP,
			SAML2TrustedSP trustedSP, RedirectPayloadDeflate redirectPayload)
			throws SAML2Exception, SAML2ConfigurationException {
		SignatureOption signSLOMessages = trustedSP
				.isToSignSingleLogoutMessages();
		if (signSLOMessages == SignatureOption.ALWAYS
				|| signSLOMessages == SignatureOption.FRONT_CHANNEL_ONLY) {
			PrivateKey privateKey = localIdP.getPrivateKeyForSignature();
			if (privateKey == null) {
				throw new SAML2ConfigurationException(
						"Private key is not configured for local IdP.");
			}
			redirectPayload.sign(privateKey);
			LOCATION.debugT("SLO redirect payload successfully signed.");
		}
	}

	public static void signSLOMessage(SAML2LocalIdP localIdP,
			SAML2TrustedSP trustedSP, SAML2ProtocolToken logoutMessage)
			throws SAML2Exception, SAML2ConfigurationException {
		SignatureOption signSLOMessages = trustedSP
				.isToSignSingleLogoutMessages();
		if (signSLOMessages == SignatureOption.ALWAYS
				|| signSLOMessages == SignatureOption.FRONT_CHANNEL_ONLY) {
			PrivateKey privateKey = localIdP.getPrivateKeyForSignature();
			if (privateKey == null) {
				throw new SAML2ConfigurationException(
						"Private key is not configured for local IdP.");
			}
			if (localIdP.isToIncludeCertInSignature()) {
				logoutMessage.sign(privateKey,
						localIdP.getCertificateForSignature());
			} else {
				logoutMessage.sign(privateKey, null);
			}
			LOCATION.debugT("SLO message successfully signed.");
		}
	}

	public static PublicKey getTrustedSPPublicKeyForEncryption(SAML2TrustedSP trustedSP)
			throws SAML2ConfigurationException {
		PublicKey publicKey = trustedSP.getPublicKeyForEncryption();
		if (publicKey == null) {
			throw new SAML2ConfigurationException(
					"Could not read trusted Service Provider public key for encryption.");
		}
		return publicKey;
	}

	public static String getIssuer(SLORequestInfo sloRequestInfo)
			throws SAML2Exception {
		String trustedSPName = sloRequestInfo.getIssuer();
		if (trustedSPName == null || trustedSPName.length() == 0) {
			throw new SAML2Exception(
					"SLO response message could not be created, because sloRequestInfo does not contain issuer name.");
		}
		return trustedSPName;
	}

	public static void checkArgument(String methodName, String argName, Object argValue) {
		if (argValue == null) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(methodName).append(": ");
			sb.append("Method call failed as provided input parameter [")
					.append(argName).append("] has invalid value [")
					.append("null").append("].");
			throw new IllegalArgumentException(sb.toString());
		}
	}

	public static void checkStringArgument(String methodName, String argName,
			String argValue) {
		if (argValue == null || argValue.length() == 0) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(methodName).append(": ");
			sb.append("Method call failed as provided input parameter [")
					.append(argName).append("] has invalid value [")
					.append(argValue).append("].");
			throw new IllegalArgumentException(sb.toString());
		}
	}

	public static void checkNameId(String methodName, SAML2NameID nameId) {
		if (nameId == null || nameId.getName() == null
				|| nameId.getName().length() == 0) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(methodName).append(": ");
			sb.append(
					"Method call failed as provided input parameter [nameId] has invalid value [")
					.append(nameId).append("].");
			throw new IllegalArgumentException(sb.toString());
		}
	}

	public static void checkSessionIndexes(String methodName, List<String> si) {
		if (si == null || si.isEmpty()) {
			StringBuilder sb = new StringBuilder(100);
			sb.append(methodName).append(": ");
			sb.append(
					"Method call failed as provided input parameter [sessionIndexes] has invalid value [")
					.append(si).append("].");
			throw new IllegalArgumentException(sb.toString());
		}
	}

	public static SAML2LocalIdP getLocalIdP(SAML2IdPConfiguration config) {
		if (config == null) {
			throw new IllegalArgumentException(
					"Parameter configuration is null.");
		}

		SAML2LocalIdP localIdP = config.getLocalIdP();

		if (LOCATION.beInfo()) {
			LOCATION.infoT("Local Identity Provider configuration: \n{0}",
					new Object[] { localIdP });
		}

		return localIdP;
	}

	public static SAML2TrustedSP getTrustedSP(SAML2IdPConfiguration config,
			String spName) throws SAML2ConfigurationException {
		if (config == null) {
			throw new IllegalArgumentException(
					"Parameter configuration is null.");
		}

		SAML2TrustedSP trustedSP = config.getTrustedSP(spName);

		if (trustedSP == null) {
			throw new SAML2ConfigurationException(
					"Configuration for trusted SP [" + spName
							+ "] does not exist.");
		}

		if (!trustedSP.isEnabled()) {
			throw new SAML2ConfigurationException("Trusted SP [" + spName
					+ "] is not enabled.");
		}

		if (LOCATION.beInfo()) {
			LOCATION.infoT("Trusted Service Provider configuration: \n{0}",
					new Object[] { trustedSP });
		}

		return trustedSP;
	}
}
