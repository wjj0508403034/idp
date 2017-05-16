package com.huoyun.idp.servicetoken.impl;

import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.servicetoken.ServiceToken;
import com.huoyun.idp.servicetoken.ServiceTokenService;
import com.huoyun.idp.servicetoken.repository.ServiceTokenRepo;

public class ServiceTokenServiceImpl implements ServiceTokenService {

	private Facade facade;

	public ServiceTokenServiceImpl(Facade facade) {
		this.facade = facade;
	}

	@Override
	public boolean isValid(String name, String token) {
		return this.facade.getService(ServiceTokenRepo.class).isValid(name, token);
	}

	@Transactional
	@Override
	public ServiceToken generateToken(String name) {
		ServiceToken token = new ServiceToken();
		token.setName(name);
		token.setToken(UUID.randomUUID().toString());
		return this.facade.getService(ServiceTokenRepo.class).save(token);
	}

}
