package com.huoyun.idp.user;

import org.springframework.security.core.authority.AuthorityUtils;

import com.huoyun.idp.user.entity.User;

public class UserInfo extends
		org.springframework.security.core.userdetails.User {

	private static final long serialVersionUID = -14150848395010645L;
	private User user;

	public UserInfo(User user) {
		super(user.getEmail(), user.getPassword(), true, true, true, true,
				AuthorityUtils.createAuthorityList(user.getRole().toString()));
		this.user = user;
	}

	public User getUser() {
		return user;
	}

}
