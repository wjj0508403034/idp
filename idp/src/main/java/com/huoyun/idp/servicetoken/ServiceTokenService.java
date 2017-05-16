package com.huoyun.idp.servicetoken;

public interface ServiceTokenService {

	boolean isValid(String name, String token);

	ServiceToken generateToken(String name);
}
