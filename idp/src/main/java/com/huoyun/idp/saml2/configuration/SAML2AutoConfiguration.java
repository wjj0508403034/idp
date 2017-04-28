package com.huoyun.idp.saml2.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huoyun.idp.saml2.SAML2Properties;
import com.huoyun.idp.saml2.slo.SingleLogoutService;
import com.huoyun.idp.saml2.slo.impl.SingleLogoutServiceImpl;
import com.huoyun.idp.saml2.utils.SAML2BuilderFactory;
import com.sap.security.saml2.cfg.metadata.SAML2MetadataGenerator;

@Configuration
public class SAML2AutoConfiguration {

	@Bean
	public SAML2MetadataGenerator metadataGenerator() {
		return SAML2MetadataGenerator.getInstance();
	}
	
	@Bean
	public SAML2IdPConfigurationFactory configurationFactory(SAML2Properties saml2Properties){
		return new SAML2IdPConfigurationFactory(saml2Properties);
	}
	
	@Bean
	public SAML2BuilderFactory builderFactory(){
		return new SAML2BuilderFactory();
	}
	
	@Bean
	public SingleLogoutService singleLogoutService(SAML2BuilderFactory builderFactory){
		return new SingleLogoutServiceImpl(builderFactory);
	}
}
