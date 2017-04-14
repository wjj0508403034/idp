package com.huoyun.idp.saml2.configuration;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.sap.security.saml2.cfg.custom.SAML2ConfigurationCustomConstants;
import com.sap.security.saml2.cfg.custom.SAML2EndpointCustomImpl;
import com.sap.security.saml2.cfg.custom.SAML2ProviderCustomImpl;
import com.sap.security.saml2.cfg.enums.DigestAlgorithm;
import com.sap.security.saml2.cfg.enums.EncryptionAlgorithm;
import com.sap.security.saml2.cfg.enums.EncryptionOption;
import com.sap.security.saml2.cfg.enums.SAML2Binding;
import com.sap.security.saml2.cfg.enums.SAML2NameIdFormat;
import com.sap.security.saml2.cfg.enums.SAML2Service;
import com.sap.security.saml2.cfg.enums.SignatureOption;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.read.SAML2Endpoint;
import com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatEmail;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatKerberosTrustedSP;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatPersistentTrustedSP;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatTransientTrustedSP;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatUnspecified;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatWindows;
import com.sap.security.saml2.cfg.interfaces.read.nameidformat.SAML2NameIdFormatX509TrustedSP;
import com.sap.security.saml2.cfg.interfaces.write.SAML2ModifiableTrustedSP;
import com.sap.security.saml2.cfg.util.CfgConstants;

