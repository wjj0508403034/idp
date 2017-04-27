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

import com.sap.security.saml2.cfg.custom.SAML2LocalIdPCustomImpl;
import com.sap.security.saml2.cfg.enums.SAML2Binding;

public class SAML2IdPConfigurationFactory {

	private final Logger logger = LoggerFactory
			.getLogger(SAML2IdPConfigurationFactory.class);

	private SAML2LocalIdPCustomImpl localIdP = null;

	public void init() {
		logger.info("init start...");

		InputStream certRawInputStream = null;
		InputStream privateKeyInputStream = null;
		try {
			String saml2PublicKey = "LS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUNoVENDQWU0Q0NRQys4eUNRdldLNUVUQU5CZ2txaGtpRzl3MEJBUXNGQURDQmhqRUxNQWtHQTFVRUJoTUMKUkVVeEREQUtCZ05WQkFnTUExaFlXREVNTUFvR0ExVUVCd3dEV0ZoWU1ROHdEUVlEVlFRS0RBWlRRVkF0VTBVeApEREFLQmdOVkJBc01BMU5OUlRFWU1CWUdBMVVFQXd3UEtpNXpiV1ZqTG5OaGNDNWpiM0p3TVNJd0lBWUpLb1pJCmh2Y05BUWtCRmhOMFpYTjBRSE5oY0dGdWVYZG9aWEpsTG1OdU1CNFhEVEUzTURJd016QXlOVE0wTTFvWERUSTMKTURJd01UQXlOVE0wTTFvd2dZWXhDekFKQmdOVkJBWVRBa1JGTVF3d0NnWURWUVFJREFOWVdGZ3hEREFLQmdOVgpCQWNNQTFoWVdERVBNQTBHQTFVRUNnd0dVMEZRTFZORk1Rd3dDZ1lEVlFRTERBTlRUVVV4R0RBV0JnTlZCQU1NCkR5b3VjMjFsWXk1ellYQXVZMjl5Y0RFaU1DQUdDU3FHU0liM0RRRUpBUllUZEdWemRFQnpZWEJoYm5sM2FHVnkKWlM1amJqQ0JuekFOQmdrcWhraUc5dzBCQVFFRkFBT0JqUUF3Z1lrQ2dZRUE3VDFRQTZkRC9RVmoxcEpPS2Z4MApxeVBZZWc0UjlkMVd6aFJaN09yejZLd0FHQVVNdVRDWEFHSlBDclJhMHdCUXl4ZytIOWV1K3R5K1pFdXpYOFpVCnA3N2pxRzhSTW5zeE14YkNwUkFXMXlZRitMVERzcVZzS2l1Y2ZqcXZkZ1dnZHZTM0hjWDREcHRDbVcwRmlKOWsKUEd0aFE0VmRuc2lhNFBYdE9tbmd3bzhDQXdFQUFUQU5CZ2txaGtpRzl3MEJBUXNGQUFPQmdRRHB2L0x3V2pmTAorOVJJWWtsRHMzSml1UlROanFFaHgvWkE5bXZXLzlwMTFsUTRHUWZJVktOeFZHL0JmNS9MQnkwa2czekxIaDlRCkloVDFwdnNvZVVPUHhUVGgwN0JqbmRPOU1wcnRKYVFFYzJjY29sYTErWTFLTmw5ampjZU1GMHgyYmRZZnExRmcKU0xGM2o4dnVWbDc5RUF5cmxsN21laGZEbUFHY2RFQUZxdz09Ci0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0K";
			byte[] certRaw = Base64.decodeBase64(saml2PublicKey);

			String saml2PrivateKey = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAO09UAOnQ/0FY9aSTin8dKsj2HoOEfXdVs4UWezq8+isABgFDLkwlwBiTwq0WtMAUMsYPh/XrvrcvmRLs1/GVKe+46hvETJ7MTMWwqUQFtcmBfi0w7KlbCornH46r3YFoHb0tx3F+A6bQpltBYifZDxrYUOFXZ7ImuD17Tpp4MKPAgMBAAECgYEAovvcXKYtL0bksCUTTgUCohCeSDwiWqcVW77fiMRhjRedeOL/OrsHAlyHsPL28r7kwqKFC5tBu1ar9nuLX+EBhmnjXPj7C8hBKGb0iHD7gqU+hKfjtczEmk0cqDFDKm0cT3Y/Qrekyg2ifRnSjhhcLe20dGdAEzGOlALOD414/QECQQD/35vuIBZK0YknHafsGx+ZsxLz1zOaU46l4SxCO3FrPtL8RP2FfyP2KL6hIWioA+qPLVyv4D8IyFb2GzZK3sV7AkEA7VtYNu9DeIiB3I60JfZUM1hMswxZpoLTGPqESnJnq/3z6/1bEpQy9IprSJ42SKX4SSz5x+jBLEDEfM50aHdI/QJAM/M4e350SDiGujRhNaTEI7ah8HQO/BRe7/rpu4DKJFYbDDENsB8CNCZNnfVkhEhXRT6WhPbolWXnkgwwV78nJQJARLyWQFpr3KUTjm7ZJsE+QxxIIfXqpHjwbyRlWTXmZf2GPQpC7I90bjMDvKTsrX869t4Ke+UgxBMBk+8p8PLSdQJAdJ8VX2WNiOedulQuoDz5qjV1uCYEEbKBTvE+YW88UtBOtqbJ3BVFqRSnaFf7HR2qBJD8ClxrNg6SFaAZYKUg2g==";
			byte[] privateKeyRaw = Base64.decodeBase64(saml2PrivateKey);

			CertificateFactory certFactory = CertificateFactory
					.getInstance("X.509");
			X509Certificate cert = (X509Certificate) certFactory
					.generateCertificate(new ByteArrayInputStream(certRaw));

			// private key must be pkcs8 format
			PKCS8EncodedKeySpec kspec = new PKCS8EncodedKeySpec(privateKeyRaw);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			PrivateKey privKey = kf.generatePrivate(kspec);

			// init localIdp
			String idpDomain = "HuoYunIDP";
			logger.info("the IDP DOMAIN LOAD FROM CMT is: '{}'", idpDomain);

			localIdP = new SAML2LocalIdPCustomImpl(idpDomain, privKey, privKey);
			localIdP.setSigningCertificate(cert);
			localIdP.setIsToSignMetadata(true);
			localIdP.setSingleSignOnLocation("/sld/saml2/idp/sso");
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
}
