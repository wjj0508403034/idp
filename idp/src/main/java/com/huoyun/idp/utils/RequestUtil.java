package com.huoyun.idp.utils;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;

public class RequestUtil {

	private static final String UNKNOWN = "unknown";

	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("X-Real-IP");
		if (!StringUtils.isEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
			return ip;
		}
		ip = request.getHeader("X-Forwarded-For");
		if (!StringUtils.isEmpty(ip) && !UNKNOWN.equalsIgnoreCase(ip)) {
			// get first IP from proxy
			int index = ip.indexOf(',');
			if (index != -1) {
				ip = ip.substring(0, index);
			}
			return ip;
		}
		return request.getRemoteAddr();
	}
}