public class SAML2TrustedSPSBOCustomImpl extends SAML2ProviderCustomImpl
		implements SAML2TrustedSP {
	protected static final List<SAML2Endpoint> EMPTY_LIST_SAML2_ENDPOINTS = new ArrayList<SAML2Endpoint>(
			0);

	private SAML2NameIdFormat defaultNameIdFormat;
	private X509Certificate secondarySigningCertificate;
	private SignatureOption signARSMessages;
	private SignatureOption requireSignedARSMessages;
	private SignatureOption signSLOMessages;
	private EncryptionAlgorithm encryptionAlgorithm;
	private DigestAlgorithm signatureAlgorithm;

	private EncryptionOption authnResponseElementsToEncrypt;

	private SignatureOption signAuthnResponses;
	private SignatureOption signAssertions;
	private SignatureOption requireSignedAuthnRequests;

	private Collection<SAML2Endpoint> endpoints;

	public SAML2TrustedSPSBOCustomImpl(String name, String acsLocationUrl,
			String sloLocationUrl) {
		super(name);
		this.endpoints = convertToFrontChannelEndpoints(acsLocationUrl,
				sloLocationUrl);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getARSEndpoints()
	 */
	public Collection<SAML2Endpoint> getARSEndpoints() {
		return getEndpoints(SAML2Service.ARTIFACT_RESOLUTION_SERVICE);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getAllEndpoints()
	 */
	public Collection<SAML2Endpoint> getAllEndpoints() {
		if (endpoints != null) {
			return endpoints;
		}
		return EMPTY_LIST_SAML2_ENDPOINTS;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getArtifactResolutionEndpoint(int)
	 */
	public SAML2Endpoint getArtifactResolutionEndpoint(int index) {
		if (endpoints != null) {
			for (SAML2Endpoint endpoint : endpoints) {
				if (endpoint != null
						&& SAML2Service.ARTIFACT_RESOLUTION_SERVICE == endpoint
								.getService() && index == endpoint.getIndex()) {
					return endpoint;
				}
			}
		}
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getDefaultNameIdFormat()
	 */
	public SAML2NameIdFormat getDefaultNameIdFormat() {
		return defaultNameIdFormat;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getDefaultSingleLogoutEndpoint()
	 */
	public SAML2Endpoint getDefaultSingleLogoutEndpoint() {
		if (endpoints != null) {
			for (SAML2Endpoint endpoint : endpoints) {
				if (endpoint != null
						&& SAML2Service.SINGLE_LOGOUT_SERVICE == endpoint
								.getService() && endpoint.isDefault()) {
					return endpoint;
				}
			}
		}
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getDescription()
	 */
	public String getDescription() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getEncryptionAlgorithm()
	 */
	public EncryptionAlgorithm getEncryptionAlgorithm() {
		if (encryptionAlgorithm != null) {
			return encryptionAlgorithm;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPTION_ALGORITHM;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getEncryptionAlgorithm()
	 */
	public DigestAlgorithm getDigestAlgorithm() {
		if (signatureAlgorithm != null) {
			return signatureAlgorithm;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGNATURE_ALGORITHM;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getEncryptionCertificateName()
	 */
	public String getEncryptionCertificateName() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getSLOEndpoints()
	 */
	public Collection<SAML2Endpoint> getSLOEndpoints() {
		return getEndpoints(SAML2Service.SINGLE_LOGOUT_SERVICE);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getSecondaryCertificateForSignature()
	 */
	public Certificate getSecondaryCertificateForSignature()
			throws SAML2ConfigurationException {
		return secondarySigningCertificate;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getSecondarySigningCertificateName()
	 */
	public String getSecondarySigningCertificateName() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getSigningCertificateName()
	 */
	public String getSigningCertificateName() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#getSingleLogoutEndpoints(com.sap.security.saml2.cfg.enums.SAML2Binding)
	 */
	public List<SAML2Endpoint> getSingleLogoutEndpoints(SAML2Binding binding) {
		return getEndpoints(SAML2Service.SINGLE_LOGOUT_SERVICE, binding);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToEncryptSingleLogoutSubject()
	 */
	public boolean isToEncryptSingleLogoutSubject() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_SLO_SUBJECT;
	}

	@Override
	public boolean isToEncryptManageNameIDNewID() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_MNI_NEW_ID;
	}

	@Override
	public boolean isToEncryptManageNameIDSubject() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_MNI_SUBJECT;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToRequireEncryptedSingleLogoutSubject()
	 */
	public boolean isToRequireEncryptedSingleLogoutSubject() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_SLO_SUBJECT;
	}

	@Override
	public boolean isToRequireEncryptedManageNameIDNewID() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_MNI_NEW_ID;
	}

	@Override
	public boolean isToRequireEncryptedManageNameIDSubject() {
		return SAML2ConfigurationCustomConstants.DEFAULT_ENCRYPT_OR_REQUIRE_MNI_SUBJECT;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToRequireSignedArtifactResolutionMessages()
	 */
	public SignatureOption isToRequireSignedArtifactResolutionMessages() {
		if (requireSignedARSMessages != null) {
			return requireSignedARSMessages;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_ARS_MESSAGES;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToRequireSignedSingleLogoutMessages()
	 */
	public SignatureOption isToRequireSignedSingleLogoutMessages() {
		return SignatureOption.NEVER;
	}

	@Override
	public SignatureOption isToRequireSignedManageNameIDMessages() {
		return CfgConstants.DEFAULT_SIGN_OR_REQUIRE_MNI_MESSAGES;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToSignArtifactResolutionMessages()
	 */
	public SignatureOption isToSignArtifactResolutionMessages() {
		if (signARSMessages != null) {
			return signARSMessages;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_ARS_MESSAGES;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedProvider#isToSignSingleLogoutMessages()
	 */
	public SignatureOption isToSignSingleLogoutMessages() {
		if (signSLOMessages != null) {
			return signSLOMessages;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_SLO_MESSAGES;
	}

	@Override
	public SignatureOption isToSignManageNameIDMessages() {
		return CfgConstants.DEFAULT_SIGN_OR_REQUIRE_MNI_MESSAGES;
	}

	protected void setDefaultNameIdFormat(SAML2NameIdFormat defaultNameIdFormat) {
		this.defaultNameIdFormat = defaultNameIdFormat;
	}

	/**
	 * @return
	 */
	protected Collection<SAML2Endpoint> getEndpoints(SAML2Service service) {
		if (endpoints != null) {
			List<SAML2Endpoint> result = new ArrayList<SAML2Endpoint>();
			for (SAML2Endpoint endpoint : endpoints) {
				if (endpoint != null && service == endpoint.getService()) {
					result.add(endpoint);
				}
			}
			return result;
		}
		return EMPTY_LIST_SAML2_ENDPOINTS;
	}

	/**
	 * @param service
	 * @param binding
	 * @return
	 */
	protected List<SAML2Endpoint> getEndpoints(SAML2Service service,
			SAML2Binding binding) {
		if (endpoints != null) {
			List<SAML2Endpoint> result = new ArrayList<SAML2Endpoint>();
			for (SAML2Endpoint endpoint : endpoints) {
				if (endpoint != null && service == endpoint.getService()
						&& endpoint.getBinding() == binding) {
					result.add(endpoint);
				}
			}
			return result;
		}
		return EMPTY_LIST_SAML2_ENDPOINTS;
	}

	@Override
	public Collection<SAML2Endpoint> getMNIEndpoints() {
		return EMPTY_LIST_SAML2_ENDPOINTS;
	}

	@Override
	public List<SAML2Endpoint> getManageNameIDEndpoints(SAML2Binding binding) {
		return EMPTY_LIST_SAML2_ENDPOINTS;
	}

	@Override
	public SAML2Endpoint getDefaultManageNameIDEndpoint() {
		return null;
	}

	@Override
	public Collection<SAML2Endpoint> getACSEndpoints() {
		return getEndpoints(SAML2Service.ASSERTION_CONSUMER_SERVICE);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getAssertionConsumerEndpoint(int)
	 */
	@Override
	public SAML2Endpoint getAssertionConsumerEndpoint(int index) {
		for (SAML2Endpoint endpoint : getACSEndpoints()) {
			if (index == endpoint.getIndex()) {
				return endpoint;
			}
		}
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getAssertionConsumerEndpoints(com.sap.security.saml2.cfg.enums.SAML2Binding)
	 */
	@Override
	public List<SAML2Endpoint> getAssertionConsumerEndpoints(
			SAML2Binding binding) {
		return getEndpoints(SAML2Service.ASSERTION_CONSUMER_SERVICE, binding);
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getAuthnResponseElementsToEncrypt()
	 */
	@Override
	public EncryptionOption getAuthnResponseElementsToEncrypt() {
		if (authnResponseElementsToEncrypt != null) {
			return authnResponseElementsToEncrypt;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_AUTHN_RESPONSE_ENCRYPT_ELEMENTS;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getDefaultAssertionConsumerEndpoint()
	 */
	@Override
	public SAML2Endpoint getDefaultAssertionConsumerEndpoint() {
		for (SAML2Endpoint endpoint : getACSEndpoints()) {
			if (endpoint.isDefault()) {
				return endpoint;
			}
		}
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getModifiableTrustedSP()
	 */
	@Override
	public SAML2ModifiableTrustedSP getModifiableTrustedSP()
			throws SAML2ConfigurationException {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatEmail()
	 */
	@Override
	public SAML2NameIdFormatEmail getNameIdFormatEmail() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatKerberos()
	 */
	@Override
	public SAML2NameIdFormatKerberosTrustedSP getNameIdFormatKerberos() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatPersistent()
	 */
	@Override
	public SAML2NameIdFormatPersistentTrustedSP getNameIdFormatPersistent() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatTransient()
	 */
	@Override
	public SAML2NameIdFormatTransientTrustedSP getNameIdFormatTransient() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatUnspecified()
	 */
	@Override
	public SAML2NameIdFormatUnspecified getNameIdFormatUnspecified() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatWindows()
	 */
	@Override
	public SAML2NameIdFormatWindows getNameIdFormatWindows() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getNameIdFormatX509()
	 */
	@Override
	public SAML2NameIdFormatX509TrustedSP getNameIdFormatX509() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getSPProvidedIDUMEAttributeName()
	 */
	@Override
	public String getSPProvidedIDUMEAttributeName() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#getSPProvidedIDUMEAttributeNamespace()
	 */
	@Override
	public String getSPProvidedIDUMEAttributeNamespace() {
		return null;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#isToRequireSignedAuthnRequests()
	 */
	@Override
	public SignatureOption isToRequireSignedAuthnRequests() {
		if (requireSignedAuthnRequests != null) {
			return requireSignedAuthnRequests;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_AUTHN_REQUESTS;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#isToSignAssertions()
	 */
	@Override
	public SignatureOption isToSignAssertions() {
		if (signAssertions != null) {
			return signAssertions;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_ASSERTIONS;
	}

	/**
	 * @see com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP#isToSignAuthnResponses()
	 */
	@Override
	public SignatureOption isToSignAuthnResponses() {
		if (signAuthnResponses != null) {
			return signAuthnResponses;
		}
		return SAML2ConfigurationCustomConstants.DEFAULT_SIGN_OR_REQUIRE_AUTHN_RESPONSES;
	}

	private static Collection<SAML2Endpoint> convertToFrontChannelEndpoints(
			String acsLocationUrl, String sloLocationUrl) {

		Collection<SAML2Endpoint> result = new ArrayList<SAML2Endpoint>();

		// acs endpoints
		if (acsLocationUrl != null && acsLocationUrl.length() > 0) {
			SAML2EndpointCustomImpl defaultACSEndpoint = new SAML2EndpointCustomImpl(
					SAML2Service.ASSERTION_CONSUMER_SERVICE,
					SAML2Binding.HTTP_POST_BINDING, acsLocationUrl);
			defaultACSEndpoint.setDefault(true);
			result.add(defaultACSEndpoint);
		}

		if (sloLocationUrl != null && sloLocationUrl.length() > 0) {
			// slo endpoints, default is redirect binding
			SAML2EndpointCustomImpl defaltSLOEndpoint = new SAML2EndpointCustomImpl(
					SAML2Service.SINGLE_LOGOUT_SERVICE,
					SAML2Binding.HTTP_REDIRECT_BINDING, sloLocationUrl);
			defaltSLOEndpoint.setDefault(true);
			result.add(defaltSLOEndpoint);
			result.add(new SAML2EndpointCustomImpl(
					SAML2Service.SINGLE_LOGOUT_SERVICE,
					SAML2Binding.HTTP_POST_BINDING, sloLocationUrl));
		}

		return result;
	}
}
