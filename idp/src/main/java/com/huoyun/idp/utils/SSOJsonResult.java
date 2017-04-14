package com.huoyun.idp.utils;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class SSOJsonResult {
	private final static Map<String, Object> EMPTY_MAP = new LinkedHashMap<String, Object>();

	private boolean success;
	private String description = "";
	private boolean needRedirect = false;
	private String redirectType = "";
	private String redirectUrl = "";
	private String nextPage = "";
	private Map<String, Object> redirectParam = EMPTY_MAP;
	private boolean validateCaptcha = false;
	private String retryMessage = "";

	private SSOJsonResult(boolean pSuccess, String pDesc,
			boolean pNeedRedirect, String pRedirectUrl, String nextPage,
			String redirectType, Map<String, Object> pRedirectParam) {
		this.redirectType = (redirectType == null ? "" : redirectType);
		this.success = pSuccess;
		this.description = (pDesc == null ? "" : pDesc);
		this.needRedirect = pNeedRedirect;
		this.redirectUrl = (pRedirectUrl == null ? "" : pRedirectUrl);
		this.nextPage = (nextPage == null ? "" : nextPage);
		this.redirectParam = pRedirectParam == null ? EMPTY_MAP
				: pRedirectParam;
	}

	public boolean isSuccess() {
		return success;
	}

	public String getDescription() {
		return description;
	}

	public boolean getNeedRedirect() {
		return needRedirect;
	}

	public String getRedirectUrl() {
		return redirectUrl;
	}

	public String getNextPage() {
		return nextPage;
	}

	public String getRedirectType() {
		return redirectType;
	}

	public Map<String, Object> getRedirectParam() {
		return redirectParam;
	}

	public boolean isValidateCaptcha() {
		return validateCaptcha;
	}

	public static SSOJsonResult buildSuccess() {
		SSOJsonResult result = new SSOJsonResult(true, "", false, "", "", "",
				EMPTY_MAP);
		return result;
	}

	public static SSOJsonResult buildFailure(final String reason) {
		SSOJsonResult result = new SSOJsonResult(false,
				StringUtils.trimToEmpty(reason), false, "", "", "", EMPTY_MAP);
		return result;
	}

	public static SSOJsonResult buildRedirect(final String url,
			final String redirectType, final Map<String, Object> param) {
		SSOJsonResult result = null;

		if (url.indexOf("invalidlink") != -1) {
			result = new SSOJsonResult(false, "", true,
					StringUtils.trimToEmpty(url), "", redirectType, param);
		} else {
			result = new SSOJsonResult(true, "", true,
					StringUtils.trimToEmpty(url), "", redirectType, param);
		}
		return result;
	}

	public static SSOJsonResult buildNextPage(String nextPage,
			final Map<String, Object> param) {
		SSOJsonResult result = new SSOJsonResult(true, "", false, "", nextPage,
				"GET", param);

		return result;
	}

	public static SSOJsonResult buildRedirect(final String url) {
		return buildRedirect(url, "GET");
	}

	public static SSOJsonResult buildRedirect(final String url,
			final String redirectType) {
		SSOJsonResult result = new SSOJsonResult(true, "", true,
				StringUtils.trimToEmpty(url), "", redirectType, EMPTY_MAP);
		return result;
	}

	public static SSOJsonResult buildNextPage(final String nextPage) {
		SSOJsonResult result = new SSOJsonResult(true, "", false, "", nextPage,
				"GET", EMPTY_MAP);
		return result;
	}

	public void setValidateCaptcha(boolean validateCaptcha) {
		this.validateCaptcha = validateCaptcha;
	}

	public String getRetryMessage() {
		return retryMessage;
	}

	public void setRetryMessage(String retryMessage) {
		this.retryMessage = retryMessage;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
