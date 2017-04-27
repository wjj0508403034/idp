package com.huoyun.idp.sso;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

public class SSOFilter extends GenericFilterBean {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(SSOFilter.class);

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

		chain.doFilter(httpRequest, response);
		long stopWatch = System.currentTimeMillis() - startTime;
		LOGGER.info("IDP Request end, method: [{}], url: [{}], cost: [{}]ms.",
				method, requestURL, stopWatch);

	}

}
