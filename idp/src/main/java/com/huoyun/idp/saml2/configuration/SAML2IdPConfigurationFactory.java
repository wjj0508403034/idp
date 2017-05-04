package com.huoyun.idp.saml2.configuration;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huoyun.idp.saml2.SAML2Properties;
import com.sap.security.saml2.cfg.custom.SAML2LocalIdPCustomImpl;
import com.sap.security.saml2.cfg.enums.SAML2Binding;

public class SAML2IdPConfigurationFactory {

	private final Logger logger = LoggerFactory
			.getLogger(SAML2IdPConfigurationFactory.class);

	private SAML2LocalIdPCustomImpl localIdP = null;
	
	private SAML2Properties saml2Properties;
	
	public SAML2IdPConfigurationFactory(SAML2Properties saml2Properties){
		this.saml2Properties = saml2Properties;
	}

	public void init() {
		logger.info("init start...");

		InputStream certRawInputStream = null;
		InputStream privateKeyInputStream = null;
		try {
			byte[] certRaw = Base64.decodeBase64(this.saml2Properties.getPublicKey());
			byte[] privateKeyRaw = Base64.decodeBase64(this.saml2Properties.getPrivateKey());

			CertificateFactory certFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(certRaw));

			// private key must be pkcs8 format
			PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(privateKeyRaw);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privKey = kf.generatePrivate(kspec);

			String idpDomain = this.saml2Properties.getIdpDomain();

			localIdP = new SAML2LocalIdPCustomImpl(idpDomain, privKey, privKey);
			localIdP.setSigningCertificate(cert);
			localIdP.setIsToSignMetadata(true);
			localIdP.setSingleSignOnLocation(this.saml2Properties.getSsoLocation());
			localIdP.getSingleSignOnSupportedBindings().add(
					SAML2Binding.HTTP_POST_BINDING);
			SAML2IdPConfigurationCustomImpl.setLocalIdP(localIdP);
		} catch (Exception e) {
			logger.error(
					"\nFatal Error!!!!!!\nInit localIdP failed!!!!!\nThis will cause login and logout problems.",
					e);
		} finally {
			IOUtils.closeQuietly(certRawInputStream);
			IOUtils.closeQuietly(privateKeyInputStream);
			logger.info("init end...");
		}
	}

	public SAML2IdPConfigurationCustomImpl getDefaultSAML2IdpConfiguration() {
		logger.info("getDefaultSAML2IdpConfiguration start...");

		SAML2IdPConfigurationCustomImpl configuration = new SAML2IdPConfigurationCustomImpl();

		if (configuration.getLocalIdP() == null) {
			synchronized (this) {
				if (configuration.getLocalIdP() == null) {
					init();
				}
			}
		}

		logger.info("getDefaultSAML2IdpConfiguration end...");
		return configuration;
	}
	
	public String getDefaultSPLocation(){
		return this.saml2Properties.getDefaultSpLocation();
	}
}
