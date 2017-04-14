package com.huoyun.idp.saml2.configuration;

import java.io.Serializable;

public class SAML2SPEndpoints implements Serializable {

	private static final long serialVersionUID = -7442657477900023753L;
	private String acs;
	private String slo;

	public SAML2SPEndpoints(String acs, String slo) {
		this.acs = acs;
		this.slo = slo;
	}

	public String getAcs() {
		return acs;
	}

	public void setAcs(String acs) {
		this.acs = acs;
	}

	public String getSlo() {
		return slo;
	}

	public void setSlo(String slo) {
		this.slo = slo;
	}
}
