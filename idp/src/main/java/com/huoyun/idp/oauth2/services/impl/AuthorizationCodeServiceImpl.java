package com.huoyun.idp.oauth2.services.impl;

import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.oauth2.entities.AuthorizeCodeEntity;
import com.huoyun.idp.oauth2.repositories.AuthorizeCodeRepository;
import com.huoyun.idp.oauth2.services.AuthorizationCodeService;

public class AuthorizationCodeServiceImpl extends
		RandomValueAuthorizationCodeServices implements
		AuthorizationCodeService {

	private AuthorizeCodeRepository authorizeCodeRepo;
	private Facade facade;

	public AuthorizationCodeServiceImpl(Facade facade) {
		this.facade = facade;
		this.authorizeCodeRepo = this.facade
				.getService(AuthorizeCodeRepository.class);
	}

	@Override
	protected void store(String code, OAuth2Authentication authentication) {
		byte[] bytes = SerializationUtils.serialize(authentication);
		AuthorizeCodeEntity entity = new AuthorizeCodeEntity();
		entity.setCode(code);
		entity.setAuthentication(bytes);
		this.authorizeCodeRepo.save(entity);
	}

	@Override
	protected OAuth2Authentication remove(String code) {
		AuthorizeCodeEntity entity = this.authorizeCodeRepo.findByCode(code);
		if (entity == null) {
			return null;
		}

		OAuth2Authentication authentication = SerializationUtils
				.deserialize(entity.getAuthentication());
		this.authorizeCodeRepo.remove(code);
		return authentication;
	}

}
