package com.huoyun.idp.oauth2.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.oauth2.clientdetails.ClientDetailsParam;
import com.huoyun.idp.oauth2.services.ClientDetailsService;

@Controller
@RequestMapping("/oauth2/clientDetails")
public class ClientDetailsController {

	//@Autowired
	//private ClientDetailsService clientDetailsService;
	
	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public ClientDetails createClientDetails(@RequestBody ClientDetailsParam clientDetailsParam){
		//return this.clientDetailsService.addClientDetails(clientDetailsParam);
		return null;
	}
}
