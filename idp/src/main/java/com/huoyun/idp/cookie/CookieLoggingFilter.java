package com.huoyun.idp.cookie;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieLoggingFilter implements Filter {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(CookieLoggingFilter.class);

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {
		ResponseWrapper wrappedResponse = new ResponseWrapper(
				(HttpServletResponse) response);

		filterChain.doFilter(request, wrappedResponse);

		LOGGER.info("CookieLoggingFilter sso url: {}.",
				((HttpServletRequest) request).getRequestURL().toString());
		LOGGER.info("CookieLoggingFilter sso email: {}.",
				((HttpServletRequest) request).getParameter("j_username"));
		for (Cookie cookie : wrappedResponse.cookies) {
			LOGGER.info("CookieLoggingFilter cookies: {} {}", cookie.getName(),
					cookie.getValue());
		}

	}

	private class ResponseWrapper extends HttpServletResponseWrapper {

		private Collection<Cookie> cookies = new ArrayList<Cookie>();

		public ResponseWrapper(HttpServletResponse response) {
			super(response);
		}

		@Override
		public void addCookie(Cookie cookie) {
			super.addCookie(cookie);
			cookies.add(cookie);
		}
	}

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub

	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}
}
