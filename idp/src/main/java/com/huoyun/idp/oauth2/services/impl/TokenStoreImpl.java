package com.huoyun.idp.oauth2.services.impl;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.common.util.SerializationUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;

import com.huoyun.idp.common.Facade;
import com.huoyun.idp.oauth2.entities.AccessTokenEntity;
import com.huoyun.idp.oauth2.entities.RefreshTokenEntity;
import com.huoyun.idp.oauth2.repositories.AccessTokenRepository;
import com.huoyun.idp.oauth2.repositories.RefreshTokenRepository;
import com.huoyun.idp.oauth2.services.TokenStore;

public class TokenStoreImpl implements TokenStore {
	private static final Log LOG = LogFactory.getLog(TokenStoreImpl.class);
	private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();
	private Facade facade;
	private AccessTokenRepository accessTokenRepo;
	private RefreshTokenRepository refreshTokenRepo;

	public TokenStoreImpl(Facade facade) {
		this.facade = facade;
		this.accessTokenRepo = this.facade
				.getService(AccessTokenRepository.class);

		this.refreshTokenRepo = this.facade
				.getService(RefreshTokenRepository.class);
	}

	@Override
	public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
		return readAuthentication(token.getValue());

	}

	@Override
	public OAuth2Authentication readAuthentication(String token) {
		AccessTokenEntity entity = this.accessTokenRepo
				.findByTokenId(extractTokenKey(token));
		if (entity == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find access token for token " + token);
			}

			return null;
		}

		return SerializationUtils.deserialize(entity.getAuthentication());
	}

	@Override
	public OAuth2AccessToken readAccessToken(String tokenValue) {
		AccessTokenEntity entity = this.accessTokenRepo
				.findByTokenId(extractTokenKey(tokenValue));
		if (entity == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find access token for token " + tokenValue);
			}

			return null;
		}

		return SerializationUtils.deserialize(entity.getToken());
	}

	@Override
	public void storeAccessToken(OAuth2AccessToken token,
			OAuth2Authentication authentication) {
		String refreshToken = null;
		if (token.getRefreshToken() != null) {
			refreshToken = token.getRefreshToken().getValue();
		}

		if (readAccessToken(token.getValue()) != null) {
			removeAccessToken(token.getValue());
		}

		AccessTokenEntity entity = new AccessTokenEntity();
		entity.setTokenId(extractTokenKey(token.getValue()));
		entity.setToken(SerializationUtils.serialize(token));
		entity.setAuthenticationId(authenticationKeyGenerator
				.extractKey(authentication));
		entity.setUserName(authentication.isClientOnly() ? null
				: authentication.getName());
		entity.setClientId(authentication.getOAuth2Request().getClientId());
		entity.setAuthentication(SerializationUtils.serialize(authentication));
		entity.setRefreshToken(refreshToken);
		this.accessTokenRepo.save(entity);
	}

	@Override
	public void removeAccessToken(OAuth2AccessToken token) {
		removeAccessToken(token.getValue());
	}

	@Override
	public void storeRefreshToken(OAuth2RefreshToken refreshToken,
			OAuth2Authentication authentication) {
		RefreshTokenEntity entity = new RefreshTokenEntity();
		entity.setTokenId(extractTokenKey(refreshToken.getValue()));
		entity.setToken(SerializationUtils.serialize(refreshToken));
		entity.setAuthentication(SerializationUtils.serialize(authentication));
		this.refreshTokenRepo.save(entity);
	}

	@Override
	public OAuth2RefreshToken readRefreshToken(String token) {

		RefreshTokenEntity entity = this.refreshTokenRepo
				.findByTokenId(extractTokenKey(token));
		if (entity == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find refresh token for token " + token);
			}

			return null;
		}

		return SerializationUtils.deserialize(entity.getToken());
	}

	@Override
	public OAuth2Authentication readAuthenticationForRefreshToken(
			OAuth2RefreshToken token) {
		return readAuthenticationForRefreshToken(token.getValue());
	}

	private OAuth2Authentication readAuthenticationForRefreshToken(String value) {
		RefreshTokenEntity entity = this.refreshTokenRepo
				.findByTokenId(extractTokenKey(value));

		if (entity == null) {
			if (LOG.isInfoEnabled()) {
				LOG.info("Failed to find refresh token for token " + value);
			}

			return null;
		}

		return SerializationUtils.deserialize(entity.getAuthentication());

	}

	@Override
	public void removeRefreshToken(OAuth2RefreshToken token) {
		removeRefreshToken(token.getValue());
	}

	@Override
	public void removeAccessTokenUsingRefreshToken(
			OAuth2RefreshToken refreshToken) {
		removeAccessTokenUsingRefreshToken(refreshToken.getValue());
	}

	@Override
	public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {

		String key = authenticationKeyGenerator.extractKey(authentication);
		AccessTokenEntity entity = this.accessTokenRepo
				.findByAuthenticationId(key);
		if (entity == null) {
			return null;
		}

		OAuth2AccessToken accessToken = SerializationUtils.deserialize(entity
				.getToken());

		if (accessToken != null
				&& !key.equals(authenticationKeyGenerator
						.extractKey(readAuthentication(accessToken.getValue())))) {
			removeAccessToken(accessToken.getValue());
			// Keep the store consistent (maybe the same user is represented by
			// this authentication but the details have
			// changed)
			storeAccessToken(accessToken, authentication);
		}
		return accessToken;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(
			String clientId, String userName) {
		List<OAuth2AccessToken> accessTokens = new ArrayList<>();
		List<AccessTokenEntity> entities = this.accessTokenRepo
				.findByClientIdAndUserName(clientId, userName);
		for (AccessTokenEntity entity : entities) {
			accessTokens.add(SerializationUtils.deserialize(entity.getToken()));
		}

		return accessTokens;
	}

	@Override
	public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
		List<OAuth2AccessToken> accessTokens = new ArrayList<>();
		List<AccessTokenEntity> entities = this.accessTokenRepo
				.findByClientId(clientId);
		for (AccessTokenEntity entity : entities) {
			accessTokens.add(SerializationUtils.deserialize(entity.getToken()));
		}

		return accessTokens;
	}

	private void removeAccessToken(String tokenValue) {
		this.accessTokenRepo.removeByTokenId(extractTokenKey(tokenValue));
	}

	private void removeRefreshToken(String token) {
		this.refreshTokenRepo.removeByTokenId(extractTokenKey(token));
	}

	private void removeAccessTokenUsingRefreshToken(String refreshToken) {
		this.accessTokenRepo
				.removeByRefreshToken(extractTokenKey(refreshToken));
	}

	private String extractTokenKey(String value) {
		if (value == null) {
			return null;
		}
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"MD5 algorithm not available.  Fatal (should be in the JDK).");
		}

		try {
			byte[] bytes = digest.digest(value.getBytes("UTF-8"));
			return String.format("%032x", new BigInteger(1, bytes));
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(
					"UTF-8 encoding not available.  Fatal (should be in the JDK).");
		}
	}
}
