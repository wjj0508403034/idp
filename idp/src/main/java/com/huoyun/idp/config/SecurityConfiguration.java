package com.huoyun.idp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.huoyun.idp.sso.SSOFilter;
import com.huoyun.idp.user.impl.UserDetailsServiceImpl;

//@Configuration
//@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Bean
	@Override
	public UserDetailsService userDetailsService() {
		return new UserDetailsServiceImpl();
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable();

//		http.authorizeRequests()
//				.regexMatchers(HttpMethod.POST, "/saml2/idp/ssojson/*","/saml2/idp/ssojsonlogin/*")
//				.permitAll().anyRequest().authenticated().and().formLogin()
//				.loginPage("/index").permitAll().and().logout()
//				.permitAll();

		http.addFilterAfter(new SSOFilter(), BasicAuthenticationFilter.class);
		// http.authorizeRequests().antMatchers().permitAll().anyRequest().authenticated();
		// http.formLogin().loginPage("/login.html").loginProcessingUrl("/login")
		// .failureUrl("/login.html?error").permitAll();
		// http.logout().logoutUrl("/logout").logoutSuccessUrl("/login.html")
		// .permitAll();
		// http.sessionManagement().maximumSessions(1).expiredUrl("/expired");
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService()).passwordEncoder(
				passwordEncoder);
	}
}
