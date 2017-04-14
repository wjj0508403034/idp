package com.huoyun.idp.session;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huoyun.idp.redis.RedisSessionManager;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;

@Configuration
public class SessionAutoConfiguration {

	@Bean
	public SessionManager sessionManager(
			SAML2IdPConfigurationFactory idpConfigurationFactory,
			RedisSessionManager redisSessionManager) {
		return new SessionManager(idpConfigurationFactory, redisSessionManager);
	}
}
