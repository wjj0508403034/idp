package com.huoyun.idp.constants;

public class EndpointsConstants {

	public static final int IDP_STATUS_ACTIVE = 1;

    public static final int IDP_STATUS_LOGGED_OUT = 2;

    public static final int IDP_STATUS_LOGOUT_IN_PROGRESS = 3;

    public static final int IDP_STATUS_PARTIAL_LOGOUT = 4;

    public static final int IDP_STATUS_LOGOUT_FAILED = 5;

    public static final int SP_STATUS_ACTIVE = 6;

    public static final int SP_STATUS_LOGGED_OUT = 7;

    public static final int SP_STATUS_LOGOUT_IN_PROGRESS = 8;

    public static final int SP_STATUS_LOGOUT_FAILED = 9;

    public static final String SESSION_CONTEXT_ATTRIBUTE_NAME = "com.sap.security.saml2.idp.endpoints.sso.session.context";

    public static final String GENERAL_AUTHENTICATION_REQUEST_ERROR_MESSAGE = "Identity Provider could not process the authentication request received.";

    public static final String PASSIVE_NO_SESSION_ERROR_MESSAGE = "No IDP session exists and no valid credentials were provided.";

    public static final String USER_SP_UNAUTHORIZED_ERROR_MESSAGE = "User is not permitted to access the specified service provider";

    public static final String SAML2_IDP_AUTHENTICATION_STACK = "IDP";

    public static final String SAML2_IDP_PASSIVE_AUTHENTICATION_STACK = "IDP_PASSIVE";

    public static final String LDAP_ATTRIBUTE_MAIL = "mail";

    public static final String LDAP_ATTRIBUTE_MAIL_VERIFIED = "mailVerified";

    public static final String TRUE = "true";

    public static final String IDP_LOGIN_SESS_ATTR = "_IDP_LOGIN_SESS_ATTR_";

    public static final String SAML2_USER_SESS_ATTR = "_SAML2_USER_SESS_ATTR_";

    public static final String SAML2_USER_CHANGE_PASSWORD_REQUIRED_ATTR = "_SAML2_USER_CHANGE_PASSWORD_REQUIRED_ATTR_";

    public static final String SAML2_USER_IDP_SESS_ATTR = "_SAML2_USER_IDP_SESS_ATTR_";

    public static final String SAML2_IDP_CONFIGURATION_ATTR = "_SAML2_USER_IDP_CONFIGURATION_SESS_ATTR_";

    public static final String LOGGER_USER_NAME = "$LOGGER_USER_NAME$";

    public static final String LOCALE_SESSION_ATTR = "locale";

    public static final String PERSISTENT_LOGIN_COOKIE = "PL";
}
