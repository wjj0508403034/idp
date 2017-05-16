package com.huoyun.idp.admin.servicetoken;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.huoyun.idp.exception.BusinessException;
import com.huoyun.idp.servicetoken.ServiceTokenService;

@Controller
@RequestMapping("/admin/servicetokens")
public class ServiceTokenController {

	@Autowired
	private ServiceTokenService tokenService;

	@RequestMapping(method = RequestMethod.POST)
	@ResponseBody
	public void createToken(@RequestBody CreateTokenParam createTokenParam) throws BusinessException {
		this.tokenService.generateToken(createTokenParam.getName());
	}
}
