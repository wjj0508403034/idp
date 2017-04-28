package com.huoyun.idp.saml2;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(locations = "classpath:/META-INF/SAML2.properties", prefix = "SAML2")
@Configuration
public class SAML2Properties {

	private String publicKey;
	private String privateKey;
	private String idpDomain;
	private String ssoLocation;
	public String getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public String getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	public String getIdpDomain() {
		return idpDomain;
	}
	public void setIdpDomain(String idpDomain) {
		this.idpDomain = idpDomain;
	}
	public String getSsoLocation() {
		return ssoLocation;
	}
	public void setSsoLocation(String ssoLocation) {
		this.ssoLocation = ssoLocation;
	}
	
}
