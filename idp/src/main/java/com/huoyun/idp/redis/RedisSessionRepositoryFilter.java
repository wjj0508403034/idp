package com.huoyun.idp.redis;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.session.web.http.SessionRepositoryFilter;
import org.springframework.web.filter.OncePerRequestFilter;

@Order(SessionRepositoryFilter.DEFAULT_ORDER)
public class RedisSessionRepositoryFilter  extends OncePerRequestFilter{
	private static final Logger LOGGER = LoggerFactory
			.getLogger(RedisSessionRepositoryFilter.class);


	private ApplicationContext context;

	public RedisSessionRepositoryFilter(ApplicationContext context) {
		this.context = context;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		try {
			LOGGER.info("RedisSessionRepositoryFilter start...");
			
			@SuppressWarnings("rawtypes")
			SessionRepositoryFilter springSessionRepositoryFilter = this.context.getBean(SessionRepositoryFilter.class);

			springSessionRepositoryFilter.doFilter(request, response,
					filterChain);

			LOGGER.info("RedisSessionRepositoryFilter end...");

		} catch (RedisSystemException | RedisConnectionFailureException ex) {
			LOGGER.error(
					"Session repository filter exception, Redis connection failed.",
					ex);

			RedisSessionContext redisSessionContext = RedisSessionContext
					.getInstance();
			redisSessionContext.setRedisConnectionFailureTime(DateTime.now());

			throw ex;
		}

	}
}
