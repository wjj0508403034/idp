package com.huoyun.idp.email;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring4.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.huoyun.idp.email.impl.EmailServiceImpl;
import com.huoyun.idp.locale.LocaleService;

@Configuration
public class EmailAutoConfiguration {

	@Bean
	public EmailService emailService(SmtpProperties smtp, LocaleService localeService) {
		return new EmailServiceImpl(emailTemplateEngine(), smtp, localeService);
	}

	private SpringTemplateEngine emailTemplateEngine() {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.addTemplateResolver(emailTemplateResolver());
		return templateEngine;
	}

	private ClassLoaderTemplateResolver emailTemplateResolver() {
		ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
		templateResolver.setPrefix("EMAIL-TEMPLATES/");
		templateResolver.setTemplateMode("LEGACYHTML5");
		templateResolver.setSuffix(".html");
		templateResolver.setOrder(1);
		templateResolver.setCacheable(false);
		return templateResolver;
	}
}
