package com.huoyun.idp.saml2.slo;

import java.util.List;

import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.commons.slo.SLORequestInfo;
import com.sap.security.saml2.idp.api.LogoutResponseData;
import com.sap.security.saml2.lib.common.SAML2Exception;
import com.sap.security.saml2.lib.interfaces.assertions.SAML2NameID;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutRequest;
import com.sap.security.saml2.lib.interfaces.protocols.SAML2LogoutResponse;

public interface SingleLogoutService {

	SAML2LogoutRequest createSLORequest(SAML2IdPConfiguration configuration,
			String spName, String relayState, SAML2NameID nameId,
			List<String> sessionIndexes, String sloRequestId)
			throws SAML2Exception, SAML2ConfigurationException;

	SAML2LogoutResponse createSLOResponse(SAML2IdPConfiguration configuration,
			SLORequestInfo sloRequestInfo) throws SAML2Exception,
			SAML2ConfigurationException;

	SAML2LogoutResponse createSLOResponse(SAML2IdPConfiguration configuration,
			String spName, LogoutResponseData logoutResponseData)
			throws SAML2Exception, SAML2ConfigurationException;

	

}
