package com.huoyun.idp.sso;

import java.io.IOException;
import java.util.Locale;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.LocaleUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

public class SSOFilter extends GenericFilterBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SSOFilter.class);

	public static final String DISABLE_OLD_SSO = "Global.IDP.DisableOldSSO";
	public static final String X_LOCALE = "X-Locale";

	private FilterConfig filterConfig;

	public String localSessionAttributeName = null;


	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		long startTime = System.currentTimeMillis();

		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String method = httpRequest.getMethod();
		String requestURL = httpRequest.getRequestURL().toString();
		LOGGER.info("IDP Request start, method: [{}], url: [{}].", method,
				requestURL);

		String disableSSO = "true";
		boolean isDisable = StringUtils.isNotBlank(disableSSO)
				&& disableSSO.equalsIgnoreCase("true");
		if (isDisable) {
			String uri = httpRequest.getRequestURI();
			if (uri.endsWith("/saml2/idp/sso")
					|| uri.endsWith("/saml2/idp/changepassword")) {
				HttpServletResponse httpResp = (HttpServletResponse) response;
				httpResp.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return;
			}
		}

		syncLocale(httpRequest);

		chain.doFilter(httpRequest, response);
		long stopWatch = System.currentTimeMillis() - startTime;
		LOGGER.info("IDP Request end, method: [{}], url: [{}], cost: [{}]ms.",
				method, requestURL, stopWatch);

	}

	public void syncLocale(HttpServletRequest httpRequest) {

		HttpSession session = httpRequest.getSession(true);

		String requestParameterLocaleStr = httpRequest.getParameter("locale");
		String xHeaderLocaleStr = httpRequest
				.getHeader(X_LOCALE);
		String acceptedLanguageHeaderLocaleStr = httpRequest.getLocale()
				.toString();
		Locale sessionLocale = (Locale) session.getAttribute("locale");
		LOGGER.info("locale info befor validatation: {} / {} / {} / {}",
				requestParameterLocaleStr, xHeaderLocaleStr,
				acceptedLanguageHeaderLocaleStr, sessionLocale);
		Locale requestParameterLocale = validateLocale(requestParameterLocaleStr);
		Locale xHeaderLocale = validateLocale(xHeaderLocaleStr);
		Locale acceptedLanguageHeaderLocale = validateLocale(acceptedLanguageHeaderLocaleStr);
		LOGGER.info("locale info after validatation: {} / {} / {} / {}",
				requestParameterLocale, xHeaderLocale,
				acceptedLanguageHeaderLocale, sessionLocale);

		if (null != requestParameterLocale) {
			LOGGER.info(
					"Locale info from request parameter is {}, set attribute into session.",
					requestParameterLocale);
			session.setAttribute(localSessionAttributeName,
					requestParameterLocale);
			session.setAttribute("locale", requestParameterLocale);
			return;
		}

		if (null != xHeaderLocale) {
			LOGGER.info(
					"Locale info from X Header is {}, set attribute into session.",
					xHeaderLocale);
			session.setAttribute(localSessionAttributeName, xHeaderLocale);
			session.setAttribute("locale", xHeaderLocale);
			return;
		}

		if (null != acceptedLanguageHeaderLocale) {
			LOGGER.info(
					"Locale info from accepted-language is {}, set attribute into session.",
					acceptedLanguageHeaderLocale);
			session.setAttribute(localSessionAttributeName,
					acceptedLanguageHeaderLocale);
			session.setAttribute("locale", acceptedLanguageHeaderLocale);
			return;
		}

		if (null != sessionLocale) {
			LOGGER.info(
					"Locale info from session is {}, set attribute into session.",
					sessionLocale);
			session.setAttribute(localSessionAttributeName, sessionLocale);
			return;
		}

		LOGGER.info("Default locale is {}, set attribute into session.",
				Locale.US);
		session.setAttribute(localSessionAttributeName, Locale.US);
		session.setAttribute("locale", Locale.US);
		return;
	}

	private Locale validateLocale(String locale) {

		if (StringUtils.isBlank(locale)) {
			return null;
		}

		Locale localeValidated = null;

		// this piece of wierd logics is because some jsps' locale parameter
		// expect the locale to be like "en_US" and "zh_CN"
		// rather than "en" and "zh".
		// otherwise, error happens in js.
		String localeStr = null;
		if (locale.contains("zh") || locale.contains("CN")) {
			localeStr = "zh_CN";
		} else if (locale.toUpperCase().contains("EN_GB")) {
			localeStr = "en_GB";
		} else {
			localeStr = "en_US";
		}
		locale = localeStr;

		try {
			localeValidated = LocaleUtils.toLocale(locale);
		} catch (IllegalArgumentException e) {
			LOGGER.warn("locale  '" + locale
					+ "' is illegal in request, ignore it", e);
			return null;
		}

		return localeValidated;
	}

}
