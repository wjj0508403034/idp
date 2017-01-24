package com.huoyun.idp.user.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.huoyun.idp.user.UserInfo;
import com.huoyun.idp.user.UserService;
import com.huoyun.idp.user.entity.User;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserService userService;

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException {
		User user = this.userService.getUserByName(username);
		if (user == null) {
			throw new UsernameNotFoundException("not found");
		}
		return new UserInfo(user);
	}

}
