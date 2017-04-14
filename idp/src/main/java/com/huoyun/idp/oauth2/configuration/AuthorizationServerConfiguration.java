package com.huoyun.idp.oauth2.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.oauth2.services.impl.AuthorizationCodeServiceImpl;
import com.huoyun.idp.oauth2.services.impl.ClientDetailsServiceImpl;
import com.huoyun.idp.oauth2.services.impl.TokenStoreImpl;

@EnableAuthorizationServer
//@Configuration
@EnableResourceServer
public class AuthorizationServerConfiguration implements
		AuthorizationServerConfigurer {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserDetailsService userDetailsService;

	@Autowired
	private Facade facade;

	@Bean
	public TokenStore tokenStore() {
		return new TokenStoreImpl(facade);
	}

	@Bean
	public ClientDetailsService clientDetailsService() {
		return new ClientDetailsServiceImpl(facade);
	}

	@Bean
	protected AuthorizationCodeServices authorizationCodeServices() {
		return new AuthorizationCodeServiceImpl(facade);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security)
			throws Exception {
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients)
			throws Exception {
		clients.withClientDetails(clientDetailsService());
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		endpoints.userDetailsService(userDetailsService)
				.authorizationCodeServices(authorizationCodeServices())
				.authenticationManager(this.authenticationManager)
				.tokenStore(tokenStore()).approvalStoreDisabled();
	}
}
