package com.huoyun.idp.internal.api;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.common.FacadeAware;
import com.huoyun.idp.servicetoken.ServiceTokenService;

public class InternalApiAuthenticationFilter implements Filter, FacadeAware {

	private static final Logger LOGGER = LoggerFactory.getLogger(InternalApiAuthenticationFilter.class);

	private final static String HTTP_Header_X_VIA = "X-VIA";
	private final static String HTTP_Header_X_SERVER_TOKEN = "X-SERVER-TOKEN";
	private ServiceTokenService serviceTokenService;

	@Override
	public void destroy() {

	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest httpRequest = (HttpServletRequest) req;
		HttpServletResponse httpResponse = (HttpServletResponse) res;

		LOGGER.info("Request {} start to service token validating ...", httpRequest.getRequestURI());

		String serviceName = httpRequest.getHeader(HTTP_Header_X_VIA);
		String serviceToken = httpRequest.getHeader(HTTP_Header_X_SERVER_TOKEN);
		if (!this.serviceTokenService.isValid(serviceName, serviceToken)) {
			LOGGER.warn("Service name {} and token {} not match, block api access.", serviceName, serviceToken);
			httpResponse.setStatus(401);
			return;
		}

		LOGGER.info("Request {} start to service token validated pass.", httpRequest.getRequestURI());
		chain.doFilter(req, res);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {

	}

	@Override
	public void setFacade(Facade facade) {
		this.serviceTokenService = facade.getService(ServiceTokenService.class);
	}

}
