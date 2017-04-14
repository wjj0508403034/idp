package com.huoyun.idp.redis;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import com.huoyun.idp.encrypt.EncryptService;
import com.huoyun.idp.redis.impl.RedisSessionManagerImpl;

@Configuration
public class RedisAutoConfiguration {

	@SuppressWarnings("rawtypes")
	@Bean
	public RedisSessionManager redisSessionManager(RedisTemplate redisTemplate) {
		return new RedisSessionManagerImpl(redisTemplate, new EncryptService());
	}
}
