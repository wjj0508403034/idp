package com.huoyun.idp.saml2.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationCustomImpl;
import com.huoyun.idp.saml2.configuration.SAML2IdPConfigurationFactory;
import com.sap.security.saml2.cfg.exceptions.SAML2ConfigurationException;
import com.sap.security.saml2.cfg.metadata.SAML2EndpointURL;
import com.sap.security.saml2.cfg.metadata.SAML2MetadataGenerator;
import com.sap.security.saml2.lib.common.SAML2Exception;

@Controller
@RequestMapping("/saml2/")
public class MetadataController {

	@Autowired
	private SAML2MetadataGenerator metadataGenerator;

	@Autowired
	private SAML2IdPConfigurationFactory idpConfigurationFactory;

	@RequestMapping(value = "/metadata", produces = "application/xml")
	@ResponseBody
	public ResponseEntity<String> metadata(HttpServletRequest httpRequest)
			throws IOException, IllegalArgumentException, SAML2Exception,
			SAML2ConfigurationException {
		SAML2EndpointURL url = new SAML2EndpointURL(httpRequest.getRequestURL()
				.toString());
		SAML2IdPConfigurationCustomImpl idpConfiguration = idpConfigurationFactory
				.getDefaultSAML2IdpConfiguration();
		String metadataXML = metadataGenerator.generateMetadata(
				idpConfiguration, url, null, null);
		HttpHeaders responseHeaders = new HttpHeaders();
		responseHeaders.setContentType(MediaType.APPLICATION_XML);
		return new ResponseEntity<String>(metadataXML, responseHeaders,
				HttpStatus.OK);
	}
}
