package com.huoyun.idp.email.impl;

import java.util.Date;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.mail.EmailConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;

import com.huoyun.idp.email.EmailErrorCodes;
import com.huoyun.idp.email.EmailService;
import com.huoyun.idp.email.EmailTemplate;
import com.huoyun.idp.email.SmtpProperties;
import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.locale.LocaleService;

public class EmailServiceImpl implements EmailService {
	private static final Logger LOGGER = LoggerFactory.getLogger(EmailServiceImpl.class);

	private final static int SOCKET_TIMEOUT_MS = 10000;
	private final static int MAIL_SMTP_TIMEOUT = 20000;

	private SmtpProperties smtp;
	private TemplateEngine templateEngine;
	private JavaMailSenderImpl mailSender;
	private LocaleService localeService;

	public EmailServiceImpl(TemplateEngine templateEngine, SmtpProperties smtp, LocaleService localeService) {
		this.smtp = smtp;
		this.templateEngine = templateEngine;
		this.localeService = localeService;
	}

	@Override
	public void send(String to, EmailTemplate template) throws BusinessException {
		try {
			LOGGER.info("Start to sending mail ...");
			JavaMailSenderImpl sender = this.getMailSender();
			MimeMessage message = sender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message,EmailConstants.UTF_8);
			helper.setSentDate(new Date());
			InternetAddress from = new InternetAddress(this.smtp.getFrom());
			from.setPersonal(this.smtp.getFromDisplayName(), EmailConstants.UTF_8);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(template.getSubject(this.localeService));
			helper.setText(template.getHtml(this.templateEngine), true);
			sender.send(message);
			LOGGER.info("Send mail successfully.");
		} catch (Exception ex) {
			LOGGER.info("Send mail failed.", ex);
			throw new BusinessException(EmailErrorCodes.Send_Mail_Failed);
		}
	}

	private JavaMailSenderImpl getMailSender() {
		if (this.mailSender == null) {
			this.mailSender = new JavaMailSenderImpl();
			this.mailSender.setHost(smtp.getServer());
			this.mailSender.setPort(smtp.getPort());
			this.mailSender.setDefaultEncoding(EmailConstants.UTF_8);
			if (!StringUtils.isEmpty(smtp.getUserName()) && !StringUtils.isEmpty(smtp.getPassword())) {
				this.mailSender.setUsername(smtp.getUserName());
				this.mailSender.setPassword(smtp.getPassword());
			}

			Properties properties = new Properties();
			properties.setProperty(EmailConstants.MAIL_DEBUG, "true");
			properties.setProperty(EmailConstants.MAIL_SMTP_TIMEOUT, Integer.toString(MAIL_SMTP_TIMEOUT));
			properties.setProperty(EmailConstants.MAIL_SMTP_CONNECTIONTIMEOUT, Integer.toString(SOCKET_TIMEOUT_MS));

			this.mailSender.setJavaMailProperties(properties);
		}

		return this.mailSender;
	}

}
