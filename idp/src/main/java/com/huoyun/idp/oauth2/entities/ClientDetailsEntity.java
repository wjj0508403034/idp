package com.huoyun.idp.oauth2.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.huoyun.idp.common.AbstractBusinessObject;

@Entity
@Table
public class ClientDetailsEntity extends AbstractBusinessObject {

	private static final long serialVersionUID = 5308788574977838517L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column(unique = true)
	private String name;

	@Column(unique = true)
	private String clientId;

	@Column
	private String resourceIds;

	@Column
	private String clientSecret;

	@Column
	private String scope;

	@Column
	private String authorizedGrantTypes;

	@Column
	private String redirectUri;

	@Column
	private String authorities;

	@Column
	private boolean accessTokenValidity;

	@Column
	private boolean refreshTokenValidity;

	@Column
	private String additionalInformation;

	@Column
	private boolean autoapprove;

	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getClientId() {
		return clientId;
	}

	public void setClientId(String clientId) {
		this.clientId = clientId;
	}

	public String getResourceIds() {
		return resourceIds;
	}

	public void setResourceIds(String resourceIds) {
		this.resourceIds = resourceIds;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public void setClientSecret(String clientSecret) {
		this.clientSecret = clientSecret;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getAuthorizedGrantTypes() {
		return authorizedGrantTypes;
	}

	public void setAuthorizedGrantTypes(String authorizedGrantTypes) {
		this.authorizedGrantTypes = authorizedGrantTypes;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public void setRedirectUri(String redirectUri) {
		this.redirectUri = redirectUri;
	}

	public String getAuthorities() {
		return authorities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAuthorities(String authorities) {
		this.authorities = authorities;
	}

	public boolean isAccessTokenValidity() {
		return accessTokenValidity;
	}

	public void setAccessTokenValidity(boolean accessTokenValidity) {
		this.accessTokenValidity = accessTokenValidity;
	}

	public boolean isRefreshTokenValidity() {
		return refreshTokenValidity;
	}

	public void setRefreshTokenValidity(boolean refreshTokenValidity) {
		this.refreshTokenValidity = refreshTokenValidity;
	}

	public String getAdditionalInformation() {
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation) {
		this.additionalInformation = additionalInformation;
	}

	public boolean isAutoapprove() {
		return autoapprove;
	}

	public void setAutoapprove(boolean autoapprove) {
		this.autoapprove = autoapprove;
	}
}
