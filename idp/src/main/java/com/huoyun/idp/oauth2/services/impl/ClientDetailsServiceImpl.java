package com.huoyun.idp.oauth2.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.oauth2.clientdetails.ClientDetailsParam;
import com.huoyun.idp.oauth2.entities.ClientDetailsEntity;
import com.huoyun.idp.oauth2.repositories.ClientDetailsRepository;
import com.huoyun.idp.oauth2.services.ClientDetailsService;

public class ClientDetailsServiceImpl implements ClientDetailsService {

	private final static String AuthorizedGrantTypes = "authorization_code;refresh_token";
	private Facade facade;
	private ClientDetailsRepository clientDetailsRepo;

	public ClientDetailsServiceImpl(Facade facade) {
		this.facade = facade;
		this.clientDetailsRepo = this.facade
				.getService(ClientDetailsRepository.class);
	}

	@Override
	public ClientDetails loadClientByClientId(String clientId)
			throws InvalidClientException {
		ClientDetailsEntity entity = this.clientDetailsRepo
				.findByClientId(clientId);
		if (entity == null) {
			throw new NoSuchClientException("No client with requested id: "
					+ clientId);
		}
		return this.parse(entity);
	}

	@Override
	public ClientDetails addClientDetails(ClientDetailsParam clientDetailsParam)
			throws ClientAlreadyExistsException {
		boolean exists = this.clientDetailsRepo
				.existsCheckBeforeCreate(clientDetailsParam.getName());
		if (exists) {
			throw new ClientAlreadyExistsException("Client already exists: "
					+ clientDetailsParam.getName());
		}
		ClientDetailsEntity entity = new ClientDetailsEntity();
		entity.setName(clientDetailsParam.getName());
		entity.setClientId(generateClientId());
		entity.setClientSecret(generateClientSecret());
		entity.setScope(clientDetailsParam.getScope());
		entity.setRedirectUri(clientDetailsParam.getRedirectUri());
		entity.setAuthorizedGrantTypes(AuthorizedGrantTypes);
		entity = this.clientDetailsRepo.save(entity);
		return this.parse(entity);
	}

	private ClientDetails parse(ClientDetailsEntity entity) {
		BaseClientDetails clientDetails = new BaseClientDetails();
		clientDetails.setClientId(entity.getClientId());
		clientDetails.setClientSecret(entity.getClientSecret());
		clientDetails.setScope(parseScopes(entity.getScope()));
		clientDetails.setAuthorizedGrantTypes(parseGrantTypes(entity
				.getAuthorizedGrantTypes()));
		clientDetails.setRegisteredRedirectUri(parseRedirectUri(entity
				.getRedirectUri()));
		return clientDetails;
	}

	private List<String> parseScopes(String scopes) {
		List<String> scopeColl = new ArrayList<>();
		for (String scopeItem : scopes.split(";")) {
			scopeColl.add(scopeItem);
		}

		return scopeColl;
	}

	private List<String> parseGrantTypes(String grantTypes) {
		List<String> grantTypeColl = new ArrayList<>();
		for (String grantType : grantTypes.split(";")) {
			grantTypeColl.add(grantType);
		}

		return grantTypeColl;
	}

	private Set<String> parseRedirectUri(String redirectUris) {
		Set<String> uris = new HashSet<>();
		for (String uri : redirectUris.split(";")) {
			uris.add(uri);
		}

		return uris;
	}

	private String generateClientId() {
		return UUID.randomUUID().toString();
	}

	private String generateClientSecret() {
		return UUID.randomUUID().toString();
	}

}
