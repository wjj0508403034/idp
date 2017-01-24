package com.huoyun.idp.oauth2.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.huoyun.idp.common.AbstractBusinessObject;

@Entity
@Table
public class RefreshTokenEntity extends AbstractBusinessObject {

	private static final long serialVersionUID = -9103933437703724969L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;

	@Column
	private String tokenId;

	@Lob
	@Column
	private byte[] token;

	@Lob
	@Column
	private byte[] authentication;

	@Override
	public Long getId() {
		return this.id;
	}

	public String getTokenId() {
		return tokenId;
	}

	public void setTokenId(String tokenId) {
		this.tokenId = tokenId;
	}

	public byte[] getToken() {
		return token;
	}

	public void setToken(byte[] token) {
		this.token = token;
	}

	public byte[] getAuthentication() {
		return authentication;
	}

	public void setAuthentication(byte[] authentication) {
		this.authentication = authentication;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
