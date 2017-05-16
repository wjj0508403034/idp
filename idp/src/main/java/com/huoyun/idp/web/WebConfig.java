package com.huoyun.idp.web;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;

import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.embedded.ServletListenerRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.internal.api.InternalApiAuthenticationFilter;
import com.huoyun.idp.redis.RedisSessionRepositoryFilter;

@Configuration
public class WebConfig extends WebMvcConfigurerAdapter {

	@Bean
	public FilterRegistrationBean someFilterRegistration(RedisSessionRepositoryFilter redisSessionRepositoryFilter) {

		FilterRegistrationBean registration = new FilterRegistrationBean();
		registration.setFilter(redisSessionRepositoryFilter);
		registration.addUrlPatterns("/*");
		registration.setOrder(1);
		return registration;
	}

	@Bean
	public RedisSessionRepositoryFilter redisSessionRepositoryFilter(ApplicationContext context) {
		return new RedisSessionRepositoryFilter(context);
	}

	@Bean
	public ConfigureRedisAction configureRedisAction() {
		return ConfigureRedisAction.NO_OP;
	}

	@Bean
	public ServletListenerRegistrationBean<EventListener> getHttpSessionListener() {
		ServletListenerRegistrationBean<EventListener> registrationBean = new ServletListenerRegistrationBean<>();
		registrationBean.setListener(new HttpSessionEventPublisher());
		return registrationBean;
	}
	
	@Bean
	public FilterRegistrationBean loginRequiredFilter(
			Facade facade) {
		InternalApiAuthenticationFilter filter = new InternalApiAuthenticationFilter();
		filter.setFacade(facade);
		FilterRegistrationBean registrationBean = new FilterRegistrationBean();
		registrationBean.setFilter(filter);
		List<String> urlPatterns = new ArrayList<String>();
		urlPatterns.add("/api/*");
		registrationBean.setUrlPatterns(urlPatterns);
		registrationBean.setName("InternalApiFilter");
		registrationBean.setOrder(1);
		return registrationBean;
	}

	@Primary
	@Bean
	public SpringTemplateEngine templateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(htmlTemplateResolver());
		return templateEngine;
	}

	private ClassLoaderTemplateResolver htmlTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix("templates/");
		templateResolver.setTemplateMode("LEGACYHTML5");
		templateResolver.setSuffix(".html");
		templateResolver.setOrder(1);
		templateResolver.setCacheable(false);
		return templateResolver;
	}

}
