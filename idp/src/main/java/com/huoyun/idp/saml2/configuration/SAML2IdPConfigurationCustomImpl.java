package com.huoyun.idp.saml2.configuration;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.sap.security.saml2.cfg.interfaces.SAML2IdPConfiguration;
import com.sap.security.saml2.cfg.interfaces.read.SAML2LocalIdP;
import com.sap.security.saml2.cfg.interfaces.read.SAML2TrustedSP;

public class SAML2IdPConfigurationCustomImpl implements SAML2IdPConfiguration,
		Serializable {

	private static final long serialVersionUID = -5494993873156280115L;
	private static SAML2LocalIdP localIdP;
	private transient Map<String, SAML2TrustedSP> trustedSPs = new HashMap<String, SAML2TrustedSP>();
	private final Map<String, SAML2SPEndpoints> trustedSPEndpoints = new HashMap<String, SAML2SPEndpoints>();

	public SAML2IdPConfigurationCustomImpl() {
	}

	public SAML2TrustedSP addSP(String trustedSPName, String trustedSPACSUrl) {
		String sloUrl = StringUtils.EMPTY;
		if (trustedSPACSUrl.contains("/acs")) {
			sloUrl = trustedSPACSUrl.substring(0,
					trustedSPACSUrl.lastIndexOf("/acs"))
					+ "/slo";
		} else if (trustedSPACSUrl.contains("/login/callback")) {
			sloUrl = trustedSPACSUrl.substring(0,
					trustedSPACSUrl.lastIndexOf("/login/callback"))
					+ "/logout/callback";
		}
		SAML2TrustedSPSBOCustomImpl trustedSP = new SAML2TrustedSPSBOCustomImpl(
				trustedSPName, trustedSPACSUrl, sloUrl);
		trustedSPs.put(trustedSPName, trustedSP);
		trustedSPEndpoints.put(trustedSPName, new SAML2SPEndpoints(
				trustedSPACSUrl, sloUrl));
		return trustedSP;

	}

	@Override
	public SAML2LocalIdP getLocalIdP() {
		return localIdP;
	}

	@Override
	public SAML2TrustedSP getTrustedSP(String name) {
		return trustedSPs.get(name);
	}

	@Override
	public SAML2TrustedSP getTrustedSP(byte[] id) {
		// this configuration currently does not support resolving of artifacts
		return null;
	}

	private void readObject(java.io.ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		in.defaultReadObject();
		trustedSPs = new HashMap<String, SAML2TrustedSP>();
		for (String trustedSPName : trustedSPEndpoints.keySet()) {
			SAML2TrustedSPSBOCustomImpl trustedSP = new SAML2TrustedSPSBOCustomImpl(
					trustedSPName, trustedSPEndpoints.get(trustedSPName)
							.getAcs(), trustedSPEndpoints.get(trustedSPName)
							.getSlo());
			trustedSPs.put(trustedSPName, trustedSP);
		}
	}

	/**
	 * Please share your advice
	 * 
	 * @param l
	 */
	static void setLocalIdP(SAML2LocalIdP l) {
		localIdP = l;
	}

}
