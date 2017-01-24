package com.huoyun.idp.oauth2.services;

import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import com.huoyun.idp.oauth2.clientdetails.ClientDetailsParam;

public interface ClientDetailsService extends
		org.springframework.security.oauth2.provider.ClientDetailsService {

	ClientDetails addClientDetails(ClientDetailsParam clientDetailsParam)
			throws ClientAlreadyExistsException;
}
