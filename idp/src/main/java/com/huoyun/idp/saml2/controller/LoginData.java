package com.huoyun.idp.saml2.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LoginData {

	private Map<String, Collection<String>> map = new HashMap<>();

	public void set(String key, String value) {
		if (!this.map.containsKey(key)) {
			this.map.put(key, new ArrayList<>());
		}

		this.map.get(key).add(value);
	}

	public Map<String, Collection<String>> getValue() {
		return this.map;
	}
}
